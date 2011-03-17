/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.cache.HeapCacheTable;
import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.importer.DBSequencerEnum;
import de.tub.citydb.db.xlink.DBXlinkSurfaceGeometry;
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
		psSelectSurfGeom = batchConn.prepareStatement("select * from SURFACE_GEOMETRY where ROOT_ID=?");
		psUpdateSurfGeom = batchConn.prepareStatement("update SURFACE_GEOMETRY set IS_XLINK=1 where ID=?");

		psParentElem = batchConn.prepareStatement("insert into SURFACE_GEOMETRY (ID, GMLID, GMLID_CODESPACE, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY) values "
				+ "(?, ?, " + gmlIdCodespace + ", ?, ?, ?, ?, ?, 1, ?, ?)");
		psMemberElem = batchConn.prepareStatement("insert into SURFACE_GEOMETRY (ID, GMLID, GMLID_CODESPACE, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY) values "
				+ "(SURFACE_GEOMETRY_SEQ.nextval, ?, " + gmlIdCodespace + ", ?, ?, 0, 0, 0, 1, ?, ?)");

	}

	public boolean insert(DBXlinkSurfaceGeometry xlink) throws SQLException {
		GmlIdEntry rootGeometryEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.GMLGEOMETRY);
		if (rootGeometryEntry == null || rootGeometryEntry.getRootId() == -1)
			return false;

		ResultSet rs = null;

		try {	
			// check whether we deal with a local gml:id
			// remote gml:ids are not supported so far...
			String gmlId = rootGeometryEntry.getMapping();
			if (Util.isRemoteXlink(gmlId))
				return false;
			
			boolean reverse = rootGeometryEntry.isReverse() ^ xlink.isReverse();			
			GeometryTree geomTree = read(rootGeometryEntry.getRootId(), reverse);
			if (geomTree == null)
				return false;
	
			gmlId = gmlId.replaceAll("^#", "");
			GeometryNode xlinkNode = geomTree.getNode(gmlId);
			
			// check whether this geometry is referenced by another xlink
			psSelectTmpSurfGeom.setLong(1, xlinkNode.id);
			rs = psSelectTmpSurfGeom.executeQuery();

			if (rs.next()) {
				// we need to re-work on this
				resolverManager.propagateXlink(xlink);
				return true;
			}

			// we cannot go on if we do not know the geometry type
			if (xlinkNode.type == GMLClass.UNDEFINED)
				return false;
			
			insert(xlinkNode, xlink.getId(), xlink.getRootId());
			psUpdateSurfGeom.setLong(1, xlinkNode.id);
			psUpdateSurfGeom.addBatch();
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
			STRUCT obj = SyncJGeometry.syncStore(node.geometry, batchConn);

			psMemberElem.setString(1, gmlId);
			psMemberElem.setLong(2, parentId);
			psMemberElem.setLong(3, rootId);
			psMemberElem.setInt(4, node.isReverse ? 1 : 0);
			psMemberElem.setObject(5, obj);

			psMemberElem.addBatch();

		} else if (node.type != GMLClass.POLYGON) {
			// set root entry
			long isSolid = node.isSolid ? 1 : 0;
			long isComposite = node.isComposite ? 1 : 0;
			long isTriangulated = node.isTriangulated ? 1 : 0;

			long surfaceGeometryId = resolverManager.getDBId(DBSequencerEnum.SURFACE_GEOMETRY_SEQ);

			psParentElem.setLong(1, surfaceGeometryId);
			psParentElem.setString(2, gmlId);
			psParentElem.setLong(3, parentId);
			psParentElem.setLong(4, rootId);
			psParentElem.setLong(5, isSolid);
			psParentElem.setLong(6, isComposite);
			psParentElem.setLong(7, isTriangulated);
			psParentElem.setInt(8, node.isReverse ? 1 : 0);
			psParentElem.setNull(9, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

			psParentElem.addBatch();

			parentId = surfaceGeometryId;

			for (GeometryNode childNode : node.childNodes)
				if (childNode.type != GMLClass.UNDEFINED)
					insert(childNode, parentId, rootId);
		}
	}

	private GeometryTree read(long rootId, boolean reverse) throws SQLException {
		ResultSet rs = null;

		psSelectSurfGeom.setLong(1, rootId);
		rs = psSelectSurfGeom.executeQuery();
		GeometryTree geomTree = new GeometryTree();

		// firstly, read the geometry entries into a
		// flat geometry tree structure
		while (rs.next()) {
			long id = rs.getLong("ID");
			String gmlId = rs.getString("GMLID");
			long parentId = rs.getLong("PARENT_ID");
			int isSolid = rs.getInt("IS_SOLID");
			int isComposite = rs.getInt("IS_COMPOSITE");
			int isTriangulated = rs.getInt("IS_TRIANGULATED");
			int isReverse = rs.getInt("IS_REVERSE");

			if (id == rootId)
				parentId = 0;

			JGeometry geometry = null;
			STRUCT struct = (STRUCT)rs.getObject("GEOMETRY");
			if (!rs.wasNull() && struct != null)
				geometry = JGeometry.load(struct);

			// constructing a geometry node
			GeometryNode geomNode = new GeometryNode();
			geomNode.id = id;
			geomNode.gmlId = gmlId;
			geomNode.type = GMLClass.UNDEFINED;
			geomNode.parentId = parentId;
			geomNode.isSolid = isSolid == 1;
			geomNode.isComposite = isComposite == 1;
			geomNode.isTriangulated = isTriangulated == 1;
			geomNode.isReverse = (isReverse == 1) ^ reverse;
			geomNode.geometry = geometry;

			// put it into our geometry tree
			geomTree.insertNode(geomNode, parentId);
		}

		rs.close();

		// interpret geometry tree
		if (geomTree.root != 0) {
			rebuildGeometry(geomTree.getNode(geomTree.root), reverse);
			return geomTree;
		} else {
			return null;
		}
	}

	private void rebuildGeometry(GeometryNode geomNode, boolean reverse) {
		if (geomNode.geometry != null) {
			geomNode.type = GMLClass.POLYGON;

			// reverse order of geometry instance if necessary
			if (reverse) {
				int[] elemInfoArray = geomNode.geometry.getElemInfo();
				double[] ordinatesArray = geomNode.geometry.getOrdinatesArray();

				if (elemInfoArray.length < 3 || ordinatesArray.length == 0) {
					geomNode.geometry = null;
					return;
				}

				// we are pragmatic here. if elemInfoArray contains more than one entry,
				// we suppose we have one outer ring and anything else are inner rings.
				List<Integer> ringLimits = new ArrayList<Integer>();
				for (int i = 3; i < elemInfoArray.length; i += 3)
					ringLimits.add(elemInfoArray[i] - 1);

				ringLimits.add(ordinatesArray.length);

				// ok, reverse polygon according to this info
				Object[] pointArray = new Object[ringLimits.size()];
				int ringElem = 0;
				int arrayIndex = 0;
				for (Integer ringLimit : ringLimits) {
					double[] coords = new double[ringLimit];

					for (int i = 0, j = ringLimit - 3; j >= ringElem; j -= 3, i += 3) {
						coords[i] = ordinatesArray[j];
						coords[i + 1] = ordinatesArray[j + 1];
						coords[i + 2] = ordinatesArray[j + 2];
					}

					pointArray[arrayIndex++] = coords;
					ringElem = ringLimit;
				}

				JGeometry geom = JGeometry.createLinearPolygon(pointArray, 
						geomNode.geometry.getDimensions(), 
						geomNode.geometry.getSRID());

				geomNode.geometry = geom;
			}

		} else if (!geomNode.isTriangulated) {
			if (!geomNode.isSolid && geomNode.isComposite) {
				// compositeSurface...
				geomNode.type = GMLClass.COMPOSITESURFACE;

				for (GeometryNode childNode : geomNode.childNodes)
					rebuildGeometry(childNode, reverse);

				return;
			}

			else if (geomNode.isSolid && geomNode.isComposite) {
				// compositeSolid
				geomNode.type = GMLClass.COMPOSITESOLID;

				for (GeometryNode childNode : geomNode.childNodes)
					rebuildGeometry(childNode, reverse);

				return;
			}

			else if (geomNode.isSolid && !geomNode.isComposite) {
				// a simple solid
				geomNode.type = GMLClass.SOLID;

				if (geomNode.childNodes.size() == 1)
					rebuildGeometry(geomNode.childNodes.firstElement(), reverse);

				return;
			}

			else if (!geomNode.isSolid && !geomNode.isComposite) {
				// differ between multiSurface and multiSolid
				if (!geomNode.childNodes.isEmpty()) {
					geomNode.type = GMLClass.MULTISOLID;

					// we have a multiSolid, if all childNodes are solids!
					for (GeometryNode childNode : geomNode.childNodes) {
						if (!childNode.isSolid && geomNode.type != GMLClass.MULTISURFACE) 
							geomNode.type = GMLClass.MULTISURFACE;

						rebuildGeometry(childNode, reverse);
					}
				}

				return;
			}

		} else if (geomNode.isTriangulated) {
			// triangulatedSurface...
			if (geomNode.childNodes == null || geomNode.childNodes.size() == 0)
				return;

			geomNode.type = GMLClass.TRIANGULATEDSURFACE;

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
		protected long parentId;
		protected boolean isSolid;
		protected boolean isComposite;
		protected boolean isTriangulated;
		protected boolean isReverse;
		protected JGeometry geometry;
		protected Vector<GeometryNode> childNodes;

		public GeometryNode() {
			childNodes = new Vector<GeometryNode>();
		}
	}

	private class GeometryTree {
		long root;
		private HashMap<Long, GeometryNode> geometryTree;

		public GeometryTree() {
			geometryTree = new HashMap<Long, GeometryNode>();
		}

		public void insertNode(GeometryNode geomNode, long parentId) {

			if (parentId == 0)
				root = geomNode.id;

			if (geometryTree.containsKey(geomNode.id)) {

				// we have inserted a pseudo node previously
				// so fill that one with life...
				GeometryNode pseudoNode = geometryTree.get(geomNode.id);
				pseudoNode.id = geomNode.id;
				pseudoNode.gmlId = geomNode.gmlId;
				pseudoNode.type = geomNode.type;
				pseudoNode.parentId = geomNode.parentId;
				pseudoNode.isSolid = geomNode.isSolid;
				pseudoNode.isComposite = geomNode.isComposite;
				pseudoNode.isTriangulated = geomNode.isTriangulated;
				pseudoNode.isReverse = geomNode.isReverse;
				pseudoNode.geometry = geomNode.geometry;

				geomNode = pseudoNode;

			} else {
				// identify hierarchy nodes and place them
				// into the tree
				if (geomNode.geometry == null || parentId == 0)
					geometryTree.put(geomNode.id, geomNode);
			}

			// make the node known to its parent...
			if (parentId != 0) {
				GeometryNode parentNode = geometryTree.get(parentId);

				if (parentNode == null) {
					// there is no entry so far, so lets create a
					// pseude node
					parentNode = new GeometryNode();
					geometryTree.put(parentId, parentNode);
				}

				parentNode.childNodes.add(geomNode);
			}
		}

		public GeometryNode getNode(long entryId) {
			Iterator<Entry<Long, GeometryNode>> iter = geometryTree.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Long, GeometryNode> mapEntry = iter.next();

				if (mapEntry.getKey() == entryId)
					return mapEntry.getValue();

				if (mapEntry.getValue().childNodes.size() != 0) {
					for (GeometryNode node : mapEntry.getValue().childNodes)
						if (node.id == entryId)
							return node;
				}
			}

			return null;
		}

		public GeometryNode getNode(String gmlId) {
			Iterator<Entry<Long, GeometryNode>> iter = geometryTree.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Long, GeometryNode> mapEntry = iter.next();

				if (mapEntry.getValue().gmlId != null && mapEntry.getValue().gmlId.equals(gmlId))
					return mapEntry.getValue();

				if (mapEntry.getValue().childNodes.size() != 0) {
					for (GeometryNode node : mapEntry.getValue().childNodes)
						if (node.gmlId != null && node.gmlId.equals(gmlId))
							return node;
				}
			}

			return null;
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		psParentElem.executeBatch();
		psMemberElem.executeBatch();
		
		final ReentrantLock lock = mainLock;
		lock.lock();
		try {
			psUpdateSurfGeom.executeBatch();
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
