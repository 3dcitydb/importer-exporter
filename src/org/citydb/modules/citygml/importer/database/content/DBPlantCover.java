/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
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
