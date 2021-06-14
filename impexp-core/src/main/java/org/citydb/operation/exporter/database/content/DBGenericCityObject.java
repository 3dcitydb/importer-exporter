/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.operation.exporter.database.content;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.operation.exporter.CityGMLExportException;
import org.citydb.operation.exporter.util.AttributeValueSplitter;
import org.citydb.operation.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.operation.exporter.util.GeometrySetter;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.generics.GenericCityObject;
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

public class DBGenericCityObject extends AbstractFeatureExporter<GenericCityObject> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final GMLConverter gmlConverter;

	private final String genericsModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final List<Table> adeHookTables;

	public DBGenericCityObject(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(GenericCityObject.class, connection, exporter);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.GENERIC_CITYOBJECT.getName());
		genericsModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.GENERICS).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.GENERIC_CITYOBJECT.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", genericsModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", genericsModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", genericsModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (lodFilter.isEnabled(0)) {
			if (projectionFilter.containsProperty("lod0TerrainIntersection", genericsModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod0_terrain_intersection")));
			if (projectionFilter.containsProperty("lod0Geometry", genericsModule)) select.addProjection(table.getColumn("lod0_brep_id"), exporter.getGeometryColumn(table.getColumn("lod0_other_geom")));
			if (projectionFilter.containsProperty("lod0ImplicitRepresentation", genericsModule)) select.addProjection(table.getColumn("lod0_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod0_implicit_ref_point")), table.getColumn("lod0_implicit_transformation"));
		}
		if (lodFilter.isEnabled(1)) {
			if (projectionFilter.containsProperty("lod1TerrainIntersection", genericsModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
			if (projectionFilter.containsProperty("lod1Geometry", genericsModule)) select.addProjection(table.getColumn("lod1_brep_id"), exporter.getGeometryColumn(table.getColumn("lod1_other_geom")));
			if (projectionFilter.containsProperty("lod1ImplicitRepresentation", genericsModule)) select.addProjection(table.getColumn("lod1_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod1_implicit_ref_point")), table.getColumn("lod1_implicit_transformation"));
		}
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2TerrainIntersection", genericsModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
			if (projectionFilter.containsProperty("lod2Geometry", genericsModule)) select.addProjection(table.getColumn("lod2_brep_id"), exporter.getGeometryColumn(table.getColumn("lod2_other_geom")));
			if (projectionFilter.containsProperty("lod2ImplicitRepresentation", genericsModule)) select.addProjection(table.getColumn("lod2_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod2_implicit_ref_point")), table.getColumn("lod2_implicit_transformation"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3TerrainIntersection", genericsModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
			if (projectionFilter.containsProperty("lod3Geometry", genericsModule)) select.addProjection(table.getColumn("lod3_brep_id"), exporter.getGeometryColumn(table.getColumn("lod3_other_geom")));
			if (projectionFilter.containsProperty("lod3ImplicitRepresentation", genericsModule)) select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4TerrainIntersection", genericsModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
			if (projectionFilter.containsProperty("lod4Geometry", genericsModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", genericsModule)) select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		}
		adeHookTables = addJoinsToADEHookTables(TableEnum.GENERIC_CITYOBJECT, table);
	}

	@Override
	protected Collection<GenericCityObject> doExport(long id, GenericCityObject root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<GenericCityObject> genericCityObjects = new ArrayList<>();

			while (rs.next()) {
				long genericCityObjectId = rs.getLong("id");
				GenericCityObject genericCityObject;
				FeatureType featureType;
				
				if (genericCityObjectId == id && root != null) {
					genericCityObject = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						// create generic city object
						int objectClassId = rs.getInt("objectclass_id");
						genericCityObject = exporter.createObject(objectClassId, GenericCityObject.class);
						if (genericCityObject == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, genericCityObjectId) + " as generic city object.");
							continue;
						}

						featureType = exporter.getFeatureType(objectClassId);
					} else {
						genericCityObject = new GenericCityObject();
						featureType = exporter.getFeatureType(genericCityObject);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectExporter.addBatch(genericCityObject, genericCityObjectId, featureType, projectionFilter);

				if (projectionFilter.containsProperty("class", genericsModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						genericCityObject.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", genericsModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						genericCityObject.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", genericsModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						genericCityObject.addUsage(usage);
					}
				}

				// terrainIntersection
				LodIterator lodIterator = lodFilter.iterator(0, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "TerrainIntersection", genericsModule))
						continue;

					Object terrainIntersectionObj = rs.getObject("lod" + lod + "_terrain_intersection");
					if (rs.wasNull())
						continue;

					GeometryObject terrainIntersection = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
					if (terrainIntersection != null) {
						MultiCurveProperty multiCurveProperty = gmlConverter.getMultiCurveProperty(terrainIntersection, false);
						if (multiCurveProperty != null) {
							switch (lod) {
							case 0:
								genericCityObject.setLod0TerrainIntersection(multiCurveProperty);
								break;
							case 1:
								genericCityObject.setLod1TerrainIntersection(multiCurveProperty);
								break;
							case 2:
								genericCityObject.setLod2TerrainIntersection(multiCurveProperty);
								break;
							case 3:
								genericCityObject.setLod3TerrainIntersection(multiCurveProperty);
								break;
							case 4:
								genericCityObject.setLod4TerrainIntersection(multiCurveProperty);
								break;
							}
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "Geometry", genericsModule))
						continue;

					long geometryId = rs.getLong("lod" + lod + "_brep_id");
					if (!rs.wasNull()) {
						switch (lod) {
							case 0:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) genericCityObject::setLod0Geometry);
								break;
							case 1:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) genericCityObject::setLod1Geometry);
								break;
							case 2:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) genericCityObject::setLod2Geometry);
								break;
							case 3:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) genericCityObject::setLod3Geometry);
								break;
							case 4:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) genericCityObject::setLod4Geometry);
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
								case 0:
									genericCityObject.setLod0Geometry(property);
									break;
								case 1:
									genericCityObject.setLod1Geometry(property);
									break;
								case 2:
									genericCityObject.setLod2Geometry(property);
									break;
								case 3:
									genericCityObject.setLod3Geometry(property);
									break;
								case 4:
									genericCityObject.setLod4Geometry(property);
									break;
							}
						}
					}
				}

				// implicit geometry
				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", genericsModule))
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
						case 0:
							genericCityObject.setLod0ImplicitRepresentation(implicitProperty);
							break;
						case 1:
							genericCityObject.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							genericCityObject.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							genericCityObject.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							genericCityObject.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, genericCityObject, genericCityObjectId, featureType, projectionFilter);
				}
				
				genericCityObjects.add(genericCityObject);
			}
			
			return genericCityObjects;
		}
	}
}
