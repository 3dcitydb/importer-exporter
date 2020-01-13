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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBPlantCover implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psPlantCover;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private AttributeValueJoiner valueJoiner;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;

	public DBPlantCover(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".plant_cover (id, class, class_codespace, function, function_codespace, usage, usage_codespace, average_height, average_height_unit, " +
				"lod1_multi_surface_id, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id, " +
				"lod1_multi_solid_id, lod2_multi_solid_id, lod3_multi_solid_id, lod4_multi_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psPlantCover = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(PlantCover plantCover) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(plantCover);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long plantCoverId = cityObjectImporter.doImport(plantCover, featureType);

		// import plant cover information
		// primary id
		psPlantCover.setLong(1, plantCoverId);

		// veg:class
		if (plantCover.isSetClazz() && plantCover.getClazz().isSetValue()) {
			psPlantCover.setString(2, plantCover.getClazz().getValue());
			psPlantCover.setString(3, plantCover.getClazz().getCodeSpace());
		} else {
			psPlantCover.setNull(2, Types.VARCHAR);
			psPlantCover.setNull(3, Types.VARCHAR);
		}

		// veg:function
		if (plantCover.isSetFunction()) {
			valueJoiner.join(plantCover.getFunction(), Code::getValue, Code::getCodeSpace);
			psPlantCover.setString(4, valueJoiner.result(0));
			psPlantCover.setString(5, valueJoiner.result(1));
		} else {
			psPlantCover.setNull(4, Types.VARCHAR);
			psPlantCover.setNull(5, Types.VARCHAR);
		}

		// veg:usage
		if (plantCover.isSetUsage()) {
			valueJoiner.join(plantCover.getUsage(), Code::getValue, Code::getCodeSpace);
			psPlantCover.setString(6, valueJoiner.result(0));
			psPlantCover.setString(7, valueJoiner.result(1));
		} else {
			psPlantCover.setNull(6, Types.VARCHAR);
			psPlantCover.setNull(7, Types.VARCHAR);
		}

		// veg:averageHeight
		if (plantCover.isSetAverageHeight() && plantCover.getAverageHeight().isSetValue()) {
			psPlantCover.setDouble(8, plantCover.getAverageHeight().getValue());
			psPlantCover.setString(9, plantCover.getAverageHeight().getUom());
		} else {
			psPlantCover.setNull(8, Types.NULL);
			psPlantCover.setNull(9, Types.VARCHAR);
		}

		// veg:lodXMultiSurface
		for (int i = 0; i < 4; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = plantCover.getLod1MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = plantCover.getLod2MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = plantCover.getLod3MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = plantCover.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), plantCoverId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.PLANT_COVER.getName(),
								plantCoverId, 
								href, 
								"lod" + (i + 1) + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psPlantCover.setLong(10 + i, multiGeometryId);
			else
				psPlantCover.setNull(10 + i, Types.NULL);
		}

		// veg:lodXMultiSolid
		for (int i = 0; i < 4; i++) {
			MultiSolidProperty multiSolidProperty = null;
			long solidGeometryId = 0;

			switch (i) {
			case 0:
				multiSolidProperty = plantCover.getLod1MultiSolid();
				break;
			case 1:
				multiSolidProperty = plantCover.getLod2MultiSolid();
				break;
			case 2:
				multiSolidProperty = plantCover.getLod3MultiSolid();
				break;
			case 3:
				multiSolidProperty = plantCover.getLod4MultiSolid();
				break;
			}

			if (multiSolidProperty != null) {
				if (multiSolidProperty.isSetMultiSolid()) {
					solidGeometryId = surfaceGeometryImporter.doImport(multiSolidProperty.getMultiSolid(), plantCoverId);
					multiSolidProperty.unsetMultiSolid();
				} else {
					String href = multiSolidProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.PLANT_COVER.getName(),
								plantCoverId, 
								href, 
								"lod" + (i + 1) + "_multi_solid_id"));
					}
				}
			}

			if (solidGeometryId != 0)
				psPlantCover.setLong(14 + i, solidGeometryId);
			else
				psPlantCover.setNull(14 + i, Types.NULL);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psPlantCover.setLong(18, featureType.getObjectClassId());

		psPlantCover.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.PLANT_COVER);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(plantCover, plantCoverId, featureType);

		return plantCoverId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psPlantCover.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psPlantCover.close();
	}

}
