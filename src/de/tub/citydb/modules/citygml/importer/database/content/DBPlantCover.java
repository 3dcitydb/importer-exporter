/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

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
		psPlantCover = batchConn.prepareStatement("insert into PLANT_COVER (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, " +
				"AVERAGE_HEIGHT, LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(PlantCover plantCover) throws SQLException {
		long plantCoverId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
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

		// gml:name
		if (plantCover.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(plantCover);

			psPlantCover.setString(2, dbGmlName[0]);
			psPlantCover.setString(3, dbGmlName[1]);
		} else {
			psPlantCover.setNull(2, Types.VARCHAR);
			psPlantCover.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (plantCover.isSetDescription()) {
			String description = plantCover.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psPlantCover.setString(4, description);
		} else {
			psPlantCover.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (plantCover.isSetClazz())
			psPlantCover.setString(5, plantCover.getClazz().trim());
		else
			psPlantCover.setNull(5, Types.VARCHAR);

		// citygml:function
		if (plantCover.isSetFunction()) {
			psPlantCover.setString(6, Util.collection2string(plantCover.getFunction(), " "));
		} else {
			psPlantCover.setNull(6, Types.VARCHAR);
		}

		// average height
		if (plantCover.isSetAverageHeight() && plantCover.getAverageHeight().isSetValue()) {
			psPlantCover.setDouble(7, plantCover.getAverageHeight().getValue());
		} else {
			psPlantCover.setNull(7, Types.DOUBLE);
		}

		// Geometry
		// lodXMultiSurface
		boolean[] lodGeometry = new boolean[4];

		for (int lod = 1; lod < 5; lod++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (lod) {
			case 1:
				multiSurfaceProperty = plantCover.getLod1MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = plantCover.getLod2MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = plantCover.getLod3MultiSurface();
				break;
			case 4:
				multiSurfaceProperty = plantCover.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), plantCoverId);
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						plantCoverId,
        						TableEnum.PLANT_COVER,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_GEOMETRY_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 1:
				if (multiSurfaceId != 0) {
					psPlantCover.setLong(8, multiSurfaceId);
					lodGeometry[0] = true;
				}
				else
					psPlantCover.setNull(8, 0);
				break;
			case 2:
				if (multiSurfaceId != 0) {
					psPlantCover.setLong(9, multiSurfaceId);
					lodGeometry[1] = true;
				}
				else
					psPlantCover.setNull(9, 0);
				break;
			case 3:
				if (multiSurfaceId != 0) {
					psPlantCover.setLong(10, multiSurfaceId);
					lodGeometry[2] = true;
				}
				else
					psPlantCover.setNull(10, 0);
				break;
			case 4:
				if (multiSurfaceId != 0) {
					psPlantCover.setLong(11, multiSurfaceId);
					lodGeometry[3] = true;
				}
				else
					psPlantCover.setNull(11, 0);
				break;
			}
		}

		// MultiSolid
		for (int lod = 1; lod < 5; lod++) {
			if (lodGeometry[lod - 1])
				continue;

			MultiSolidProperty multiSolidProperty = null;
			long multiSolidGeometryId = 0;

			switch (lod) {
			case 1:
				multiSolidProperty = plantCover.getLod1MultiSolid();
				break;
			case 2:
				multiSolidProperty = plantCover.getLod2MultiSolid();
				break;
			case 3:
				multiSolidProperty = plantCover.getLod3MultiSolid();
				break;
			case 4:
				multiSolidProperty = null;
				break;
			}

			if (multiSolidProperty != null) {
				if (multiSolidProperty.isSetMultiSolid()) {
					multiSolidGeometryId = surfaceGeometryImporter.insert(multiSolidProperty.getMultiSolid(), plantCoverId);
				} else {
					// xlink
					String href = multiSolidProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						plantCoverId,
        						TableEnum.PLANT_COVER,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_GEOMETRY_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 1:
				if (multiSolidGeometryId != 0)
					psPlantCover.setLong(8, multiSolidGeometryId);
				else
					psPlantCover.setNull(8, 0);
				break;
			case 2:
				if (multiSolidGeometryId != 0)
					psPlantCover.setLong(9, multiSolidGeometryId);
				else
					psPlantCover.setNull(9, 0);
				break;
			case 3:
				if (multiSolidGeometryId != 0)
					psPlantCover.setLong(10, multiSolidGeometryId);
				else
					psPlantCover.setNull(10, 0);
				break;
			case 4:
				psPlantCover.setNull(11, 0);
				break;
			}
		}

		psPlantCover.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.PLANT_COVER);

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
