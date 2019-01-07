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
package org.citydb.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
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

import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.ConstantColumn;

public class DBSurfaceGeometry implements DBExporter {
	private final CityGMLExportManager exporter;

	private PreparedStatement psSelect;
	private PreparedStatement psImport;

	private boolean exportAppearance;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean isImplicit;
	private String gmlIdPrefix;

	private int commitAfter;
	private int batchCounter;

	public DBSurfaceGeometry(Connection connection, CacheTable cacheTable, CityGMLExportManager exporter, Config config) throws SQLException {
		this.exporter = exporter;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		exportAppearance = config.getInternal().isExportGlobalAppearances();
		if (exportAppearance) {
			commitAfter = exporter.getDatabaseAdapter().getMaxBatchSize();
			Integer commitAfterProp = config.getProject().getDatabase().getUpdateBatching().getTempBatchValue();
			if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= exporter.getDatabaseAdapter().getMaxBatchSize())
				commitAfter = commitAfterProp;

			Table table = new Table(TableEnum.TEXTUREPARAM.getName(), schema);
			Select select = new Select().addProjection(new ConstantColumn(new PlaceHolder<>()));
			if (exporter.getDatabaseAdapter().getSQLAdapter().requiresPseudoTableInSelect()) select.setPseudoTable(exporter.getDatabaseAdapter().getSQLAdapter().getPseudoTableName());
			select.addSelection(ComparisonFactory.exists(new Select().addProjection(new ConstantColumn(1).withFromTable(table))
					.addSelection(ComparisonFactory.equalTo(table.getColumn("surface_geometry_id"), new PlaceHolder<>()))));
			
			psImport = cacheTable.getConnection().prepareStatement(new StringBuilder("insert into ").append(cacheTable.getTableName()).append(" ").append(select.toString()).toString());
		}

