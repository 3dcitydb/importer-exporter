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

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBWaterBody implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psWaterBody;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBoundarySurface boundarySurfaceImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBWaterBody(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into WATERBODY (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD0_MULTI_CURVE, LOD1_MULTI_CURVE, LOD0_MULTI_SURFACE_ID, LOD1_MULTI_SURFACE_ID, ")
		.append("LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psWaterBody = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		boundarySurfaceImporter = (DBWaterBoundarySurface)dbImporterManager.getDBImporter(DBImporterEnum.WATERBOUNDARY_SURFACE);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(WaterBody waterBody) throws SQLException {
		long waterBodyId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (waterBodyId != 0)
			success = insert(waterBody, waterBodyId);

		if (success)
			return waterBodyId;
		else
			return 0;
	}

	private boolean insert(WaterBody waterBody, long waterBodyId) throws SQLException {
		String origGmlId = waterBody.getId();

		// CityObject
		long cityObjectId = cityObjectImporter.insert(waterBody, waterBodyId, true);
		if (cityObjectId == 0)
			return false;

		// CityFurniture
		// ID
		psWaterBody.setLong(1, cityObjectId);

		// wtr:class
		if (waterBody.isSetClazz() && waterBody.getClazz().isSetValue()) {
			psWaterBody.setString(2, waterBody.getClazz().getValue());
			psWaterBody.setString(3, waterBody.getClazz().getCodeSpace());
		} else {
			psWaterBody.setNull(2, Types.VARCHAR);
			psWaterBody.setNull(3, Types.VARCHAR);
		}

		// wtr:function
		if (waterBody.isSetFunction()) {
			String[] function = Util.codeList2string(waterBody.getFunction());
			psWaterBody.setString(4, function[0]);
			psWaterBody.setString(5, function[1]);
		} else {
			psWaterBody.setNull(4, Types.VARCHAR);
			psWaterBody.setNull(5, Types.VARCHAR);
		}

		// wtr:usage
		if (waterBody.isSetUsage()) {
			String[] usage = Util.codeList2string(waterBody.getUsage());
			psWaterBody.setString(6, usage[0]);
			psWaterBody.setString(7, usage[1]);
		} else {
			psWaterBody.setNull(6, Types.VARCHAR);
			psWaterBody.setNull(7, Types.VARCHAR);
		}

		// Geometry
		// lodXMultiCurve
		for (int i = 0; i < 2; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = waterBody.getLod0MultiCurve();
				break;
			case 1:
				multiCurveProperty = waterBody.getLod1MultiCurve();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psWaterBody.setObject(8 + i, multiLineObj);
			} else
				psWaterBody.setNull(8 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXMultiSurface
		for (int i = 0; i < 2; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = waterBody.getLod0MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = waterBody.getLod1MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), waterBodyId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								waterBodyId, 
								TableEnum.WATERBODY, 
								"LOD" + i + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psWaterBody.setLong(10 + i, multiGeometryId);
			else
				psWaterBody.setNull(10 + i, Types.NULL);
		}

		// lodXSolid
		for (int i = 0; i < 4; i++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (i) {
			case 0:
				solidProperty = waterBody.getLod1Solid();
				break;
			case 1:
				solidProperty = waterBody.getLod2Solid();
				break;
			case 2:
				solidProperty = waterBody.getLod3Solid();
				break;
			case 3:
				solidProperty = waterBody.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), waterBodyId);
					solidProperty.unsetSolid();
				} else {
					// xlink
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								waterBodyId, 
								TableEnum.WATERBODY, 
								"LOD" + (i + 1) + "_SOLID_ID"));
					}
				}
			}

			if (solidGeometryId != 0)
				psWaterBody.setLong(12 + i, solidGeometryId);
			else
				psWaterBody.setNull(12 + i, Types.NULL);
		}

		psWaterBody.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.WATERBODY);

		// boundary surfaces
		if (waterBody.isSetBoundedBySurface()) {
			for (BoundedByWaterSurfaceProperty waterSurfaceProperty : waterBody.getBoundedBySurface()) {
				AbstractWaterBoundarySurface boundarySurface = waterSurfaceProperty.getWaterBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = boundarySurfaceImporter.insert(boundarySurface, waterBodyId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								CityGMLClass.WATER_BODY, 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					waterSurfaceProperty.unsetWaterBoundarySurface();
				} else {
					// xlink
					String href = waterSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								waterBodyId,
								TableEnum.WATERBODY,
								href,
								TableEnum.WATERBOUNDARY_SURFACE
								));
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(waterBody, waterBodyId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psWaterBody.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psWaterBody.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.WATERBODY;
	}
}
