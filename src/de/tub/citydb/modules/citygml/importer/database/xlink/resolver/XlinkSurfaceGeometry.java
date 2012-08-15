/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgis.Polygon;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.modules.citygml.common.database.cache.HeapCacheTable;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;
import de.tub.citydb.util.Util;

public class XlinkSurfaceGeometry implements DBXlinkResolver {
	private static final ReentrantLock mainLock = new ReentrantLock();

	private final Connection batchConn;
	private final HeapCacheTable heapTable;
	private final Config config;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectTmpSurfGeom;
	private PreparedStatement psSelectSurfGeom;
	private PreparedStatement psUpdateSurfGeom;
	private PreparedStatement psParentElem;
	private PreparedStatement psMemberElem;

	private int parentBatchCounter;
	private int memberBatchCounter;
	private int updateBatchCounter;

	public XlinkSurfaceGeometry(Connection batchConn, HeapCacheTable heapTable, Config config, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.heapTable = heapTable;
		this.config = config;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "'";
		else
			gmlIdCodespace = "null";

		psSelectTmpSurfGeom = heapTable.getConnection().prepareStatement("select ID from " + heapTable.getTableName() + " where PARENT_ID=?");
		psSelectSurfGeom = batchConn.prepareStatement("WITH RECURSIVE geometry (id, gmlid, gmlid_codespace, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry, level) " +
              " AS (SELECT sg.*, 1 AS level FROM surface_geometry sg WHERE sg.id=? UNION ALL " +
              " SELECT sg.*, g.level + 1 AS level FROM surface_geometry sg, geometry g WHERE sg.parent_id=g.id)" + 
              " SELECT * FROM geometry ORDER BY level asc");
		psUpdateSurfGeom = batchConn.prepareStatement("update SURFACE_GEOMETRY set IS_XLINK=1 where ID=?");

		psParentElem = batchConn.prepareStatement("insert into SURFACE_GEOMETRY (ID, GMLID, GMLID_CODESPACE, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY) values "
				+ "(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, 1, ?, ?)");
		psMemberElem = batchConn.prepareStatement("insert into SURFACE_GEOMETRY (ID, GMLID, GMLID_CODESPACE, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY) values "
				+ "(nextval('SURFACE_GEOMETRY_ID_SEQ'), ?, " + gmlIdCodespace + ", ?, ?, 0, 0, 0, 1, ?, ?)");

	}

	public boolean insert(DBXlinkSurfaceGeometry xlink) throws SQLException {
		GmlIdEntry rootGeometryEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.ABSTRACT_GML_GEOMETRY);
		if (rootGeometryEntry == null || rootGeometryEntry.getRootId() == -1)
			return false;

		ResultSet rs = null;