		useXLink = config.getProject().getExporter().getXlink().getGeometry().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getGeometry().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getGeometry().getIdPrefix();
		}

		Table table = new Table(TableEnum.SURFACE_GEOMETRY.getName(), schema);
		Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("gmlid"), table.getColumn("parent_id"), table.getColumn("is_solid"), table.getColumn("is_composite"),
				table.getColumn("is_triangulated"), table.getColumn("is_xlink"), table.getColumn("is_reverse"),
				exporter.getGeometryColumn(table.getColumn("geometry")), table.getColumn("implicit_geometry"))
				.addSelection(ComparisonFactory.equalTo(table.getColumn("root_id"), new PlaceHolder<>()));

		psSelect = connection.prepareStatement(select.toString());
	}

	protected SurfaceGeometry doExport(long rootId) throws CityGMLExportException, SQLException {
		psSelect.setLong(1, rootId);

		try (ResultSet rs = psSelect.executeQuery()) {
			GeometryTree geomTree = new GeometryTree();

			// firstly, read the geometry entries into a flat geometry tree structure
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
				if (!rs.wasNull()) {
					try {
						geometry = exporter.getDatabaseAdapter().getGeometryConverter().getPolygon(object);
					} catch (Exception e) {
						exporter.logOrThrowErrorMessage(new StringBuilder("Skipping ").append(exporter.getGeometrySignature(GMLClass.POLYGON, id))
								.append(": ").append(e.getMessage()).toString());
						continue;
					}
				}

				geomNode.geometry = geometry;

				// put polygon into the geometry tree
				geomTree.insertNode(geomNode, geomNode.parentId);
			}

			// interpret geometry tree as a single abstract geometry
			if (geomTree.root != 0)
				return rebuildGeometry(geomTree.getNode(geomTree.root), false, false);
			else {
				exporter.logOrThrowErrorMessage("Failed to interpret geometry object.");
				return null;
			}
		}
	}

	protected SurfaceGeometry doExportImplicitGeometry(long rootId) throws CityGMLExportException, SQLException {
		try {
			isImplicit = true;
			return doExport(rootId);
		} finally {
			isImplicit = false;
		}
	}

	private SurfaceGeometry rebuildGeometry(GeometryNode geomNode, boolean isSetOrientableSurface, boolean wasXlink) throws CityGMLExportException, SQLException {
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

		// check for xlinks
		if (geomNode.gmlId != null) {
			if (geomNode.isXlink) {
				if (exporter.lookupAndPutGeometryUID(geomNode.gmlId, geomNode.id)) {

					if (useXLink) {
						// check whether we have to embrace the geometry with an orientableSurface
						if (geomNode.isReverse != isSetOrientableSurface) {
							OrientableSurface orientableSurface = new OrientableSurface();				
							SurfaceProperty surfaceProperty = new SurfaceProperty();
							surfaceProperty.setHref("#" + geomNode.gmlId); 
							orientableSurface.setBaseSurface(surfaceProperty);
							orientableSurface.setOrientation(Sign.MINUS);

							return new SurfaceGeometry(orientableSurface);
						} else
							return new SurfaceGeometry("#" + geomNode.gmlId, surfaceGeometryType);
					} else {
						geomNode.isXlink = false;
						String gmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
						if (appendOldGmlId)
							gmlId = new StringBuilder(gmlId).append("-").append(geomNode.gmlId).toString();

						geomNode.gmlId = gmlId;
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
				}
			}

			// check whether we have to embrace the polygon with an orientableSurface
			if (initOrientableSurface || (isSetOrientableSurface && !geomNode.isReverse)) {
				OrientableSurface orientableSurface = new OrientableSurface();				
				SurfaceProperty surfaceProperty = new SurfaceProperty();
				surfaceProperty.setSurface(polygon);
				orientableSurface.setBaseSurface(surfaceProperty);
				orientableSurface.setOrientation(Sign.MINUS);

				return new SurfaceGeometry(orientableSurface);
			} else
				return new SurfaceGeometry(polygon);
		}

		// compositeSurface
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SURFACE) {
			CompositeSurface compositeSurface = new CompositeSurface();

			if (geomNode.gmlId != null)
				compositeSurface.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				SurfaceGeometry geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getGeometry();
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
						surfaceMember.setHref(geomMember.getReference());
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

					return new SurfaceGeometry(orientableSurface);
				} else
					return new SurfaceGeometry(compositeSurface);
			}					

			return null;
		}

		// compositeSolid
		else if (surfaceGeometryType == GMLClass.COMPOSITE_SOLID) {
			CompositeSolid compositeSolid = new CompositeSolid();

			if (geomNode.gmlId != null)
				compositeSolid.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				SurfaceGeometry geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getGeometry();
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
						solidMember.setHref(geomMember.getReference());
					}

					if (solidMember != null)
						compositeSolid.addSolidMember(solidMember);
				}
			}

			if (compositeSolid.isSetSolidMember())
				return new SurfaceGeometry(compositeSolid);

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
				SurfaceGeometry geomMember = rebuildGeometry(geomNode.childNodes.get(0), isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getGeometry();
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
						surfaceProperty.setHref(geomMember.getReference());
					}

					if (surfaceProperty != null)
						solid.setExterior(surfaceProperty);
				}
			}

			if (solid.isSetExterior())
				return new SurfaceGeometry(solid);

			return null;
		}

		// multiSolid
		else if (surfaceGeometryType == GMLClass.MULTI_SOLID) {
			MultiSolid multiSolid = new MultiSolid();

			if (geomNode.gmlId != null)
				multiSolid.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				SurfaceGeometry geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getGeometry();
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
						solidMember.setHref(geomMember.getReference());
					}

					if (solidMember != null)
						multiSolid.addSolidMember(solidMember);
				}
			}

			if (multiSolid.isSetSolidMember())
				return new SurfaceGeometry(multiSolid);

			return null;

		}

		// multiSurface
		else if (surfaceGeometryType == GMLClass.MULTI_SURFACE){
			MultiSurface multiSurface = new MultiSurface();

			if (geomNode.gmlId != null)
				multiSurface.setId(geomNode.gmlId);

			for (GeometryNode childNode : geomNode.childNodes) {
				SurfaceGeometry geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					AbstractGeometry absGeom = geomMember.getGeometry();
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
						surfaceMember.setHref(geomMember.getReference());
					}

					if (surfaceMember != null)
						multiSurface.addSurfaceMember(surfaceMember);
				}
			}

			if (multiSurface.isSetSurfaceMember())
				return new SurfaceGeometry(multiSurface);

			return null;
		}

		// triangulatedSurface
		else if (surfaceGeometryType == GMLClass.TRIANGULATED_SURFACE) {
			TriangulatedSurface triangulatedSurface = new TriangulatedSurface();

			if (geomNode.gmlId != null)
				triangulatedSurface.setId(geomNode.gmlId);

			TrianglePatchArrayProperty triangleArray = new TrianglePatchArrayProperty();
			for (GeometryNode childNode : geomNode.childNodes) {
				SurfaceGeometry geomMember = rebuildGeometry(childNode, isSetOrientableSurface, wasXlink);

				if (geomMember != null) {
					// we are only expecting polygons...
					AbstractGeometry absGeom = geomMember.getGeometry();					

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

					return new SurfaceGeometry(orientableSurface);
				} else
					return new SurfaceGeometry(triangulatedSurface);
			}

			return null;
		}

		return null;
	}

	private void writeToAppearanceCache(GeometryNode geomNode) throws SQLException {
		psImport.setLong(1, geomNode.id);
		psImport.setLong(2, geomNode.id);
		psImport.addBatch();
		batchCounter++;

		if (batchCounter == commitAfter) {
			psImport.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws SQLException {
		psSelect.close();

		if (psImport != null) {
			psImport.executeBatch();
			psImport.close();
		}
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
