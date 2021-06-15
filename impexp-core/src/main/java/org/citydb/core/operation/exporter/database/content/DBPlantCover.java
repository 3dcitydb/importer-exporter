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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.lod.LodIterator;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.basicTypes.Code;
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
	private final List<Table> adeHookTables;

	public DBPlantCover(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(PlantCover.class, connection, exporter);

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.PLANT_COVER.getName());
		vegetationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.VEGETATION).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.PLANT_COVER.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", vegetationModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", vegetationModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", vegetationModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("averageHeight", vegetationModule)) select.addProjection(table.getColumn("average_height"), table.getColumn("average_height_unit"));
		if (lodFilter.isEnabled(1)) {
			if (projectionFilter.containsProperty("lod1MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
			if (projectionFilter.containsProperty("lod1MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod1_multi_solid_id"));
		}
		if (lodFilter.isEnabled(2)) {
			if (projectionFilter.containsProperty("lod2MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
			if (projectionFilter.containsProperty("lod2MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod2_multi_solid_id"));
		}
		if (lodFilter.isEnabled(3)) {
			if (projectionFilter.containsProperty("lod3MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
			if (projectionFilter.containsProperty("lod3MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod3_multi_solid_id"));
		}
		if (lodFilter.isEnabled(4)) {
			if (projectionFilter.containsProperty("lod4MultiSurface", vegetationModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
			if (projectionFilter.containsProperty("lod4MultiSolid", vegetationModule)) select.addProjection(table.getColumn("lod4_multi_solid_id"));
		}
		adeHookTables = addJoinsToADEHookTables(TableEnum.PLANT_COVER, table);
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
						// create plant cover object
						int objectClassId = rs.getInt("objectclass_id");
						plantCover = exporter.createObject(objectClassId, PlantCover.class);
						if (plantCover == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, plantCoverId) + " as city furniture object.");
							continue;
						}

						featureType = exporter.getFeatureType(objectClassId);
					} else {
						plantCover = new PlantCover();
						featureType = exporter.getFeatureType(plantCover);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				cityObjectExporter.addBatch(plantCover, plantCoverId, featureType, projectionFilter);

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

					long geometryId = rs.getLong("lod" + lod + "_multi_surface_id");
					if (rs.wasNull())
						continue;

					switch (lod) {
						case 1:
							geometryExporter.addBatch(geometryId, plantCover::setLod1MultiSurface);
							break;
						case 2:
							geometryExporter.addBatch(geometryId, plantCover::setLod2MultiSurface);
							break;
						case 3:
							geometryExporter.addBatch(geometryId, plantCover::setLod3MultiSurface);
							break;
						case 4:
							geometryExporter.addBatch(geometryId, plantCover::setLod4MultiSurface);
							break;
					}
				}

				lodIterator.reset();
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!projectionFilter.containsProperty("lod" + lod + "MultiSolid", vegetationModule))
						continue;

					long geometryId = rs.getLong("lod" + lod + "_multi_solid_id");
					if (rs.wasNull())
						continue;

					switch (lod) {
						case 1:
							geometryExporter.addBatch(geometryId, plantCover::setLod1MultiSolid);
							break;
						case 2:
							geometryExporter.addBatch(geometryId, plantCover::setLod2MultiSolid);
							break;
						case 3:
							geometryExporter.addBatch(geometryId, plantCover::setLod3MultiSolid);
							break;
						case 4:
							geometryExporter.addBatch(geometryId, plantCover::setLod4MultiSolid);
							break;
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
