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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.gml.geometry.aggregates.MultiSolidImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSurfaceImpl;
import org.citygml4j.impl.gml.geometry.complexes.CompositeSolidImpl;
import org.citygml4j.impl.gml.geometry.complexes.CompositeSurfaceImpl;
import org.citygml4j.impl.gml.geometry.primitives.DirectPositionListImpl;
import org.citygml4j.impl.gml.geometry.primitives.ExteriorImpl;
import org.citygml4j.impl.gml.geometry.primitives.InteriorImpl;
import org.citygml4j.impl.gml.geometry.primitives.LinearRingImpl;
import org.citygml4j.impl.gml.geometry.primitives.OrientableSurfaceImpl;
import org.citygml4j.impl.gml.geometry.primitives.PolygonImpl;
import org.citygml4j.impl.gml.geometry.primitives.SolidImpl;
import org.citygml4j.impl.gml.geometry.primitives.SolidPropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.SurfacePropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.TriangleImpl;
import org.citygml4j.impl.gml.geometry.primitives.TrianglePatchArrayPropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.TriangulatedSurfaceImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;

public class DBSurfaceGeometry implements DBExporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection connection;
	private final TemporaryCacheTable tempTable;
	private final DBExporterManager dbExporterManager;
	private final Config config;

	private PreparedStatement psSurfaceGeometry;
	private PreparedStatement psImportGmlId;

	private boolean exportAppearance;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean transformCoords;
	private String gmlIdPrefix;

	private int commitAfter = 1000;
	private int batchCounter;

	public DBSurfaceGeometry(Connection connection, TemporaryCacheTable tempTable, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.tempTable = tempTable;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		exportAppearance = config.getInternal().isExportGlobalAppearances();

		if (exportAppearance) {
			Integer commitAfterProp = config.getProject().getDatabase().getUpdateBatching().getTempBatchValue();
			if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= Internal.ORACLE_MAX_BATCH_SIZE)
				commitAfter = commitAfterProp;

			psImportGmlId = tempTable.getConnection().prepareStatement(
					"insert into " + tempTable.getTableName() + 
					" select ?, ? from dual " +
					" where exists (select SURFACE_GEOMETRY_ID from TEXTUREPARAM where SURFACE_GEOMETRY_ID = ?)"
			);
		}

		useXLink = config.getProject().getExporter().getXlink().getGeometry().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getGeometry().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getGeometry().getIdPrefix();
		}	

		transformCoords = config.getInternal().isTransformCoordinates();
		if (!transformCoords) {
			psSurfaceGeometry = connection.prepareStatement("select * from SURFACE_GEOMETRY where ROOT_ID = ?");
		} else {	
			int srid = config.getInternal().getExportTargetSRS().getSrid();

			psSurfaceGeometry = connection.prepareStatement("select ID, GMLID, PARENT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, " +
					"geodb_util.transform_or_null(GEOMETRY, " + srid + ") AS GEOMETRY " +
			"from SURFACE_GEOMETRY where ROOT_ID = ?");
		}
	}

	public DBSurfaceGeometryResult read(long rootId) throws SQLException {
		ResultSet rs = null;

		try {
			psSurfaceGeometry.setLong(1, rootId);
			rs = psSurfaceGeometry.executeQuery();
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
				int isXlink = rs.getInt("IS_XLINK");
				int isReverse = rs.getInt("IS_REVERSE");

				JGeometry geometry = null;
				STRUCT struct = (STRUCT)rs.getObject("GEOMETRY");
				if (!rs.wasNull() && struct != null)
					geometry = JGeometry.load(struct);

				// constructing a geometry node
				GeometryNode geomNode = new GeometryNode();
				geomNode.id = id;
				geomNode.gmlId = gmlId;
				geomNode.parentId = parentId;
				geomNode.isSolid = isSolid == 1;
				geomNode.isComposite = isComposite == 1;
				geomNode.isTriangulated = isTriangulated == 1;			
				geomNode.isXlink = isXlink == 1;			
				geomNode.isReverse = isReverse == 1;

				geomNode.geometry = geometry;

				// put it into our geometry tree
				geomTree.insertNode(geomNode, parentId);
			}

			// interpret geometry tree as a single abstractGeometry
			if (geomTree.root != 0)
				return rebuildGeometry(geomTree.getNode(geomTree.root), false);
			else {
				LOG.error("Failed to interpret geometry object.");
				return null;
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	private DBSurfaceGeometryResult rebuildGeometry(GeometryNode geomNode, boolean isSetOrientableSurface) throws SQLException {
		// try and determine the geometry type
		GMLClass surfaceGeometryType = null;
		if (geomNode.geometry != null) {
			surfaceGeometryType = GMLClass.POLYGON;
		} else {

			if (geomNode.childNodes == null || geomNode.childNodes.size() == 0)
				return null;

			if (!geomNode.isTriangulated) {
				if (!geomNode.isSolid && geomNode.isComposite)
					surfaceGeometryType = GMLClass.COMPOSITE_SURFACE;
				else if (geomNode.isSolid && !geomNode.isComposite)
					surfaceGeometryType = GMLClass.SOLID;
				else if (geomNode.isSolid && geomNode.isComposite)
					surfaceGeometryType = GMLClass.COMPOSITE_SOLID;
				else if (!geomNode.isSolid && !geomNode.isComposite) {
					boolean isMultiSolid = true;
					for (GeometryNode childNode : geomNode.childNodes) {
						if (!childNode.isSolid){
							isMultiSolid = false;
							break;
						}
					}

					if (isMultiSolid) 
						surfaceGeometryType = GMLClass.MULTI_SOLID;
					else
						surfaceGeometryType = GMLClass.MULTI_SURFACE;
				}
			} else
				surfaceGeometryType = GMLClass.TRIANGULATED_SURFACE;
		}

		// return if we cannot identify the geometry
		if (surfaceGeometryType == null)
			return null;

		dbExporterManager.updateGeometryCounter(surfaceGeometryType);

		// check for xlinks
		if (geomNode.gmlId != null) {
			if (geomNode.isXlink) {
				if (dbExporterManager.lookupAndPutGmlId(geomNode.gmlId, geomNode.id, CityGMLClass.ABSTRACT_GML_GEOMETRY)) {

					if (useXLink) {
						if (exportAppearance)
							writeToAppearanceCache(geomNode);
						
						// check whether we have to embrace the geometry with an orientableSurface
						if (geomNode.isReverse != isSetOrientableSurface) {
							OrientableSurface orientableSurface = new OrientableSurfaceImpl();				
							SurfaceProperty surfaceProperty = new SurfacePropertyImpl();
							surfaceProperty.setHref("#" + geomNode.gmlId); 
							orientableSurface.setBaseSurface(surfaceProperty);
							orientableSurface.setOrientation("-");
							dbExporterManager.updateGeometryCounter(GMLClass.ORIENTABLE_SURFACE);

							return new DBSurfaceGeometryResult(orientableSurface);
						} else
							return new DBSurfaceGeometryResult("#" + geomNode.gmlId, surfaceGeometryType);
					} else {
						geomNode.isXlink = false;
						String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
						if (appendOldGmlId)
							newGmlId += '-' + geomNode.gmlId;

						geomNode.gmlId = newGmlId;
						return rebuildGeometry(geomNode, isSetOrientableSurface);
					}

				}
			} else if (exportAppearance)
				writeToAppearanceCache(geomNode);
		}

		// check whether we have to initialize an orientableSurface
		boolean initOrientableSurface = false;
		if (geomNode.isReverse && !isSetOrientableSurface) {
			isSetOrientableSurface = true;
			initOrientableSurface = true;
		}

		// deal with geometry according to the identified type
		// Polygon
		if (surfaceGeometryType == GMLClass.POLYGON) {
			// try and interpret JGeometry
			Polygon polygon = new PolygonImpl();
			boolean forceRingIds = false;

			if (geomNode.gmlId != null) {
				polygon.setId(geomNode.gmlId);
				forceRingIds = true;
			}

			int[] elemInfoArray = geomNode.geometry.getElemInfo();
			double[] ordinatesArray = geomNode.geometry.getOrdinatesArray();

			if (elemInfoArray.length < 3 || ordinatesArray.length == 0)
				return null;

			// we are pragmatic here. if elemInfoArray contains more than one entry,
			// we suppose we have one outer ring and anything else are inner rings.
			List<Integer> ringLimits = new ArrayList<Integer>();
			for (int i = 3; i < elemInfoArray.length; i += 3)
				ringLimits.add(elemInfoArray[i] - 1);

			ringLimits.add(ordinatesArray.length);

			// ok, rebuild surface according to this info
			boolean isExterior = elemInfoArray[1] == 1003;
			int ringElem = 0;
			int ringNo = 0;
			for (Integer ringLimit : ringLimits) {
				List<Double> values = new ArrayList<Double>();

				// check whether we have to reverse the coordinate order
				if (!geomNode.isReverse) { 
					for ( ; ringElem < ringLimit; ringElem++)
						values.add(ordinatesArray[ringElem]);
				} else {
					for (int i = ringLimit - 3; i >= ringElem; i -= 3) {
						values.add(ordinatesArray[i]);
						values.add(ordinatesArray[i + 1]);
						values.add(ordinatesArray[i + 2]);
					}
				}

				if (isExterior) {
					LinearRing linearRing = new LinearRingImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					if (forceRingIds)
						linearRing.setId(polygon.getId() + '_' + ringNo + '_');

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(3);
					linearRing.setPosList(directPositionList);

					Exterior exterior = new ExteriorImpl();
					exterior.setRing(linearRing);
					polygon.setExterior(exterior);

					isExterior = false;
					dbExporterManager.updateGeometryCounter(GMLClass.LINEAR_RING);
				} else {
					LinearRing linearRing = new LinearRingImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					if (forceRingIds)
						linearRing.setId(polygon.getId() + '_' + ringNo + '_');

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(3);
					linearRing.setPosList(directPositionList);

					Interior interior = new InteriorImpl();
					interior.setRing(linearRing);
					polygon.addInterior(interior);

					dbExporterManager.updateGeometryCounter(GMLClass.LINEAR_RING);
				}

				ringElem = ringLimit;
				ringNo++;
			}

			// check whether we have to embrace the polygon with an orientableSurface
			if (initOrientableSurface || (isSetOrientableSurface && !geomNode.isReverse)) {
				OrientableSurface orientableSurface = new OrientableSurfaceImpl();				
				SurfaceProperty surfaceProperty = new SurfacePropertyImpl();
				surfaceProperty.setSurface(polygon);
				orientableSurface.setBaseSurface(surfaceProperty);
				orientableSurface.setOrientation("-");
				dbExporterManager.updateGeometryCounter(GMLClass.ORIENTABLE_SURFACE);

				return new DBSurfaceGeometryResult(orientableSurface);
			} else
				return new DBSurfaceGeometryResult(polygon);
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SURFACE) {
			CompositeSurface compositeSurface = new CompositeSurfaceImpl();

			if (geomNode.gmlId != null)
				compositeSurface.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SurfaceProperty surfaceMember = new SurfacePropertyImpl();

					if (absGeom != null) {
						switch (geomMember.getType()) {
						case POLYGON:
						case ORIENTABLE_SURFACE:
						case COMPOSITE_SURFACE:
						case TRIANGULATED_SURFACE:
							surfaceMember.setSurface((AbstractSurface)absGeom);
							break;						
						default:
							surfaceMember = null;
						}
					} else {
						surfaceMember.setHref(geomMember.getTarget());
					}

					if (surfaceMember != null)
						compositeSurface.addSurfaceMember(surfaceMember);
				}
			}

			if (compositeSurface.isSetSurfaceMember()) {
				// check whether we have to embrace the compositeSurface with an orientableSurface
				if (initOrientableSurface || (isSetOrientableSurface && !geomNode.isReverse)) {
					OrientableSurface orientableSurface = new OrientableSurfaceImpl();				
					SurfaceProperty surfaceProperty = new SurfacePropertyImpl();
					surfaceProperty.setSurface(compositeSurface);
					orientableSurface.setBaseSurface(surfaceProperty);
					orientableSurface.setOrientation("-");
					dbExporterManager.updateGeometryCounter(GMLClass.ORIENTABLE_SURFACE);

					return new DBSurfaceGeometryResult(orientableSurface);
				} else
					return new DBSurfaceGeometryResult(compositeSurface);
			}					

			return null;
		}

		// compositeSolid
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SOLID) {
			CompositeSolid compositeSolid = new CompositeSolidImpl();

			if (geomNode.gmlId != null)
				compositeSolid.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SolidProperty solidMember = new SolidPropertyImpl();

					if (absGeom != null) {					
						switch (geomMember.getType()) {
						case SOLID:
						case COMPOSITE_SOLID:
							solidMember.setSolid((AbstractSolid)absGeom);
							break;
						default:
							solidMember = null;
						}
					} else {
						solidMember.setHref(geomMember.getTarget());
					}

					if (solidMember != null)
						compositeSolid.addSolidMember(solidMember);
				}
			}

			if (compositeSolid.isSetSolidMember())
				return new DBSurfaceGeometryResult(compositeSolid);

			return null;
		}

		// a simple solid
		else if (surfaceGeometryType == GMLClass.SOLID) {
			Solid solid = new SolidImpl();

			if (geomNode.gmlId != null)
				solid.setId(geomNode.gmlId);

			// we strongly assume solids contain one single CompositeSurface
			// as exterior. Nothing else is interpreted here...
			if (geomNode.childNodes.size() == 1) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(geomNode.childNodes.get(0), isSetOrientableSurface);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SurfaceProperty surfaceProperty = new SurfacePropertyImpl();

					if (absGeom != null) {
						switch (geomMember.getType()) {
						case COMPOSITE_SURFACE:
						case ORIENTABLE_SURFACE:
							surfaceProperty.setSurface((AbstractSurface)absGeom);
							break;
						default:
							surfaceProperty = null;
						}
					} else {
						surfaceProperty.setHref(geomMember.getTarget());
					}

					if (surfaceProperty != null)
						solid.setExterior(surfaceProperty);
				}
			}

			if (solid.isSetExterior())
				return new DBSurfaceGeometryResult(solid);

			return null;
		}

		// multiSolid
		else if (surfaceGeometryType == GMLClass.MULTI_SOLID) {
			MultiSolid multiSolid = new MultiSolidImpl();

			if (geomNode.gmlId != null)
				multiSolid.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SolidProperty solidMember = new SolidPropertyImpl();

					if (absGeom != null) {
						switch (geomMember.getType()) {
						case SOLID:
						case COMPOSITE_SOLID:
							solidMember.setSolid((AbstractSolid)absGeom);
							break;
						default:
							solidMember = null;
						}
					} else {
						solidMember.setHref(geomMember.getTarget());
					}

					if (solidMember != null)
						multiSolid.addSolidMember(solidMember);
				}
			}

			if (multiSolid.isSetSolidMember())
				return new DBSurfaceGeometryResult(multiSolid);

			return null;

		}

		// multiSurface
		else if (surfaceGeometryType == GMLClass.MULTI_SURFACE){
			MultiSurface multiSurface = new MultiSurfaceImpl();

			if (geomNode.gmlId != null)
				multiSurface.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SurfaceProperty surfaceMember = new SurfacePropertyImpl();

					if (absGeom != null) {
						switch (geomMember.getType()) {
						case POLYGON:
						case ORIENTABLE_SURFACE:
						case COMPOSITE_SURFACE:
						case TRIANGULATED_SURFACE:
							surfaceMember.setSurface((AbstractSurface)absGeom);
							break;
						default:
							surfaceMember = null;
						}
					} else {
						surfaceMember.setHref(geomMember.getTarget());
					}

					if (surfaceMember != null)
						multiSurface.addSurfaceMember(surfaceMember);
				}
			}

			if (multiSurface.isSetSurfaceMember())
				return new DBSurfaceGeometryResult(multiSurface);

			return null;
		}

		// triangulatedSurface
		else if (surfaceGeometryType == GMLClass.TRIANGULATED_SURFACE) {
			TriangulatedSurface triangulatedSurface = new TriangulatedSurfaceImpl();

			if (geomNode.gmlId != null)
				triangulatedSurface.setId(geomNode.gmlId);

			TrianglePatchArrayProperty triangleArray = new TrianglePatchArrayPropertyImpl();
			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface);

				if (geomMember != null) {
					// we are only expecting polygons...
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();					

					switch (geomMember.getType()) {
					case POLYGON:
						// we do not have to deal with xlinks here...
						if (absGeom != null) {
							// convert polygon to trianglePatch
							Triangle triangle = new TriangleImpl();
							Polygon polygon = (Polygon)absGeom;

							if (polygon.isSetExterior()) {								
								triangle.setExterior(polygon.getExterior());
								triangleArray.addTriangle(triangle);
							}							
						}

						break;
					}
				}
			}

			if (triangleArray.isSetTriangle() && !triangleArray.getTriangle().isEmpty()) {
				triangulatedSurface.setTrianglePatches(triangleArray);

				// check whether we have to embrace the compositeSurface with an orientableSurface
				if (initOrientableSurface || (isSetOrientableSurface && !geomNode.isReverse)) {
					OrientableSurface orientableSurface = new OrientableSurfaceImpl();				
					SurfaceProperty surfaceProperty = new SurfacePropertyImpl();
					surfaceProperty.setSurface(triangulatedSurface);
					orientableSurface.setBaseSurface(surfaceProperty);
					orientableSurface.setOrientation("-");
					dbExporterManager.updateGeometryCounter(GMLClass.ORIENTABLE_SURFACE);

					return new DBSurfaceGeometryResult(orientableSurface);
				} else
					return new DBSurfaceGeometryResult(triangulatedSurface);
			}

			return null;
		}

		return null;
	}
	
	private void writeToAppearanceCache(GeometryNode geomNode) throws SQLException {
		psImportGmlId.setString(1, geomNode.gmlId);
		psImportGmlId.setLong(2, geomNode.id);
		psImportGmlId.setLong(3, geomNode.id);
		psImportGmlId.addBatch();
		batchCounter++;

		if (batchCounter == commitAfter || batchCounter == Internal.ORACLE_MAX_BATCH_SIZE) {
			psImportGmlId.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws SQLException {
		psSurfaceGeometry.close();
		if (psImportGmlId != null) {
			psImportGmlId.executeBatch();
			psImportGmlId.close();
		}
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.SURFACE_GEOMETRY;
	}

	private class GeometryNode {
		protected long id;
		protected String gmlId;
		protected long parentId;
		protected boolean isSolid;
		protected boolean isComposite;
		protected boolean isTriangulated;
		protected boolean isXlink;
		protected boolean isReverse;
		protected JGeometry geometry;
		protected List<GeometryNode> childNodes;

		public GeometryNode() {
			childNodes = new ArrayList<GeometryNode>();
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
				pseudoNode.parentId = geomNode.parentId;
				pseudoNode.isSolid = geomNode.isSolid;
				pseudoNode.isComposite = geomNode.isComposite;
				pseudoNode.isTriangulated = geomNode.isTriangulated;
				pseudoNode.isXlink = geomNode.isXlink;
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
					// pseudo node
					parentNode = new GeometryNode();
					geometryTree.put(parentId, parentNode);
				}

				parentNode.childNodes.add(geomNode);
			}
		}

		public GeometryNode getNode(long entryId) {
			return geometryTree.get(entryId);
		}
	}
}
