/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

public class DBBuildingFurniture extends AbstractFeatureExporter<BuildingFurniture> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private GMLConverter gmlConverter;

	private String buildingModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;
	private boolean hasObjectClassIdColumn;
	private Set<String> adeHookTables;

	public DBBuildingFurniture(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(BuildingFurniture.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BUILDING_FURNITURE.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.BUILDING_FURNITURE.getName());
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", buildingModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", buildingModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", buildingModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));		
		if (projectionFilter.containsProperty("lod4Geometry", buildingModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
		if (projectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule))
			select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.BUILDING_FURNITURE);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}
		
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Collection<BuildingFurniture> doExport(Room parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("room_id"));
	}
	
	@Override
	protected Collection<BuildingFurniture> doExport(long id, BuildingFurniture root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<BuildingFurniture> buildingFurnitures = new ArrayList<>();

			while (rs.next()) {
				long buildingFurnitureId = rs.getLong("id");
				BuildingFurniture buildingFurniture = null;
				FeatureType featureType = null;
				
				if (buildingFurnitureId == id && root != null) {
					buildingFurniture = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create building furniture object
						buildingFurniture = exporter.createObject(objectClassId, BuildingFurniture.class);
						if (buildingFurniture == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, buildingFurnitureId) + " as building furniture object.");
							continue;
						}
					} else {
						buildingFurniture = new BuildingFurniture();
						featureType = exporter.getFeatureType(buildingFurniture);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectExporter.doExport(buildingFurniture, buildingFurnitureId, featureType, projectionFilter);

				if (projectionFilter.containsProperty("class", buildingModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						buildingFurniture.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", buildingModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						buildingFurniture.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", buildingModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						buildingFurniture.addUsage(usage);
					}
				}
				
				if (lodFilter.isEnabled(4)) {
					if (projectionFilter.containsProperty("lod4Geometry", buildingModule)) {					
						long geometryId = rs.getLong("lod4_brep_id");
						Object geometryObj = rs.getObject("lod4_other_geom");
						if (geometryId != 0 || geometryObj != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = null;
							if (geometryId != 0) {
								SurfaceGeometry geometry = geometryExporter.doExport(geometryId);
								if (geometry != null) {
									geometryProperty = new GeometryProperty<>();
									if (geometry.isSetGeometry())
										geometryProperty.setGeometry(geometry.getGeometry());
									else
										geometryProperty.setHref(geometry.getReference());
								}
							} else {
								GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
								if (geometry != null)
									geometryProperty = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
							}

							if (geometryProperty != null)
								buildingFurniture.setLod4Geometry(geometryProperty);
						}
					}

					if (projectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule)) {					
						long implicitGeometryId = rs.getLong("lod4_implicit_rep_id");
						if (!rs.wasNull()) {
							GeometryObject referencePoint = null;
							Object referencePointObj = rs.getObject("lod4_implicit_ref_point");
							if (!rs.wasNull())
								referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

							String transformationMatrix = rs.getString("lod4_implicit_transformation");

							ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
							if (implicit != null) {
								ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
								implicitProperty.setObject(implicit);
								buildingFurniture.setLod4ImplicitRepresentation(implicitProperty);
							}
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, buildingFurniture, buildingFurnitureId, featureType, projectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(buildingFurniture))
					continue;

				buildingFurnitures.add(buildingFurniture);
			}
			
			return buildingFurnitures;
		}
	}

}
