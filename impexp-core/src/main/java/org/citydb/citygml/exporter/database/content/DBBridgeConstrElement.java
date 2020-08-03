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

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.citygml.exporter.util.GeometrySetter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBBridgeConstrElement extends AbstractFeatureExporter<BridgeConstructionElement> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBBridgeThematicSurface thematicSurfaceExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final GMLConverter gmlConverter;

	private final String bridgeModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private List<Table> adeHookTables;

	public DBBridgeConstrElement(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(BridgeConstructionElement.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_CONSTR_ELEMENT.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.BRIDGE_CONSTR_ELEMENT.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (lodFilter.isEnabled(1)) {
			if (projectionFilter.containsProperty("lod1TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
			if (projectionFilter.containsProperty("lod1Geometry", bridgeModule)) select.addProjection(table.getColumn("lod1_brep_id"), exporter.getGeometryColumn(table.getColumn("lod1_other_geom")));
			if (projectionFilter.containsProperty("lod1ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod1_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod1_implicit_ref_point")), table.getColumn("lod1_implicit_transformation"));
		}
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
			if (projectionFilter.containsProperty("lod2Geometry", bridgeModule)) select.addProjection(table.getColumn("lod2_brep_id"), exporter.getGeometryColumn(table.getColumn("lod2_other_geom")));
			if (projectionFilter.containsProperty("lod2ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod2_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod2_implicit_ref_point")), table.getColumn("lod2_implicit_transformation"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
			if (projectionFilter.containsProperty("lod3Geometry", bridgeModule)) select.addProjection(table.getColumn("lod3_brep_id"), exporter.getGeometryColumn(table.getColumn("lod3_other_geom")));
			if (projectionFilter.containsProperty("lod3ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4TerrainIntersection", bridgeModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
			if (projectionFilter.containsProperty("lod4Geometry", bridgeModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule)) select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport())
			adeHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_CONSTR_ELEMENT, table);
		
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Collection<BridgeConstructionElement> doExport(AbstractBridge parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("bridge_id"));
	}

	@Override
	protected Collection<BridgeConstructionElement> doExport(long id, BridgeConstructionElement root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<BridgeConstructionElement> constructionElements = new ArrayList<>();

			while (rs.next()) {
				long constructionElementId = rs.getLong("id");
				BridgeConstructionElement constructionElement;
				FeatureType featureType;

				if (constructionElementId == id && root != null) {
					constructionElement = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create bridge construction element object
						constructionElement = exporter.createObject(objectClassId, BridgeConstructionElement.class);
						if (constructionElement == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, constructionElementId) + " as bridge construction element object.");
							continue;
						}
					} else {
						constructionElement = new BridgeConstructionElement();
						featureType = exporter.getFeatureType(constructionElement);
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectExporter.addBatch(constructionElement, constructionElementId, featureType, projectionFilter);

				if (projectionFilter.containsProperty("class", bridgeModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						constructionElement.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						constructionElement.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						constructionElement.addUsage(usage);
					}
				}

				// brid:boundedBy
				if (projectionFilter.containsProperty("boundedBy", bridgeModule) 
						&& lodFilter.containsLodGreaterThanOrEuqalTo(2)) {
					for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(constructionElement, constructionElementId))
						constructionElement.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
				}

				LodIterator lodIterator = lodFilter.iterator(1, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "TerrainIntersection", bridgeModule))
						continue;

					Object terrainIntersectionObj = rs.getObject("lod" + lod + "_terrain_intersection");
					if (rs.wasNull())
						continue;

					GeometryObject terrainIntersection = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
					if (terrainIntersection != null) {
						MultiCurveProperty multiCurveProperty = gmlConverter.getMultiCurveProperty(terrainIntersection, false);
						if (multiCurveProperty != null) {
							switch (lod) {
							case 1:
								constructionElement.setLod1TerrainIntersection(multiCurveProperty);
								break;
							case 2:
								constructionElement.setLod2TerrainIntersection(multiCurveProperty);
								break;
							case 3:
								constructionElement.setLod3TerrainIntersection(multiCurveProperty);
								break;
							case 4:
								constructionElement.setLod4TerrainIntersection(multiCurveProperty);
								break;
							}
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "Geometry", bridgeModule))
						continue;

					long geometryId = rs.getLong("lod" + lod + "_brep_id");
					if (!rs.wasNull()) {
						switch (lod) {
							case 1:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) constructionElement::setLod1Geometry);
								break;
							case 2:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) constructionElement::setLod2Geometry);
								break;
							case 3:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) constructionElement::setLod3Geometry);
								break;
							case 4:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) constructionElement::setLod4Geometry);
								break;
						}
					} else {
						Object geometryObj = rs.getObject("lod" + lod + "_other_geom");
						if (rs.wasNull())
							continue;

						GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
						if (geometry != null) {
							GeometryProperty<AbstractGeometry> property = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
							switch (lod) {
								case 1:
									constructionElement.setLod1Geometry(property);
									break;
								case 2:
									constructionElement.setLod2Geometry(property);
									break;
								case 3:
									constructionElement.setLod3Geometry(property);
									break;
								case 4:
									constructionElement.setLod4Geometry(property);
									break;
							}
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", bridgeModule))
						continue;

					// get implicit geometry details
					long implicitGeometryId = rs.getLong("lod" + lod + "_implicit_rep_id");
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject("lod" + lod + "_implicit_ref_point");
					if (!rs.wasNull())
						referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString("lod" + lod + "_implicit_transformation");

					ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 1:
							constructionElement.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							constructionElement.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							constructionElement.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							constructionElement.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, constructionElement, constructionElementId, featureType, projectionFilter);
				}
				
				constructionElements.add(constructionElement);
			}

			return constructionElements;
		}
	}

}