		try {	
			// check whether we deal with a local gml:id
			// remote gml:ids are not supported so far...
			String gmlId = rootGeometryEntry.getMapping();
			if (Util.isRemoteXlink(gmlId))
				return false;

			// check whether this geometry is referenced by another xlink
			psSelectTmpSurfGeom.setLong(1, rootGeometryEntry.getId());
			rs = psSelectTmpSurfGeom.executeQuery();

			if (rs.next()) {
				// we need to re-work on this
				resolverManager.propagateXlink(xlink);
				return true;
			}

			boolean reverse = rootGeometryEntry.isReverse() ^ xlink.isReverse();			
			GeometryNode xlinkNode = read(rootGeometryEntry.getId(), reverse);
			if (xlinkNode == null)
				return false;

			// we cannot go on if we do not know the geometry type
			if (xlinkNode.type == GMLClass.UNDEFINED)
				return false;

			insert(xlinkNode, xlink.getId(), xlink.getRootId());
			psUpdateSurfGeom.setLong(1, xlinkNode.id);
			psUpdateSurfGeom.addBatch();
			if (++updateBatchCounter == Internal.POSTGRESQL_MAX_BATCH_SIZE)
				executeUpdateSurfGeomBatch();

			return true;

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

	private void insert(GeometryNode node,
			long parentId,
			long rootId) throws SQLException {

		// gml:id
		String gmlId = node.gmlId;

		if (node.geometry != null) {
			PGgeometry pgGeom = new PGgeometry(node.geometry);

			psMemberElem.setString(1, gmlId);
			psMemberElem.setLong(2, parentId);
			psMemberElem.setLong(3, rootId);
			psMemberElem.setInt(4, node.isReverse ? 1 : 0);
			psMemberElem.setObject(5, pgGeom);

			psMemberElem.addBatch();
			if (++memberBatchCounter == Internal.POSTGRESQL_MAX_BATCH_SIZE) {
				psParentElem.executeBatch();
				psMemberElem.executeBatch();
				
				parentBatchCounter = 0;
				memberBatchCounter = 0;
			}

		} else if (node.type != GMLClass.POLYGON) {
			// set root entry
			long isSolid = node.isSolid ? 1 : 0;
			long isComposite = node.isComposite ? 1 : 0;
			long isTriangulated = node.isTriangulated ? 1 : 0;

			long surfaceGeometryId = resolverManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_ID_SEQ);

			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(3, parentId);
			psParentElem.setLong(4, rootId);
			psParentElem.setLong(5, isSolid);
			psParentElem.setLong(6, isComposite);
			psParentElem.setLong(7, isTriangulated);
			psParentElem.setInt(8, node.isReverse ? 1 : 0);
			psParentElem.setNull(9, Types.OTHER, "ST_GEOMETRY");

			psParentElem.addBatch();
			if (++parentBatchCounter == Internal.POSTGRESQL_MAX_BATCH_SIZE) {
				psParentElem.executeBatch();
				parentBatchCounter = 0;
			}

			for (GeometryNode childNode : node.childNodes)
				if (childNode.type != GMLClass.UNDEFINED)
					insert(childNode, surfaceGeometryId, rootId);
		}
	}

