/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.database.DatabaseType;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSolidGeometry;

public class XlinkSolidGeometry implements DBXlinkResolver {
	private static final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectSurfGeom;
	private PreparedStatement psUpdateSurfGeom;

	private int updateBatchCounter;
	private int dbSrid;

	public XlinkSolidGeometry(Connection batchConn, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		dbSrid = resolverManager.getDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		psSelectSurfGeom = batchConn.prepareStatement(resolverManager.getDatabaseAdapter().getSQLAdapter().getHierarchicalGeometryQuery());

		StringBuilder stmt = new StringBuilder("update SURFACE_GEOMETRY set SOLID_GEOMETRY=");
		if (resolverManager.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS) {
			// the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
			// thus, we have to use the database function ST_GeomFromEWKT to insert such geometries
			// TODO: rework as soon as the JDBC driver supports PolyhedralSurface
			stmt.append("ST_GeomFromEWKT(?) ");	
		} else
			stmt.append("? ");

		stmt.append("where ID=?");
		psUpdateSurfGeom = batchConn.prepareStatement(stmt.toString());
	}

	public boolean insert(DBXlinkSolidGeometry xlink) throws SQLException {
		GeometryNode root = read(xlink.getId());
		if (root == null) {
			LOG.error("Failed to read solid geometry with id '" + xlink.getId() + "'.");
			return false;
		}

		// get solids from SURFACE_GEOMETRY as GeometryObject instances
		List<GeometryObject> solids = new ArrayList<>();
		rebuildSolids(root, solids, new ArrayList<GeometryObject>());

		if (!solids.isEmpty()) {
			GeometryObject solidObj = null;

			if (!root.isComposite) 
				solidObj = solids.get(0);
			else {
				GeometryObject[] tmp = new GeometryObject[solids.size()];

				int i = 0;
				for (GeometryObject solid : solids)
					tmp[i++] = solid;

				solidObj = GeometryObject.createCompositeSolid(tmp, dbSrid);
			}

			if (solidObj != null) {
				psUpdateSurfGeom.setObject(1, resolverManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(solidObj, batchConn));
				psUpdateSurfGeom.setLong(2, xlink.getId());
				psUpdateSurfGeom.addBatch();
				if (++updateBatchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
					executeBatch();
			}
		}

		return true;
	}

	private void rebuildSolids(GeometryNode node, List<GeometryObject> solids, List<GeometryObject> surfaces) {
		if (node.geometry != null)
			surfaces.add(node.geometry);

		// visit child nodes depth-first
		for (GeometryNode childNode : node.childNodes)
			rebuildSolids(childNode, solids, surfaces);

		// rebuild solid geometry
		if (node.isSolid && !node.isComposite) {
			if (!surfaces.isEmpty()) {
							
				int nrOfRings = 0;			
				for (GeometryObject surface : surfaces)
					nrOfRings += surface.getNumElements();

				int[] exteriorRings = new int[surfaces.size()];
				double[][] coordinates = new double[nrOfRings][];

				int ringNo = 0;
				for (int i = 0; i < surfaces.size(); i++) {
					GeometryObject surface = surfaces.get(i);
					exteriorRings[i] = ringNo;
					for (int j = 0; j < surface.getNumElements(); j++)
						coordinates[ringNo + j] = surface.getCoordinates(j);

					ringNo += surface.getNumElements();
				}

				solids.add(GeometryObject.createSolid(coordinates, exteriorRings, dbSrid));
				surfaces.clear();
			}
		}
	}

	private GeometryNode read(long rootId) throws SQLException {
		ResultSet rs = null;

		try {
			psSelectSurfGeom.setLong(1, rootId);
			rs = psSelectSurfGeom.executeQuery();

			GeometryNode root = null;
			HashMap<Long, GeometryNode> parentMap = new HashMap<Long, GeometryNode>();

			// rebuild geometry hierarchy
			while (rs.next()) {
				int isSolid = rs.getInt("IS_SOLID");
				int isComposite = rs.getInt("IS_COMPOSITE");
				long id = rs.getLong("ID");
				long parentId = rs.getInt("PARENT_ID");

				GeometryObject geometry = null;
				Object object = rs.getObject("GEOMETRY");
				if (!rs.wasNull() && object != null)
					geometry = resolverManager.getDatabaseAdapter().getGeometryConverter().getPolygon(object);

				// constructing a geometry node
				GeometryNode geomNode = new GeometryNode();
				geomNode.isSolid = isSolid == 1;
				geomNode.isComposite = isComposite == 1;
				geomNode.geometry = geometry;

				if (root != null) {
					GeometryNode parentNode = parentMap.get(parentId);
					if (parentNode == null)
						return null;
					
					parentNode.childNodes.add(geomNode);
				} else
					root = geomNode;

				// make this node the parent for the next hierarchy level
				if (geomNode.geometry == null)
					parentMap.put(id, geomNode);			
			}

			return root;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					//
				}

				rs = null;
			}
		}
	}

	private class GeometryNode {
		private boolean isSolid;
		private boolean isComposite;
		private GeometryObject geometry;
		private List<GeometryNode> childNodes = new ArrayList<GeometryNode>();
	}

	@Override
	public void executeBatch() throws SQLException {
		// we need to synchronize updates otherwise Oracle will run into deadlocks
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			psUpdateSurfGeom.executeBatch();
			updateBatchCounter = 0;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws SQLException {
		psSelectSurfGeom.close();
		psUpdateSurfGeom.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.SOLID_GEOMETRY;
	}
}
