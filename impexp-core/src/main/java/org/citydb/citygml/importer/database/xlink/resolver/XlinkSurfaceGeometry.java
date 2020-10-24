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
import java.util.Map;

public class XlinkSurfaceGeometry implements DBXlinkResolver {
	private final Connection connection;
	private final DBXlinkResolverManager manager;

	private final PreparedStatement psSelectTmpSurfGeom;
	private final PreparedStatement psSelectSurfGeom;
	private final PreparedStatement psUpdateSurfGeom;
	private final PreparedStatement psParentElem;
	private final PreparedStatement psMemberElem;
	private final Map<String, PreparedStatement> updates;
	private final Map<String, Integer> counters;
	private final String schema;

	private int parentBatchCounter;
	private int memberBatchCounter;
	private int updateBatchCounter;
	
	public XlinkSurfaceGeometry(Connection connection, CacheTable cacheTable, DBXlinkResolverManager manager) throws SQLException {
		this.connection = connection;
		this.manager = manager;

		updates = new HashMap<>();
		counters = new HashMap<>();
		schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();

		psSelectTmpSurfGeom = cacheTable.getConnection().prepareStatement("select ID from " + cacheTable.getTableName() +
				" where PARENT_ID=? or ROOT_ID=?");

		psSelectSurfGeom = connection.prepareStatement(manager.getDatabaseAdapter().getSQLAdapter().getHierarchicalGeometryQuery());
		psUpdateSurfGeom = connection.prepareStatement("update " + schema + ".SURFACE_GEOMETRY set IS_XLINK=1 where ID=?");
		
		StringBuilder parentStmt = new StringBuilder()
		.append("insert into ").append(schema).append(".SURFACE_GEOMETRY (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, SOLID_GEOMETRY, CITYOBJECT_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ");
		
		if (manager.getDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS) {
			// the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
			// thus, we have to use the database function ST_GeomFromEWKT to insert such geometries
			// TODO: rework as soon as the JDBC driver supports PolyhedralSurface
			parentStmt.append("ST_GeomFromEWKT(?), ");	
		} else
			parentStmt.append("?, ");
		
		parentStmt.append("?)");
		psParentElem = connection.prepareStatement(parentStmt.toString());

		psMemberElem = connection.prepareStatement("insert into " + schema + ".SURFACE_GEOMETRY (ID, GMLID, PARENT_ID, " +
				"ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, CITYOBJECT_ID) " +
				"values (" + manager.getDatabaseAdapter().getSQLAdapter().getNextSequenceValue(SequenceEnum.SURFACE_GEOMETRY_ID_SEQ.getName()) +
				", ?, ?, ?, 0, 0, 0, 1, ?, ?, ?)");
	}

	public boolean insert(DBXlinkSurfaceGeometry xlink) throws SQLException {
		UIDCacheEntry rootGeometryEntry = manager.getGeometryId(xlink.getGmlId());
		if (rootGeometryEntry == null || rootGeometryEntry.getRootId() == -1) {
			// do not return an error in case of implicit geometries since the
			// the implicit geometry might be a point or curve
			return MappingConstants.IMPLICIT_GEOMETRY_TABLE.equalsIgnoreCase(xlink.getTable());
		}

		if (!MappingConstants.IMPLICIT_GEOMETRY_TABLE.equalsIgnoreCase(xlink.getTable())) {
			// check whether we deal with a local gml:id
			// remote gml:ids are not supported so far...
			String gmlId = rootGeometryEntry.getMapping();
			if (Util.isRemoteXlink(gmlId))
				return false;

			// check whether this geometry is referenced by another xlink
			psSelectTmpSurfGeom.setLong(1, rootGeometryEntry.getId());
			psSelectTmpSurfGeom.setLong(2, rootGeometryEntry.getId());

			try (ResultSet rs = psSelectTmpSurfGeom.executeQuery()) {
				if (rs.next()) {
					// we need to re-work on this
					manager.propagateXlink(xlink);
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
						if (counters.merge(key, 1, Integer::sum) == manager.getDatabaseAdapter().getMaxBatchSize()) {
							manager.executeBatchWithLock(ps, this);
							counters.put(key, 0);
						}
					}
				}
			}
		}

