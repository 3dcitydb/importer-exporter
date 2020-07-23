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
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBPlantCover extends AbstractFeatureExporter<PlantCover> {
	private final DBSurfaceGeometry geometryExporter;
	private final DBCityObject cityObjectExporter;

	private final String vegetationModule;
	private final LodFilter lodFilter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean hasObjectClassIdColumn;
	private List<Table> adeHookTables;

	public DBPlantCover(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(PlantCover.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.PLANT_COVER.getName());
		vegetationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.VEGETATION).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.PLANT_COVER.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", vegetationModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", vegetationModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", vegetationModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("averageHeight", vegetationModule)) select.addProjection(table.getColumn("average_height"), table.getColumn("average_height_unit"));
		if (projectionFilter.containsProperty("lod1MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		if (projectionFilter.containsProperty("lod2MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("lod1MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod1_multi_solid_id"));
		if (projectionFilter.containsProperty("lod2MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod2_multi_solid_id"));
		if (projectionFilter.containsProperty("lod3MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod3_multi_solid_id"));
		if (projectionFilter.containsProperty("lod4MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod4_multi_solid_id"));
		
		// add joins to ADE hook tables
		if (exporter.hasADESupport())
			adeHookTables = addJoinsToADEHookTables(TableEnum.PLANT_COVER, table);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	@Override
	protected Collection<PlantCover> doExport(long id, PlantCover root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<PlantCover> plantCovers = new ArrayList<>();

			while (rs.next()) {
				long plantCoverId = rs.getLong("id");
				PlantCover plantCover;
				FeatureType featureType;
				
				if (plantCoverId == id && root != null) {
					plantCover = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create plant cover object
						plantCover = exporter.createObject(objectClassId, PlantCover.class);
						if (plantCover == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, plantCoverId) + " as city furniture object.");
							continue;
						}
					} else {
						plantCover = new PlantCover();
						featureType = exporter.getFeatureType(plantCover);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				boolean success = cityObjectExporter.doExport(plantCover, plantCoverId, featureType, projectionFilter);
				if (!success)
					continue;
				
				if (projectionFilter.containsProperty("class", vegetationModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						plantCover.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", vegetationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						plantCover.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", vegetationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						plantCover.addUsage(usage);
					}
				}
				
				if (projectionFilter.containsProperty("averageHeight", vegetationModule)) {
					double averageHeight = rs.getDouble("average_height");
					if (!rs.wasNull()) {
						Length length = new Length(averageHeight);
						length.setUom(rs.getString("average_height_unit"));
						plantCover.setAverageHeight(length);
					}
				}

				LodIterator lodIterator = lodFilter.iterator(1, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", vegetationModule))
						continue;

					long surfaceGeometryId = rs.getLong("lod" + lod + "_multi_surface_id");
					if (rs.wasNull())
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.getGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getReference());

						switch (lod) {
						case 1:
							plantCover.setLod1MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							plantCover.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							plantCover.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 4:
							plantCover.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "MultiSolid", vegetationModule))
						continue;

					long surfaceGeometryId = rs.getLong("lod" + lod + "_multi_solid_id");
					if (rs.wasNull())
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SOLID) {
						MultiSolidProperty solidProperty = new MultiSolidProperty();
						if (geometry.isSetGeometry())
							solidProperty.setMultiSolid((MultiSolid)geometry.getGeometry());
						else
							solidProperty.setHref(geometry.getReference());

						switch (lod) {
						case 1:
							plantCover.setLod1MultiSolid(solidProperty);
							break;
						case 2:
							plantCover.setLod2MultiSolid(solidProperty);
							break;
						case 3:
							plantCover.setLod3MultiSolid(solidProperty);
							break;
						case 4:
							plantCover.setLod4MultiSolid(solidProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, plantCover, plantCoverId, featureType, projectionFilter);
				}
				
				plantCovers.add(plantCover);
			}

			return plantCovers;
		}
	}

}
