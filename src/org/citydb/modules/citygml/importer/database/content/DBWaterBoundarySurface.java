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

		// waterLevel
		if (waterBoundarySurface.getCityGMLClass() == CityGMLClass.WATER_SURFACE && 
				((WaterSurface)waterBoundarySurface).isSetWaterLevel() && ((WaterSurface)waterBoundarySurface).getWaterLevel().isSetValue()) {
			psWaterBoundarySurface.setString(3, ((WaterSurface)waterBoundarySurface).getWaterLevel().getValue());
			psWaterBoundarySurface.setString(4, ((WaterSurface)waterBoundarySurface).getWaterLevel().getCodeSpace());
		} else {
			psWaterBoundarySurface.setNull(3, Types.NULL);
			psWaterBoundarySurface.setNull(4, Types.VARCHAR);
		}

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
