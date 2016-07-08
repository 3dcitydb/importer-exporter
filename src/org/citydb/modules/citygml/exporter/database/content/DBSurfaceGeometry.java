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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.util.Util;
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
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBSurfaceGeometry implements DBExporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection connection;
	private final CacheTable tempTable;
	private final DBExporterManager dbExporterManager;
	private final Config config;

	private PreparedStatement psSurfaceGeometry;
	private PreparedStatement psTransformSurfaceGeometry;
	private PreparedStatement psImportGmlId;

	private boolean exportAppearance;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean applyTransformation;
	private boolean isImplicit;
	private String gmlIdPrefix;

	private int commitAfter;
	private int batchCounter;

	public DBSurfaceGeometry(Connection connection, CacheTable tempTable, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.tempTable = tempTable;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		exportAppearance = config.getInternal().isExportGlobalAppearances();

		if (exportAppearance) {
			commitAfter = dbExporterManager.getDatabaseAdapter().getMaxBatchSize();
			Integer commitAfterProp = config.getProject().getDatabase().getUpdateBatching().getTempBatchValue();
			if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= dbExporterManager.getDatabaseAdapter().getMaxBatchSize())
				commitAfter = commitAfterProp;

			StringBuilder query = new StringBuilder("insert into ").append(tempTable.getTableName()).append(" ")
					.append("select ? ");
			if (dbExporterManager.getDatabaseAdapter().getSQLAdapter().requiresPseudoTableInSelect())
				query.append("from ").append(dbExporterManager.getDatabaseAdapter().getSQLAdapter().getPseudoTableName()).append(" ");

			query.append("where exists (select 1 from TEXTUREPARAM where SURFACE_GEOMETRY_ID = ?)");
			psImportGmlId = tempTable.getConnection().prepareStatement(query.toString());
		}

		useXLink = config.getProject().getExporter().getXlink().getGeometry().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getGeometry().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getGeometry().getIdPrefix();
		}	

		StringBuilder query = new StringBuilder("select ID, GMLID, PARENT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, ")
		.append("GEOMETRY, IMPLICIT_GEOMETRY from SURFACE_GEOMETRY where ROOT_ID = ?");
		psSurfaceGeometry = connection.prepareStatement(query.toString());

		applyTransformation = config.getInternal().isTransformCoordinates();
		if (applyTransformation) {	
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			query = new StringBuilder("select ID, GMLID, PARENT_ID, IS_SOLID, IS_COMPOSITE, IS_TRIANGULATED, IS_XLINK, IS_REVERSE, ")
			.append(transformOrNull).append("(GEOMETRY, ").append(srid).append(") AS GEOMETRY, ")
			.append("IMPLICIT_GEOMETRY from SURFACE_GEOMETRY where ROOT_ID = ?");
			psTransformSurfaceGeometry = connection.prepareStatement(query.toString());
		}
	}

	public DBSurfaceGeometryResult read(long rootId) throws SQLException {
		ResultSet rs = null;

		try {
			if (!applyTransformation || isImplicit) {
				psSurfaceGeometry.setLong(1, rootId);
				rs = psSurfaceGeometry.executeQuery();
			} else {
				psTransformSurfaceGeometry.setLong(1, rootId);
				rs = psTransformSurfaceGeometry.executeQuery();
			}

			GeometryTree geomTree = new GeometryTree();

			// firstly, read the geometry entries into a
			// flat geometry tree structure
			while (rs.next()) {
				long id = rs.getLong(1);
				
				// constructing a geometry node
				GeometryNode geomNode = new GeometryNode();
				geomNode.id = id;				
				geomNode.gmlId = rs.getString(2);
				geomNode.parentId = rs.getLong(3);
				geomNode.isSolid = rs.getBoolean(4);
				geomNode.isComposite = rs.getBoolean(5);
				geomNode.isTriangulated = rs.getBoolean(6);
				geomNode.isXlink = rs.getBoolean(7);
				geomNode.isReverse = rs.getBoolean(8);

				GeometryObject geometry = null;
				Object object = rs.getObject(!isImplicit ? 9 : 10);
				if (!rs.wasNull() && object != null) {
					try {
						geometry = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPolygon(object);
					} catch (IllegalArgumentException e) {
						StringBuilder msg = new StringBuilder("Skipping ").append(Util.getGeometrySignature(
								GMLClass.POLYGON, 
								geomNode.gmlId));
						msg.append(": ").append(e.getMessage());

						LOG.error(msg.toString());
						continue;
					}
				}

				geomNode.geometry = geometry;

				// put it into our geometry tree
				geomTree.insertNode(geomNode, geomNode.parentId);
			}

			// interpret geometry tree as a single abstract geometry
			if (geomTree.root != 0)
				return rebuildGeometry(geomTree.getNode(geomTree.root), false, false);
			else {
				LOG.error("Failed to interpret geometry object.");
				return null;
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}
	
	public DBSurfaceGeometryResult readImplicitGeometry(long rootId) throws SQLException {
		try {
			// if coordinate transformation is activated we need to ensure
			// that the transformation is not applied to the prototype
			isImplicit = true;
			return read(rootId);
		} finally {
			isImplicit = false;
		}
	}

	private DBSurfaceGeometryResult rebuildGeometry(GeometryNode geomNode, boolean isSetOrientableSurface, boolean wasXlink) throws SQLException {
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
						// check whether we have to embrace the geometry with an orientableSurface
						if (geomNode.isReverse != isSetOrientableSurface) {
							OrientableSurface orientableSurface = new OrientableSurface();				
							SurfaceProperty surfaceProperty = new SurfaceProperty();
							surfaceProperty.setHref("#" + geomNode.gmlId); 
							orientableSurface.setBaseSurface(surfaceProperty);
							orientableSurface.setOrientation(Sign.MINUS);
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
						return rebuildGeometry(geomNode, isSetOrientableSurface, true);
					}
				}
			} 

			if (exportAppearance && !wasXlink)
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
			// try and interpret geometry object from database
			Polygon polygon = new Polygon();
			boolean forceRingIds = false;

			if (geomNode.gmlId != null) {
				polygon.setId(geomNode.gmlId);
				forceRingIds = true;
			}

			// we suppose we have one outer ring and one or more inner rings
			boolean isExterior = true;
			for (int ringIndex = 0; ringIndex < geomNode.geometry.getNumElements(); ringIndex++) {
				List<Double> values = null;

				// check whether we have to reverse the coordinate order
				if (!geomNode.isReverse) { 
					values = geomNode.geometry.getCoordinatesAsList(ringIndex);
				} else {
					values = new ArrayList<Double>(geomNode.geometry.getCoordinates(ringIndex).length);
					double[] coordinates = geomNode.geometry.getCoordinates(ringIndex);
					for (int i = coordinates.length - 3; i >= 0; i -= 3) {
						values.add(coordinates[i]);
						values.add(coordinates[i + 1]);
						values.add(coordinates[i + 2]);
					}
				}

				if (isExterior) {
					LinearRing linearRing = new LinearRing();
					DirectPositionList directPositionList = new DirectPositionList();

					if (forceRingIds)
						linearRing.setId(polygon.getId() + '_' + ringIndex + '_');

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(3);
					linearRing.setPosList(directPositionList);

					Exterior exterior = new Exterior();
					exterior.setRing(linearRing);
					polygon.setExterior(exterior);

					isExterior = false;
					dbExporterManager.updateGeometryCounter(GMLClass.LINEAR_RING);
				} else {
					LinearRing linearRing = new LinearRing();
					DirectPositionList directPositionList = new DirectPositionList();

					if (forceRingIds)
						linearRing.setId(polygon.getId() + '_' + ringIndex + '_');

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(3);
					linearRing.setPosList(directPositionList);

					Interior interior = new Interior();
					interior.setRing(linearRing);
					polygon.addInterior(interior);

					dbExporterManager.updateGeometryCounter(GMLClass.LINEAR_RING);
				}
			}

			// check whether we have to embrace the polygon with an orientableSurface
			if (initOrientableSurface || (isSetOrientableSurface && !geomNode.isReverse)) {
				OrientableSurface orientableSurface = new OrientableSurface();				
				SurfaceProperty surfaceProperty = new SurfaceProperty();
				surfaceProperty.setSurface(polygon);
				orientableSurface.setBaseSurface(surfaceProperty);
				orientableSurface.setOrientation(Sign.MINUS);
				dbExporterManager.updateGeometryCounter(GMLClass.ORIENTABLE_SURFACE);

				return new DBSurfaceGeometryResult(orientableSurface);
			} else
				return new DBSurfaceGeometryResult(polygon);
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SURFACE) {
			CompositeSurface compositeSurface = new CompositeSurface();

			if (geomNode.gmlId != null)
				compositeSurface.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SurfaceProperty surfaceMember = new SurfaceProperty();

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
					OrientableSurface orientableSurface = new OrientableSurface();				
					SurfaceProperty surfaceProperty = new SurfaceProperty();
					surfaceProperty.setSurface(compositeSurface);
					orientableSurface.setBaseSurface(surfaceProperty);
					orientableSurface.setOrientation(Sign.MINUS);
					dbExporterManager.updateGeometryCounter(GMLClass.ORIENTABLE_SURFACE);

					return new DBSurfaceGeometryResult(orientableSurface);
				} else
					return new DBSurfaceGeometryResult(compositeSurface);
			}					

			return null;
		}

		// compositeSolid
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SOLID) {
			CompositeSolid compositeSolid = new CompositeSolid();

			if (geomNode.gmlId != null)
				compositeSolid.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SolidProperty solidMember = new SolidProperty();

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
			Solid solid = new Solid();

			if (geomNode.gmlId != null)
				solid.setId(geomNode.gmlId);

			// we strongly assume solids contain one single CompositeSurface
			// as exterior. Nothing else is interpreted here...
			if (geomNode.childNodes.size() == 1) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(geomNode.childNodes.get(0), isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SurfaceProperty surfaceProperty = new SurfaceProperty();

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
			MultiSolid multiSolid = new MultiSolid();

			if (geomNode.gmlId != null)
				multiSolid.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SolidProperty solidMember = new SolidProperty();

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
			MultiSurface multiSurface = new MultiSurface();

			if (geomNode.gmlId != null)
				multiSurface.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();
					SurfaceProperty surfaceMember = new SurfaceProperty();

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
			TriangulatedSurface triangulatedSurface = new TriangulatedSurface();

			if (geomNode.gmlId != null)
				triangulatedSurface.setId(geomNode.gmlId);

			TrianglePatchArrayProperty triangleArray = new TrianglePatchArrayProperty();
			for (GeometryNode childNode : geomNode.childNodes) {
				DBSurfaceGeometryResult geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					// we are only expecting polygons...
					AbstractGeometry absGeom = geomMember.getAbstractGeometry();					

					if (geomMember.getType() == GMLClass.POLYGON) {
						// we do not have to deal with xlinks here...
						if (absGeom != null) {
							// convert polygon to trianglePatch
							Triangle triangle = new Triangle();
							Polygon polygon = (Polygon)absGeom;

							if (polygon.isSetExterior()) {								
								triangle.setExterior(polygon.getExterior());
								triangleArray.addTriangle(triangle);
							}							
						}
					}
				}
			}

			if (triangleArray.isSetTriangle() && !triangleArray.getTriangle().isEmpty()) {
				triangulatedSurface.setTrianglePatches(triangleArray);

				// check whether we have to embrace the compositeSurface with an orientableSurface
				if (initOrientableSurface || (isSetOrientableSurface && !geomNode.isReverse)) {
					OrientableSurface orientableSurface = new OrientableSurface();				
					SurfaceProperty surfaceProperty = new SurfaceProperty();
					surfaceProperty.setSurface(triangulatedSurface);
					orientableSurface.setBaseSurface(surfaceProperty);
					orientableSurface.setOrientation(Sign.MINUS);
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
		psImportGmlId.setLong(1, geomNode.id);
		psImportGmlId.setLong(2, geomNode.id);
		psImportGmlId.addBatch();
		batchCounter++;

		if (batchCounter == commitAfter) {
			psImportGmlId.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws SQLException {
		psSurfaceGeometry.close();

		if (psTransformSurfaceGeometry != null)
			psTransformSurfaceGeometry.close();

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
		protected GeometryObject geometry;
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
