/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.database.xlink.resolver;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class XlinkSurfaceGeometry implements DBXlinkResolver {
	private static final ReentrantLock mainLock = new ReentrantLock();

	private final Connection batchConn;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectTmpSurfGeom;
	private PreparedStatement psSelectSurfGeom;
	private PreparedStatement psUpdateSurfGeom;
	private PreparedStatement psParentElem;
	private PreparedStatement psMemberElem;
	private HashMap<String, PreparedStatement> psMap;

	private HashMap<String, Integer> psBatchCounterMap;
	private String schema;
	private int parentBatchCounter;
	private int memberBatchCounter;
	private int updateBatchCounter;
	
	public XlinkSurfaceGeometry(Connection batchConn, CacheTable cacheTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.resolverManager = resolverManager;

		psMap = new HashMap<String, PreparedStatement>();
		psBatchCounterMap = new HashMap<String, Integer>();
		schema = resolverManager.getDatabaseAdapter().getConnectionDetails().getSchema();

		psSelectTmpSurfGeom = cacheTable.getConnection().prepareStatement(new StringBuilder("select ID from ").append(cacheTable.getTableName()).append(" where PARENT_ID=? or ROOT_ID=?").toString());
		psSelectSurfGeom = batchConn.prepareStatement(resolverManager.getDatabaseAdapter().getSQLAdapter().getHierarchicalGeometryQuery());
		
		StringBuilder updateStmt = new StringBuilder()
		.append("update ").append(schema).append(".SURFACE_GEOMETRY set IS_XLINK=1 where ID=?");
		psUpdateSurfGeom = batchConn.prepareStatement(updateStmt.toString());
		
		StringBuilder parentStmt = new StringBuilder()
		.append("insert into ").append(schema).append(".SURFACE_GEOMETRY (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, SOLID_GEOMETRY, CITYOBJECT_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ");
		
		if (resolverManager.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS) {
			// the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
			// thus, we have to use the database function ST_GeomFromEWKT to insert such geometries
			// TODO: rework as soon as the JDBC driver supports PolyhedralSurface
			parentStmt.append("ST_GeomFromEWKT(?), ");	
		} else
			parentStmt.append("?, ");
		
		parentStmt.append("?)");
		psParentElem = batchConn.prepareStatement(parentStmt.toString());
		
		StringBuilder memberStmt = new StringBuilder()
		.append("insert into ").append(schema).append(".SURFACE_GEOMETRY (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, CITYOBJECT_ID) values ")
		.append("(").append(resolverManager.getDatabaseAdapter().getSQLAdapter().getNextSequenceValue(SequenceEnum.SURFACE_GEOMETRY_ID_SEQ.getName()))
		.append(", ?, ?, ?, 0, 0, 0, 1, ?, ?, ?)");
		psMemberElem = batchConn.prepareStatement(memberStmt.toString());
	}

	public boolean insert(DBXlinkSurfaceGeometry xlink) throws SQLException {
		UIDCacheEntry rootGeometryEntry = resolverManager.getGeometryId(xlink.getGmlId());
		if (rootGeometryEntry == null || rootGeometryEntry.getRootId() == -1) {
			// do not return an error in case of implicit geometries since the
			// the implicit geometry might be a point or curve
			return MappingConstants.IMPLICIT_GEOMETRY_TABLE.equalsIgnoreCase(xlink.getTable());
		}

		if (!MappingConstants.IMPLICIT_GEOMETRY_TABLE.equalsIgnoreCase(xlink.getTable())) {
			ResultSet rs = null;

			try {
				// check whether we deal with a local gml:id
				// remote gml:ids are not supported so far...
				String gmlId = rootGeometryEntry.getMapping();
				if (Util.isRemoteXlink(gmlId))
					return false;

				// check whether this geometry is referenced by another xlink
				psSelectTmpSurfGeom.setLong(1, rootGeometryEntry.getId());
				psSelectTmpSurfGeom.setLong(2, rootGeometryEntry.getId());
				rs = psSelectTmpSurfGeom.executeQuery();

				if (rs.next()) {
					// we need to re-work on this
					resolverManager.propagateXlink(xlink);
					return true;
				}

				boolean reverse = rootGeometryEntry.isReverse() ^ xlink.isReverse();			
				GeometryNode geomNode = read(rootGeometryEntry.getId(), reverse);
				if (geomNode == null)
					return false;

				long surfaceGeometryId = insert(geomNode, xlink.getId(), xlink.getRootId(), xlink.getCityObjectId());

				// if this is an xlink from a feature table, then we also let
				// the geometry column of this table point to the geometry object 
				if (xlink.getTable() != null) {
					String key = getKey(xlink.getTable(), xlink.getFromColumn());
					PreparedStatement ps = getUpdateStatement(xlink.getTable(), xlink.getFromColumn(), key);
					if (ps != null) {
						ps.setLong(1, surfaceGeometryId);
						ps.setLong(2, xlink.getCityObjectId());

						ps.addBatch();
						int counter = psBatchCounterMap.get(key);
						if (++counter == resolverManager.getDatabaseAdapter().getMaxBatchSize()) {
							ps.executeBatch();
							psBatchCounterMap.put(key, 0);
						} else
							psBatchCounterMap.put(key, counter);
					}
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

		psUpdateSurfGeom.setLong(1, rootGeometryEntry.getId());
		psUpdateSurfGeom.addBatch();
		if (++updateBatchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
			executeUpdateSurfGeomBatch();

		return true;
	}

	private long insert(GeometryNode node, long parentId, long rootId, long cityObjectId) throws SQLException {
		String gmlId = node.gmlId;
		long surfaceGeometryId = 0;

		if (node.geometry != null) {
			Object obj = resolverManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(node.geometry, batchConn);

			psMemberElem.setString(1, gmlId);
			psMemberElem.setLong(2, parentId);
			psMemberElem.setLong(3, rootId);
			psMemberElem.setInt(4, node.isReverse ? 1 : 0);
			psMemberElem.setObject(5, obj);

			if (cityObjectId != 0) 
				psMemberElem.setLong(6, cityObjectId);
			else
				psMemberElem.setNull(6, Types.NULL);

			psMemberElem.addBatch();
			if (++memberBatchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize()) {
				psParentElem.executeBatch();
				psMemberElem.executeBatch();
				parentBatchCounter = 0;
				memberBatchCounter = 0;
			}

		} else {
			// set root entry
			long isSolid = node.isSolid ? 1 : 0;
			long isComposite = node.isComposite ? 1 : 0;
			long isTriangulated = node.isTriangulated ? 1 : 0;
			surfaceGeometryId = resolverManager.getDBId(SequenceEnum.SURFACE_GEOMETRY_ID_SEQ.getName());
			if (rootId == 0)
				rootId = surfaceGeometryId;

			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setLong(5, isSolid);
			psParentElem.setLong(6, isComposite);
			psParentElem.setLong(7, isTriangulated);
			psParentElem.setInt(8, node.isReverse ? 1 : 0);
			psParentElem.setNull(9, resolverManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					resolverManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (node.solidGeometry != null)
				psParentElem.setObject(10, node.solidGeometry);
			else
				psParentElem.setNull(10, resolverManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
						resolverManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, Types.NULL);			

			if (cityObjectId != 0) 
				psParentElem.setLong(11, cityObjectId);
			else
				psParentElem.setNull(11, Types.NULL);

			psParentElem.addBatch();
			if (++parentBatchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize()) {
				psParentElem.executeBatch();
				parentBatchCounter = 0;
			}

			for (GeometryNode childNode : node.childNodes)
				insert(childNode, surfaceGeometryId, rootId, cityObjectId);
		}

		return surfaceGeometryId;
	}

	private GeometryNode read(long rootId, boolean reverse) throws SQLException {
		ResultSet rs = null;

		try {
			psSelectSurfGeom.setLong(1, rootId);
			rs = psSelectSurfGeom.executeQuery();

			GeometryNode root = null;
			HashMap<Long, GeometryNode> parentMap = new HashMap<Long, GeometryNode>();

			// rebuild geometry hierarchy
			while (rs.next()) {
				String gmlId = rs.getString("GMLID");
				int isSolid = rs.getInt("IS_SOLID");
				int isComposite = rs.getInt("IS_COMPOSITE");
				int isTriangulated = rs.getInt("IS_TRIANGULATED");
				int isReverse = rs.getInt("IS_REVERSE");				
				long id = rs.getLong("ID");
				long parent_id = rs.getInt("PARENT_ID");

				Object solidGeometry = null;
				if (isSolid == 1)
					solidGeometry = rs.getObject("SOLID_GEOMETRY");

				GeometryObject geometry = null;
				Object object = rs.getObject("GEOMETRY");
				if (!rs.wasNull() && object != null)
					geometry = resolverManager.getDatabaseAdapter().getGeometryConverter().getPolygon(object);

				// constructing a geometry node
				GeometryNode geomNode = new GeometryNode();
				geomNode.gmlId = gmlId;
				geomNode.isSolid = isSolid == 1;
				geomNode.isComposite = isComposite == 1;
				geomNode.isTriangulated = isTriangulated == 1;
				geomNode.isReverse = (isReverse == 1) ^ reverse;
				geomNode.geometry = geometry;
				geomNode.solidGeometry = solidGeometry;

				if (root != null)
					parentMap.get(parent_id).childNodes.add(geomNode);
				else
					root = geomNode;

				// make this node the parent for the next hierarchy level
				if (geomNode.geometry == null)
					parentMap.put(id, geomNode);			
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

	private void rebuildGeometry(GeometryNode geomNode, boolean reverse) {
		if (geomNode.geometry != null) {
			// reverse order of geometry instance if necessary
			if (reverse) {
				double[][] rings = new double[geomNode.geometry.getNumElements()][];

				for (int i = 0; i < rings.length; i++) {
					double[] origRing = geomNode.geometry.getCoordinates(i);
					double[] reversedRing = new double[origRing.length];
					for (int j = origRing.length - 3, ringIndex = 0; j >= 0; j -= 3) {
						reversedRing[ringIndex++] = origRing[j];
						reversedRing[ringIndex++] = origRing[j + 1];
						reversedRing[ringIndex++] = origRing[j + 2];
					}

					rings[i] = reversedRing;
				}

				geomNode.geometry = GeometryObject.createPolygon(rings, geomNode.geometry.getDimension(), geomNode.geometry.getSrid());
			}
		} else {
			for (GeometryNode childNode : geomNode.childNodes)
				rebuildGeometry(childNode, reverse);
		}
	}

	private class GeometryNode {
		private String gmlId;
		private boolean isSolid;
		private boolean isComposite;
		private boolean isTriangulated;
		private boolean isReverse;
		private GeometryObject geometry;
		private Object solidGeometry;
		private List<GeometryNode> childNodes = new ArrayList<GeometryNode>();
	}

	private String getKey(String fromTable, String fromColumn) {
		return fromTable + "_" + fromColumn;
	}

	private PreparedStatement getUpdateStatement(String fromTable, String fromColumn, String key) throws SQLException {
		PreparedStatement ps = psMap.get(key);
		if (ps == null) {
			ps = batchConn.prepareStatement("update " + schema + "." + fromTable + " set " + fromColumn + "=? where ID=?");
			psMap.put(key, ps);
			psBatchCounterMap.put(key, 0);
		}

		return ps;
	}

	@Override
	public void executeBatch() throws SQLException {
		psParentElem.executeBatch();
		psMemberElem.executeBatch();
		for (PreparedStatement ps : psMap.values())
			ps.executeBatch();		

		parentBatchCounter = 0;
		memberBatchCounter = 0;
		for (Entry<String, Integer> entry : psBatchCounterMap.entrySet())
			entry.setValue(0);

		executeUpdateSurfGeomBatch();
	}

	private void executeUpdateSurfGeomBatch() throws SQLException {
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
		psSelectTmpSurfGeom.close();
		psSelectSurfGeom.close();
		psUpdateSurfGeom.close();
		psParentElem.close();
		psMemberElem.close();
		for (PreparedStatement ps : psMap.values())
			ps.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.SURFACE_GEOMETRY;
	}
}
