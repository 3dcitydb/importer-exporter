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
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBLandUse implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psLandUse;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private int batchCounter;

	public DBLandUse(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into LAND_USE (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD0_MULTI_SURFACE_ID, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psLandUse = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(LandUse landUse) throws SQLException {
		long landUseId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (landUseId != 0)
			success = insert(landUse, landUseId);

		if (success)
			return landUseId;
		else
			return 0;
	}

	private boolean insert(LandUse landUse, long landUseId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(landUse, landUseId, true);
		if (cityObjectId == 0)
			return false;

		// LandUse
		// ID
		psLandUse.setLong(1, landUseId);

		// luse:class
		if (landUse.isSetClazz() && landUse.getClazz().isSetValue()) {
			psLandUse.setString(2, landUse.getClazz().getValue());
			psLandUse.setString(3, landUse.getClazz().getCodeSpace());
		} else {
			psLandUse.setNull(2, Types.VARCHAR);
			psLandUse.setNull(3, Types.VARCHAR);
		}

		// luse:function
		if (landUse.isSetFunction()) {
			String[] function = Util.codeList2string(landUse.getFunction());
			psLandUse.setString(4, function[0]);
			psLandUse.setString(5, function[1]);
		} else {
			psLandUse.setNull(4, Types.VARCHAR);
			psLandUse.setNull(5, Types.VARCHAR);
		}

		// luse:usage
		if (landUse.isSetUsage()) {
			String[] usage = Util.codeList2string(landUse.getUsage());
			psLandUse.setString(6, usage[0]);
			psLandUse.setString(7, usage[1]);
		} else {
			psLandUse.setNull(6, Types.VARCHAR);
			psLandUse.setNull(7, Types.VARCHAR);
		}

		// Geometry
		for (int i = 0; i < 5; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = landUse.getLod0MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = landUse.getLod1MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = landUse.getLod2MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = landUse.getLod3MultiSurface();
				break;
			case 4:
				multiSurfaceProperty = landUse.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), landUseId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								landUseId, 
								TableEnum.LAND_USE, 
								"LOD" + i + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psLandUse.setLong(8 + i, multiGeometryId);
			else
				psLandUse.setNull(8 + i, Types.NULL);
		}

		psLandUse.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.LAND_USE);

		// insert local appearance
		cityObjectImporter.insertAppearance(landUse, landUseId);

		return true;
	}


	@Override
	public void executeBatch() throws SQLException {
		psLandUse.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psLandUse.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.LAND_USE;
	}

}
