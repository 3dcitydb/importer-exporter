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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.database.TableEnum;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBPlantCover implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psPlantCover;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private int batchCounter;

	public DBPlantCover(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into PLANT_COVER (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, AVERAGE_HEIGHT, AVERAGE_HEIGHT_UNIT, ")
		.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD1_MULTI_SOLID_ID, LOD2_MULTI_SOLID_ID, LOD3_MULTI_SOLID_ID, LOD4_MULTI_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psPlantCover = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(PlantCover plantCover) throws SQLException {
		long plantCoverId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (plantCoverId != 0)
			success = insert(plantCover, plantCoverId);

		if (success)
			return plantCoverId;
		else
			return 0;
	}

	private boolean insert(PlantCover plantCover, long plantCoverId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(plantCover, plantCoverId, true);
		if (cityObjectId == 0)
			return false;

		// PlantCover
		// ID
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
			String[] function = Util.codeList2string(plantCover.getFunction());
			psPlantCover.setString(4, function[0]);
			psPlantCover.setString(5, function[1]);
		} else {
			psPlantCover.setNull(4, Types.VARCHAR);
			psPlantCover.setNull(5, Types.VARCHAR);
		}

		// veg:usage
		if (plantCover.isSetUsage()) {
			String[] usage = Util.codeList2string(plantCover.getUsage());
			psPlantCover.setString(6, usage[0]);
			psPlantCover.setString(7, usage[1]);
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

		// Geometry
		// lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), plantCoverId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								plantCoverId, 
								TableEnum.PLANT_COVER, 
								"LOD" + (i + 1) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psPlantCover.setLong(10 + i, multiGeometryId);
			else
				psPlantCover.setNull(10 + i, Types.NULL);
		}

		// lodXMultiSolid
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
					solidGeometryId = surfaceGeometryImporter.insert(multiSolidProperty.getMultiSolid(), plantCoverId);
					multiSolidProperty.unsetMultiSolid();
				} else {
					// xlink
					String href = multiSolidProperty.getHref();
					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								plantCoverId, 
								TableEnum.PLANT_COVER, 
								"LOD" + (i + 1) + "_MULTI_SOLID_ID"));
					}
				}
			}

			if (solidGeometryId != 0)
				psPlantCover.setLong(14 + i, solidGeometryId);
			else
				psPlantCover.setNull(14 + i, Types.NULL);
		}

		psPlantCover.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.PLANT_COVER);

		// insert local appearance
		cityObjectImporter.insertAppearance(plantCover, plantCoverId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psPlantCover.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psPlantCover.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.PLANT_COVER;
	}

}
