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
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBTrafficArea implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTrafficArea;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private int batchCounter;

	public DBTrafficArea(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into TRAFFIC_AREA (ID, OBJECTCLASS_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("SURFACE_MATERIAL, SURFACE_MATERIAL_CODESPACE, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("TRANSPORTATION_COMPLEX_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psTrafficArea = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(TrafficArea trafficArea, long parentId) throws SQLException {
		long trafficAreaId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (trafficAreaId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(trafficArea, trafficAreaId);

		// TrafficArea
		// ID
		psTrafficArea.setLong(1, trafficAreaId);

		// OBJECTCLASS_ID
		psTrafficArea.setLong(2, Util.cityObject2classId(trafficArea.getCityGMLClass()));

		// tran:class
		if (trafficArea.isSetClazz() && trafficArea.getClazz().isSetValue()) {
			psTrafficArea.setString(3, trafficArea.getClazz().getValue());
			psTrafficArea.setString(4, trafficArea.getClazz().getCodeSpace());
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// tran:function
		if (trafficArea.isSetFunction()) {
			String[] function = Util.codeList2string(trafficArea.getFunction());
			psTrafficArea.setString(5, function[0]);
			psTrafficArea.setString(6, function[1]);
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// tran:usage
		if (trafficArea.isSetUsage()) {
			String[] usage = Util.codeList2string(trafficArea.getUsage());
			psTrafficArea.setString(7, usage[0]);
			psTrafficArea.setString(8, usage[1]);
		} else {
			psTrafficArea.setNull(7, Types.VARCHAR);
			psTrafficArea.setNull(8, Types.VARCHAR);
		}

		// tran:surface material
		if (trafficArea.isSetSurfaceMaterial() && trafficArea.getSurfaceMaterial().isSetValue()) {
			psTrafficArea.setString(9, trafficArea.getSurfaceMaterial().getValue());
			psTrafficArea.setString(10, trafficArea.getSurfaceMaterial().getCodeSpace());
		} else {
			psTrafficArea.setNull(9, Types.VARCHAR);
			psTrafficArea.setNull(10, Types.VARCHAR);
		}

		// Geometry
		// lodXMultiSurface
		for (int i = 0; i < 3; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = trafficArea.getLod2MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = trafficArea.getLod3MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = trafficArea.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), trafficAreaId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								trafficAreaId, 
								TableEnum.TRAFFIC_AREA, 
								"LOD" + (i + 2) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTrafficArea.setLong(11 + i, multiGeometryId);
			else
				psTrafficArea.setNull(11 + i, Types.NULL);
		}

		// reference to transportation complex
		psTrafficArea.setLong(14, parentId);

		psTrafficArea.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TRAFFIC_AREA);

		// insert local appearance
		cityObjectImporter.insertAppearance(trafficArea, trafficAreaId);

		return trafficAreaId;
	}

	public long insert(AuxiliaryTrafficArea auxiliaryTrafficArea, long parentId) throws SQLException {
		long auxiliaryTrafficAreaId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (auxiliaryTrafficAreaId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(auxiliaryTrafficArea, auxiliaryTrafficAreaId);

		// TrafficArea
		// ID
		psTrafficArea.setLong(1, auxiliaryTrafficAreaId);

		// OBJECTCLASS_ID
		psTrafficArea.setLong(2, Util.cityObject2classId(auxiliaryTrafficArea.getCityGMLClass()));

		// class
		if (auxiliaryTrafficArea.isSetClazz() && auxiliaryTrafficArea.getClazz().isSetValue()) {
			psTrafficArea.setString(3, auxiliaryTrafficArea.getClazz().getValue());
			psTrafficArea.setString(4, auxiliaryTrafficArea.getClazz().getCodeSpace());
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// function
		if (auxiliaryTrafficArea.isSetFunction()) {
			String[] function = Util.codeList2string(auxiliaryTrafficArea.getFunction());
			psTrafficArea.setString(5, function[0]);
			psTrafficArea.setString(6, function[1]);
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// usage
		if (auxiliaryTrafficArea.isSetUsage()) {
			String[] usage = Util.codeList2string(auxiliaryTrafficArea.getUsage());
			psTrafficArea.setString(7, usage[0]);
			psTrafficArea.setString(8, usage[1]);
		} else {
			psTrafficArea.setNull(7, Types.VARCHAR);
			psTrafficArea.setNull(8, Types.VARCHAR);
		}

		// surface material
		if (auxiliaryTrafficArea.isSetSurfaceMaterial() && auxiliaryTrafficArea.getSurfaceMaterial().isSetValue()) {
			psTrafficArea.setString(9, auxiliaryTrafficArea.getSurfaceMaterial().getValue());
			psTrafficArea.setString(10, auxiliaryTrafficArea.getSurfaceMaterial().getCodeSpace());
		} else {
			psTrafficArea.setNull(9, Types.VARCHAR);
			psTrafficArea.setNull(10, Types.VARCHAR);
		}

		// lodXMultiSurface
		for (int i = 0; i < 3; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = auxiliaryTrafficArea.getLod2MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = auxiliaryTrafficArea.getLod3MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = auxiliaryTrafficArea.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), auxiliaryTrafficAreaId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								auxiliaryTrafficAreaId, 
								TableEnum.TRAFFIC_AREA, 
								"LOD" + (i + 2) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTrafficArea.setLong(11 + i, multiGeometryId);
			else
				psTrafficArea.setNull(11 + i, Types.NULL);
		}

		// reference to transportation complex
		psTrafficArea.setLong(14, parentId);

		psTrafficArea.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TRAFFIC_AREA);

		// insert local appearance
		cityObjectImporter.insertAppearance(auxiliaryTrafficArea, auxiliaryTrafficAreaId);

		return auxiliaryTrafficAreaId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTrafficArea.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTrafficArea.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TRAFFIC_AREA;
	}

}
