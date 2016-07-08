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
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;

public class DBWaterBoundarySurface implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psWaterBoundarySurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBodToWaterBndSrf bodyToSurfaceImporter;

	private int batchCounter;

	public DBWaterBoundarySurface(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into WATERBOUNDARY_SURFACE (ID, OBJECTCLASS_ID, WATER_LEVEL, WATER_LEVEL_CODESPACE, ")
		.append("LOD2_SURFACE_ID, LOD3_SURFACE_ID, LOD4_SURFACE_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?)");
		psWaterBoundarySurface = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		bodyToSurfaceImporter = (DBWaterBodToWaterBndSrf)dbImporterManager.getDBImporter(DBImporterEnum.WATERBOD_TO_WATERBND_SRF);
	}

	public long insert(AbstractWaterBoundarySurface waterBoundarySurface, long parentId) throws SQLException {
		long waterBoundarySurfaceId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (waterBoundarySurfaceId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(waterBoundarySurface, waterBoundarySurfaceId);

		// BoundarySurface
		// ID
		psWaterBoundarySurface.setLong(1, waterBoundarySurfaceId);

		// OBJECTCLASS_ID
		psWaterBoundarySurface.setLong(2, Util.cityObject2classId(waterBoundarySurface.getCityGMLClass()));

		// wtr:waterLevel
		if (waterBoundarySurface.getCityGMLClass() == CityGMLClass.WATER_SURFACE && 
				((WaterSurface)waterBoundarySurface).isSetWaterLevel() && ((WaterSurface)waterBoundarySurface).getWaterLevel().isSetValue()) {
			psWaterBoundarySurface.setString(3, ((WaterSurface)waterBoundarySurface).getWaterLevel().getValue());
			psWaterBoundarySurface.setString(4, ((WaterSurface)waterBoundarySurface).getWaterLevel().getCodeSpace());
		} else {
			psWaterBoundarySurface.setNull(3, Types.NULL);
			psWaterBoundarySurface.setNull(4, Types.VARCHAR);
		}

		// Geometry
		// lodXMultiSurface
		for (int i = 0; i < 3; i++) {
			SurfaceProperty surfaceProperty = null;
			long surfaceGeometryId = 0;

			switch (i) {
			case 0:
				surfaceProperty = waterBoundarySurface.getLod2Surface();
				break;
			case 1:
				surfaceProperty = waterBoundarySurface.getLod3Surface();
				break;
			case 2:
				surfaceProperty = waterBoundarySurface.getLod4Surface();
				break;
			}

			if (surfaceProperty != null) {
				if (surfaceProperty.isSetSurface()) {
					surfaceGeometryId = surfaceGeometryImporter.insert(surfaceProperty.getSurface(), waterBoundarySurfaceId);
					surfaceProperty.unsetSurface();
				} else {
					// xlink
					String href = surfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								waterBoundarySurfaceId, 
								TableEnum.WATERBOUNDARY_SURFACE, 
								"LOD" + (i + 2) + "_SURFACE_ID"));
					}
				}
			}

			if (surfaceGeometryId != 0)
				psWaterBoundarySurface.setLong(5 + i, surfaceGeometryId);
			else
				psWaterBoundarySurface.setNull(5 + i, Types.NULL);
		}

		psWaterBoundarySurface.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.WATERBOUNDARY_SURFACE);

		// boundary surface to waterBody
		bodyToSurfaceImporter.insert(waterBoundarySurfaceId, parentId);

		// insert local appearance
		cityObjectImporter.insertAppearance(waterBoundarySurface, waterBoundarySurfaceId);

		return waterBoundarySurfaceId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psWaterBoundarySurface.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psWaterBoundarySurface.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.WATERBOUNDARY_SURFACE;
	}

}