	private GeometryNode read(long rootId, boolean reverse) throws SQLException {
		ResultSet rs = null;

		try {
			psSelectSurfGeom.setLong(1, rootId);
			rs = psSelectSurfGeom.executeQuery();

			GeometryNode root = null;
			HashMap<Integer, GeometryNode> parentMap = new HashMap<Integer, GeometryNode>();

			// rebuild geometry hierarchy
			while (rs.next()) {
				long id = rs.getLong("ID");
				String gmlId = rs.getString("GMLID");
				int isSolid = rs.getInt("IS_SOLID");
				int isComposite = rs.getInt("IS_COMPOSITE");
				int isTriangulated = rs.getInt("IS_TRIANGULATED");
				int isReverse = rs.getInt("IS_REVERSE");
				int level = rs.getInt("LEVEL");

				Geometry geometry = null;
				PGgeometry pgGeometry = (PGgeometry)rs.getObject("GEOMETRY");
				if (!rs.wasNull() && pgGeometry != null)
					geometry = pgGeometry.getGeometry();

				// constructing a geometry node
				GeometryNode geomNode = new GeometryNode();
				geomNode.id = id;
				geomNode.gmlId = gmlId;
				geomNode.type = GMLClass.UNDEFINED;
				geomNode.isSolid = isSolid == 1;
				geomNode.isComposite = isComposite == 1;
				geomNode.isTriangulated = isTriangulated == 1;
				geomNode.isReverse = (isReverse == 1) ^ reverse;
				geomNode.geometry = geometry;

				if (root != null)
					parentMap.get(level).childNodes.add(geomNode);
				else
					root = geomNode;

				// make this node the parent for the next hierarchy level
				parentMap.put(level + 1, geomNode);			
			}

			// interpret geometry tree
			if (root != null) {
				rebuildGeometry(root, reverse);
				return root;
			} else {
				return null;
			}
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

	private void rebuildGeometry(GeometryNode geomNode, boolean reverse) throws SQLException {
		if (geomNode.geometry != null) {
			geomNode.type = GMLClass.POLYGON;

			// reverse order of geometry instance if necessary
			if (reverse) {
				
				String geomEWKT = "SRID=" + geomNode.geometry.getSrid() + ";POLYGON((";
				Polygon polyGeom = (Polygon) geomNode.geometry;
				int dimensions = geomNode.geometry.getDimension();
				
				for (int i = 0; i < polyGeom.numRings(); i++){
					
					if (dimensions == 2)
						for (int j = 0; j < polyGeom.getRing(i).numPoints(); j++){
							geomEWKT += polyGeom.getRing(i).getPoint(j).x + " " + polyGeom.getRing(i).getPoint(j).y + ",";
						}
					
					if (dimensions == 3)
						for (int j = 0; j < polyGeom.getRing(i).numPoints(); j++){
							geomEWKT += polyGeom.getRing(i).getPoint(j).x + " " + polyGeom.getRing(i).getPoint(j).y + " " + polyGeom.getRing(i).getPoint(j).z + ",";
						}					
					
					geomEWKT = geomEWKT.substring(0, geomEWKT.length() - 1);
					geomEWKT += "),(";				
				}
				
				geomEWKT = geomEWKT.substring(0, geomEWKT.length() - 2);
				geomEWKT += ")";
							
				Geometry geom = PGgeometry.geomFromString(geomEWKT);
				geomNode.geometry = geom;
			}

		} else if (!geomNode.isTriangulated) {
			if (!geomNode.isSolid && geomNode.isComposite) {
				// compositeSurface...
				geomNode.type = GMLClass.COMPOSITE_SURFACE;

				for (GeometryNode childNode : geomNode.childNodes)
					rebuildGeometry(childNode, reverse);

				return;
			}

			else if (geomNode.isSolid && geomNode.isComposite) {
				// compositeSolid
				geomNode.type = GMLClass.COMPOSITE_SOLID;

				for (GeometryNode childNode : geomNode.childNodes)
					rebuildGeometry(childNode, reverse);

				return;
			}

			else if (geomNode.isSolid && !geomNode.isComposite) {
				// a simple solid
				geomNode.type = GMLClass.SOLID;

				if (geomNode.childNodes.size() == 1)
					rebuildGeometry(geomNode.childNodes.get(0), reverse);

				return;
			}

			else if (!geomNode.isSolid && !geomNode.isComposite) {
				// differ between multiSurface and multiSolid
				if (!geomNode.childNodes.isEmpty()) {
					geomNode.type = GMLClass.MULTI_SOLID;

					// we have a multiSolid, if all childNodes are solids!
					for (GeometryNode childNode : geomNode.childNodes) {
						if (!childNode.isSolid && geomNode.type != GMLClass.MULTI_SURFACE) 
							geomNode.type = GMLClass.MULTI_SURFACE;

						rebuildGeometry(childNode, reverse);
					}
				}

				return;
			}

		} else if (geomNode.isTriangulated) {
			// triangulatedSurface...
			if (geomNode.childNodes == null || geomNode.childNodes.size() == 0)
				return;

			geomNode.type = GMLClass.TRIANGULATED_SURFACE;

			for (GeometryNode childNode : geomNode.childNodes)
				rebuildGeometry(childNode, reverse);

			return;
		}

		return;
	}

	private class GeometryNode {
		protected long id;
		protected String gmlId;
		protected GMLClass type;
		protected boolean isSolid;
		protected boolean isComposite;
		protected boolean isTriangulated;
		protected boolean isReverse;
		protected Geometry geometry;	
		protected List<GeometryNode> childNodes;

		public GeometryNode() {
			childNodes = new ArrayList<GeometryNode>();
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		psParentElem.executeBatch();
		psMemberElem.executeBatch();		
		parentBatchCounter = 0;
		memberBatchCounter = 0;

		executeUpdateSurfGeomBatch();
	}

	private void executeUpdateSurfGeomBatch() throws SQLException {
		// we need to synchronize updates otherwise Oracle will run into deadlocks
		// does PostgreSQL, too?
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
		psSelectTmpSurfGeom.close();
		psSelectSurfGeom.close();
		psUpdateSurfGeom.close();
		psParentElem.close();
		psMemberElem.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.SURFACE_GEOMETRY;
	}
}
