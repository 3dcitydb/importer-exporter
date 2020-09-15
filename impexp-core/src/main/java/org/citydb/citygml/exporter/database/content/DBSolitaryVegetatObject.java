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
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBSolitaryVegetatObject extends AbstractFeatureExporter<SolitaryVegetationObject> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;
	private final DBImplicitGeometry implicitGeometryExporter;
	private final GMLConverter gmlConverter;

	private final String vegetationModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private final List<Table> adeHookTables;

	public DBSolitaryVegetatObject(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(SolitaryVegetationObject.class, connection, exporter);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.SOLITARY_VEGETAT_OBJECT.getName());
		vegetationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.VEGETATION).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.SOLITARY_VEGETAT_OBJECT.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", vegetationModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", vegetationModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", vegetationModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("species", vegetationModule)) select.addProjection(table.getColumn("species"), table.getColumn("species_codespace"));
		if (projectionFilter.containsProperty("height", vegetationModule)) select.addProjection(table.getColumn("height"), table.getColumn("height_unit"));
		if (projectionFilter.containsProperty("trunkDiameter", vegetationModule)) select.addProjection(table.getColumn("trunk_diameter"), table.getColumn("trunk_diameter_unit"));
		if (projectionFilter.containsProperty("crownDiameter", vegetationModule)) select.addProjection(table.getColumn("crown_diameter"), table.getColumn("crown_diameter_unit"));
		if (lodFilter.isEnabled(1)) {
			if (projectionFilter.containsProperty("lod1Geometry", vegetationModule)) select.addProjection(table.getColumn("lod1_brep_id"), exporter.getGeometryColumn(table.getColumn("lod1_other_geom")));
			if (projectionFilter.containsProperty("lod1ImplicitRepresentation", vegetationModule)) select.addProjection(table.getColumn("lod1_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod1_implicit_ref_point")), table.getColumn("lod1_implicit_transformation"));
		}
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2Geometry", vegetationModule)) select.addProjection(table.getColumn("lod2_brep_id"), exporter.getGeometryColumn(table.getColumn("lod2_other_geom")));
			if (projectionFilter.containsProperty("lod2ImplicitRepresentation", vegetationModule)) select.addProjection(table.getColumn("lod2_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod2_implicit_ref_point")), table.getColumn("lod2_implicit_transformation"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3Geometry", vegetationModule)) select.addProjection(table.getColumn("lod3_brep_id"), exporter.getGeometryColumn(table.getColumn("lod3_other_geom")));
			if (projectionFilter.containsProperty("lod3ImplicitRepresentation", vegetationModule)) select.addProjection(table.getColumn("lod3_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point")), table.getColumn("lod3_implicit_transformation"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4Geometry", vegetationModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
			if (projectionFilter.containsProperty("lod4ImplicitRepresentation", vegetationModule)) select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));
		}
		adeHookTables = addJoinsToADEHookTables(TableEnum.SOLITARY_VEGETAT_OBJECT, table);
	}

	@Override
	protected Collection<SolitaryVegetationObject> doExport(long id, SolitaryVegetationObject root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<SolitaryVegetationObject> vegetationObjects = new ArrayList<>();

			while (rs.next()) {
				long vegetationObjectId = rs.getLong("id");
				SolitaryVegetationObject vegetationObject;
				FeatureType featureType;

				if (vegetationObjectId == id && root != null) {
					vegetationObject = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						// create solitary vegetation object
						int objectClassId = rs.getInt("objectclass_id");
						vegetationObject = exporter.createObject(objectClassId, SolitaryVegetationObject.class);
						if (vegetationObject == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, vegetationObjectId) + " as solitary vegetation object.");
							continue;
						}

						featureType = exporter.getFeatureType(objectClassId);
					} else {
						vegetationObject = new SolitaryVegetationObject();
						featureType = exporter.getFeatureType(vegetationObject);
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				cityObjectExporter.addBatch(vegetationObject, vegetationObjectId, featureType, projectionFilter);

				if (projectionFilter.containsProperty("class", vegetationModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						vegetationObject.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", vegetationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						vegetationObject.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", vegetationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						vegetationObject.addUsage(usage);
					}
				}

				if (projectionFilter.containsProperty("species", vegetationModule)) {
					String species = rs.getString("species");
					if (!rs.wasNull()) {
						Code code = new Code(species);
						code.setCodeSpace(rs.getString("species_codespace"));
						vegetationObject.setSpecies(code);
					}
				}

				if (projectionFilter.containsProperty("height", vegetationModule)) {
					double height = rs.getDouble("height");
					if (!rs.wasNull()) {
						Length length = new Length(height);
						length.setUom(rs.getString("height_unit"));
						vegetationObject.setHeight(length);
					}
				}

				if (projectionFilter.containsProperty("trunkDiameter", vegetationModule)) {
					double trunkDiameter = rs.getDouble("trunk_diameter");
					if (!rs.wasNull()) {
						Length length = new Length(trunkDiameter);
						length.setUom(rs.getString("trunk_diameter_unit"));
						vegetationObject.setTrunkDiameter(length);
					}
				}

				if (projectionFilter.containsProperty("crownDiameter", vegetationModule)) {
					double crownDiameter = rs.getDouble("crown_diameter");
					if (!rs.wasNull()) {
						Length length = new Length(crownDiameter);
						length.setUom(rs.getString("crown_diameter_unit"));
						vegetationObject.setCrownDiameter(length);
					}
				}

				// geometry
				LodIterator lodIterator = lodFilter.iterator(1, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "Geometry", vegetationModule))
						continue;

					long geometryId = rs.getLong("lod" + lod + "_brep_id");
					if (!rs.wasNull()) {
						switch (lod) {
							case 1:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) vegetationObject::setLod1Geometry);
								break;
							case 2:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) vegetationObject::setLod2Geometry);
								break;
							case 3:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) vegetationObject::setLod3Geometry);
								break;
							case 4:
								geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) vegetationObject::setLod4Geometry);
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
									vegetationObject.setLod1Geometry(property);
									break;
								case 2:
									vegetationObject.setLod2Geometry(property);
									break;
								case 3:
									vegetationObject.setLod3Geometry(property);
									break;
								case 4:
									vegetationObject.setLod4Geometry(property);
									break;
							}
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", vegetationModule))
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
							vegetationObject.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							vegetationObject.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							vegetationObject.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 4:
							vegetationObject.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, vegetationObject, vegetationObjectId, featureType, projectionFilter);
				}

				vegetationObjects.add(vegetationObject);
			}

			return vegetationObjects;
		}
	}
}
