/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.cache.CacheTable;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;
import de.tub.citydb.util.Util;

public class XlinkSurfaceGeometry implements DBXlinkResolver {
	private static final ReentrantLock mainLock = new ReentrantLock();

	private final Connection batchConn;
	private final CacheTable cacheTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectTmpSurfGeom;
	private PreparedStatement psSelectSurfGeom;
	private PreparedStatement psUpdateSurfGeom;
	private PreparedStatement psParentElem;
	private PreparedStatement psMemberElem;
	private HashMap<String, PreparedStatement> psMap;

	private HashMap<String, Integer> psBatchCounterMap;
	private int parentBatchCounter;
	private int memberBatchCounter;
	private int updateBatchCounter;

	public XlinkSurfaceGeometry(Connection batchConn, CacheTable cacheTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.cacheTable = cacheTable;
		this.resolverManager = resolverManager;

		psMap = new HashMap<String, PreparedStatement>();
		psBatchCounterMap = new HashMap<String, Integer>();

		init();
	}

	private void init() throws SQLException {
		psSelectTmpSurfGeom = cacheTable.getConnection().prepareStatement(new StringBuilder("select ID from ").append(cacheTable.getTableName()).append(" where PARENT_ID=?").toString());
		psSelectSurfGeom = batchConn.prepareStatement(resolverManager.getDatabaseAdapter().getSQLAdapter().getHierarchicalGeometryQuery());
		psUpdateSurfGeom = batchConn.prepareStatement("update SURFACE_GEOMETRY set IS_XLINK=1 where ID=?");

		psParentElem = batchConn.prepareStatement(new StringBuilder("insert into SURFACE_GEOMETRY (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, SOLID_GEOMETRY, CITYOBJECT_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?)").toString());
		psMemberElem = batchConn.prepareStatement(new StringBuilder("insert into SURFACE_GEOMETRY (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY, CITYOBJECT_ID) values ")
		.append("(").append(resolverManager.getDatabaseAdapter().getSQLAdapter().getNextSequenceValue(DBSequencerEnum.SURFACE_GEOMETRY_ID_SEQ))
		.append(", ?, ?, ?, 0, 0, 0, 1, ?, ?, ?)").toString());
	}

	public boolean insert(DBXlinkSurfaceGeometry xlink) throws SQLException {
		UIDCacheEntry rootGeometryEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.ABSTRACT_GML_GEOMETRY);
		if (rootGeometryEntry == null || rootGeometryEntry.getRootId() == -1)
			// do not return an error in case of implicit geometries since the
			// the implicit geometry might be a point or curve
			return xlink.getFromTable() == TableEnum.IMPLICIT_GEOMETRY;

		if (xlink.getFromTable() != TableEnum.IMPLICIT_GEOMETRY) {
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
				GeometryNode geomNode = read(rootGeometryEntry.getId(), reverse);
				if (geomNode == null)
					return false;

				long surfaceGeometryId = insert(geomNode, xlink.getId(), xlink.getRootId(), xlink.getCityObjectId());

				// if this is an xlink from a feature table, then we also let
				// the geometry column of this table point to the geometry object 
				if (xlink.getFromTable() != null && xlink.getFromTable() != TableEnum.UNDEFINED) {
					String key = getKey(xlink);
					PreparedStatement ps = getUpdateStatement(xlink, key);
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
			surfaceGeometryId = resolverManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_ID_SEQ);
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
			psParentElem.setObject(10, node.solidGeometry);

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
			HashMap<Integer, GeometryNode> parentMap = new HashMap<Integer, GeometryNode>();

			// rebuild geometry hierarchy
			while (rs.next()) {
				String gmlId = rs.getString("GMLID");
				int isSolid = rs.getInt("IS_SOLID");
				int isComposite = rs.getInt("IS_COMPOSITE");
				int isTriangulated = rs.getInt("IS_TRIANGULATED");
				int isReverse = rs.getInt("IS_REVERSE");
				int level = rs.getInt("LEVEL");

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

	private String getKey(DBXlinkSurfaceGeometry xlink) {
		return xlink.getFromTable().ordinal() + "_" + xlink.getFromTableAttributeName();
	}

	private PreparedStatement getUpdateStatement(DBXlinkSurfaceGeometry xlink, String key) throws SQLException {
		PreparedStatement ps = psMap.get(key);
		if (ps == null) {
			ps = batchConn.prepareStatement("update " + xlink.getFromTable() + " set " + xlink.getFromTableAttributeName() + "=? where ID=?");
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
