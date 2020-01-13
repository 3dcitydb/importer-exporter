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
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBLandUse implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psLandUse;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private AttributeValueJoiner valueJoiner;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;

	public DBLandUse(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".land_use (id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"lod0_multi_surface_id, lod1_multi_surface_id, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psLandUse = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(LandUse landUse) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(landUse);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long landUseId = cityObjectImporter.doImport(landUse, featureType);

		// import land use information
		// primary id
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
			valueJoiner.join(landUse.getFunction(), Code::getValue, Code::getCodeSpace);
			psLandUse.setString(4, valueJoiner.result(0));
			psLandUse.setString(5, valueJoiner.result(1));
		} else {
			psLandUse.setNull(4, Types.VARCHAR);
			psLandUse.setNull(5, Types.VARCHAR);
		}

		// luse:usage
		if (landUse.isSetUsage()) {
			valueJoiner.join(landUse.getUsage(), Code::getValue, Code::getCodeSpace);
			psLandUse.setString(6, valueJoiner.result(0));
			psLandUse.setString(7, valueJoiner.result(1));
		} else {
			psLandUse.setNull(6, Types.VARCHAR);
			psLandUse.setNull(7, Types.VARCHAR);
		}

		// luse:lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), landUseId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.LAND_USE.getName(),
								landUseId, 
								href, 
								"lod" + i + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psLandUse.setLong(8 + i, multiGeometryId);
			else
				psLandUse.setNull(8 + i, Types.NULL);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psLandUse.setLong(13, featureType.getObjectClassId());

		psLandUse.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.LAND_USE);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(landUse, landUseId, featureType);

		return landUseId;
	}


	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psLandUse.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psLandUse.close();
	}

}
