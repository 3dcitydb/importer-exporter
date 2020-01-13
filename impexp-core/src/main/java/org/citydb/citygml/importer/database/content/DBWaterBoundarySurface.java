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

import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBWaterBoundarySurface implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psWaterBoundarySurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBodToWaterBndSrf bodyToSurfaceImporter;
	private int batchCounter;

	public DBWaterBoundarySurface(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".waterboundary_surface (id, objectclass_id, water_level, water_level_codespace, " +
				"lod2_surface_id, lod3_surface_id, lod4_surface_id) values " +
				"(?, ?, ?, ?, ?, ?, ?)";
		psWaterBoundarySurface = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		bodyToSurfaceImporter = importer.getImporter(DBWaterBodToWaterBndSrf.class);
	}

	protected long doImport(AbstractWaterBoundarySurface waterBoundarySurface) throws CityGMLImportException, SQLException {
		return doImport(waterBoundarySurface, null, 0);
	}

	public long doImport(AbstractWaterBoundarySurface waterBoundarySurface, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(waterBoundarySurface);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long waterBoundarySurfaceId = cityObjectImporter.doImport(waterBoundarySurface, featureType);

		// import boundary surface information
		// primary id
		psWaterBoundarySurface.setLong(1, waterBoundarySurfaceId);

		// objectclass id
		psWaterBoundarySurface.setLong(2, featureType.getObjectClassId());

		// wtr:waterLevel
		if (waterBoundarySurface instanceof WaterSurface 
				&& ((WaterSurface)waterBoundarySurface).isSetWaterLevel() && ((WaterSurface)waterBoundarySurface).getWaterLevel().isSetValue()) {
			psWaterBoundarySurface.setString(3, ((WaterSurface)waterBoundarySurface).getWaterLevel().getValue());
			psWaterBoundarySurface.setString(4, ((WaterSurface)waterBoundarySurface).getWaterLevel().getCodeSpace());
		} else {
			psWaterBoundarySurface.setNull(3, Types.NULL);
			psWaterBoundarySurface.setNull(4, Types.VARCHAR);
		}

		// wtr:lodXMultiSurface
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
					surfaceGeometryId = surfaceGeometryImporter.doImport(surfaceProperty.getSurface(), waterBoundarySurfaceId);
					surfaceProperty.unsetSurface();
				} else {
					String href = surfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.WATERBOUNDARY_SURFACE.getName(),
								waterBoundarySurfaceId, 
								href, 
								"lod" + (i + 2) + "_surface_id"));
					}
				}
			}

			if (surfaceGeometryId != 0)
				psWaterBoundarySurface.setLong(5 + i, surfaceGeometryId);
			else
				psWaterBoundarySurface.setNull(5 + i, Types.NULL);
		}

		psWaterBoundarySurface.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.WATERBOUNDARY_SURFACE);

		// boundary surface to water body
		if (parent instanceof WaterBody)
			bodyToSurfaceImporter.doImport(waterBoundarySurfaceId, parentId);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(waterBoundarySurface, waterBoundarySurfaceId, featureType);

		return waterBoundarySurfaceId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psWaterBoundarySurface.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psWaterBoundarySurface.close();
	}

}