		psUpdateSurfGeom.setLong(1, rootGeometryEntry.getId());
		psUpdateSurfGeom.addBatch();
		if (++updateBatchCounter == manager.getDatabaseAdapter().getMaxBatchSize()) {
			manager.executeBatchWithLock(psUpdateSurfGeom, this);
			updateBatchCounter = 0;
		}

		return true;
	}

	private long insert(GeometryNode node, long parentId, long rootId, long cityObjectId) throws SQLException {
		String gmlId = node.gmlId;
		long surfaceGeometryId = 0;

		if (node.geometry != null) {
			Object obj = manager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(node.geometry, connection);

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
			if (++memberBatchCounter == manager.getDatabaseAdapter().getMaxBatchSize()) {
				manager.executeBatchWithLock(psParentElem, this);
				manager.executeBatchWithLock(psMemberElem, this);
				parentBatchCounter = 0;
				memberBatchCounter = 0;
			}
		} else {
			// set root entry
			long isSolid = node.isSolid ? 1 : 0;
			long isComposite = node.isComposite ? 1 : 0;
			long isTriangulated = node.isTriangulated ? 1 : 0;
			surfaceGeometryId = manager.getDBId(SequenceEnum.SURFACE_GEOMETRY_ID_SEQ.getName());
			if (rootId == 0)
				rootId = surfaceGeometryId;

			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(4, rootId);
			psParentElem.setLong(5, isSolid);
			psParentElem.setLong(6, isComposite);
			psParentElem.setLong(7, isTriangulated);
			psParentElem.setInt(8, node.isReverse ? 1 : 0);
			psParentElem.setNull(9, manager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					manager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (node.solidGeometry != null)
				psParentElem.setObject(10, node.solidGeometry);
			else
				psParentElem.setNull(10, manager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
						manager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (parentId != 0)
				psParentElem.setLong(3, parentId);
			else
				psParentElem.setNull(3, Types.NULL);			

			if (cityObjectId != 0) 
				psParentElem.setLong(11, cityObjectId);
			else
				psParentElem.setNull(11, Types.NULL);

			psParentElem.addBatch();
			if (++parentBatchCounter == manager.getDatabaseAdapter().getMaxBatchSize()) {
				manager.executeBatchWithLock(psParentElem, this);
				parentBatchCounter = 0;
			}

			for (GeometryNode childNode : node.childNodes)
				insert(childNode, surfaceGeometryId, rootId, cityObjectId);
		}

		return surfaceGeometryId;
	}

	private GeometryNode read(long rootId, boolean reverse) throws SQLException {
		psSelectSurfGeom.setLong(1, rootId);

		try (ResultSet rs = psSelectSurfGeom.executeQuery()) {
			GeometryNode root = null;
			HashMap<Long, GeometryNode> parentMap = new HashMap<>();

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
					geometry = manager.getDatabaseAdapter().getGeometryConverter().getPolygon(object);

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

	private static class GeometryNode {
		private String gmlId;
		private boolean isSolid;
		private boolean isComposite;
		private boolean isTriangulated;
		private boolean isReverse;
		private GeometryObject geometry;
		private Object solidGeometry;
		private final List<GeometryNode> childNodes = new ArrayList<>();
	}

	private String getKey(String fromTable, String fromColumn) {
		return fromTable + "_" + fromColumn;
	}

	private PreparedStatement getUpdateStatement(String fromTable, String fromColumn, String key) throws SQLException {
		PreparedStatement ps = updates.get(key);
		if (ps == null) {
			ps = connection.prepareStatement("update " + schema + "." + fromTable + " set " + fromColumn + "=? where ID=?");
			updates.put(key, ps);
		}

		return ps;
	}

	@Override
	public void executeBatch() throws SQLException {
		psParentElem.executeBatch();
		psMemberElem.executeBatch();
		psUpdateSurfGeom.executeBatch();
		for (PreparedStatement ps : updates.values())
			ps.executeBatch();

		updateBatchCounter = 0;
		parentBatchCounter = 0;
		memberBatchCounter = 0;
		counters.replaceAll((k, v) -> v = 0);
	}

	@Override
	public void close() throws SQLException {
		psSelectTmpSurfGeom.close();
		psSelectSurfGeom.close();
		psUpdateSurfGeom.close();
		psParentElem.close();
		psMemberElem.close();
		for (PreparedStatement ps : updates.values())
			ps.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.SURFACE_GEOMETRY;
	}
}
