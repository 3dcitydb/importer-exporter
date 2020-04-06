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

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBWaterBody implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psWaterBody;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBoundarySurface boundarySurfaceImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;

	private int batchCounter;
	private boolean hasObjectClassIdColumn;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBWaterBody(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".waterbody (id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"lod0_multi_curve, lod1_multi_curve, lod0_multi_surface_id, lod1_multi_surface_id, " +
				"lod1_solid_id, lod2_solid_id, lod3_solid_id, lod4_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psWaterBody = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		boundarySurfaceImporter = importer.getImporter(DBWaterBoundarySurface.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(WaterBody waterBody) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(waterBody);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long waterBodyId = cityObjectImporter.doImport(waterBody, featureType);

		// import water body information
		// primary id
		psWaterBody.setLong(1, waterBodyId);

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
			valueJoiner.join(waterBody.getFunction(), Code::getValue, Code::getCodeSpace);
			psWaterBody.setString(4, valueJoiner.result(0));
			psWaterBody.setString(5, valueJoiner.result(1));
		} else {
			psWaterBody.setNull(4, Types.VARCHAR);
			psWaterBody.setNull(5, Types.VARCHAR);
		}

		// wtr:usage
		if (waterBody.isSetUsage()) {
			valueJoiner.join(waterBody.getUsage(), Code::getValue, Code::getCodeSpace);
			psWaterBody.setString(6, valueJoiner.result(0));
			psWaterBody.setString(7, valueJoiner.result(1));
		} else {
			psWaterBody.setNull(6, Types.VARCHAR);
			psWaterBody.setNull(7, Types.VARCHAR);
		}

		// wtr:lodXMultiCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psWaterBody.setObject(8 + i, multiLineObj);
			} else
				psWaterBody.setNull(8 + i, nullGeometryType, nullGeometryTypeName);
		}

		// wtr:lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), waterBodyId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.WATERBODY.getName(),
								waterBodyId, 
								href, 
								"lod" + i + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psWaterBody.setLong(10 + i, multiGeometryId);
			else
				psWaterBody.setNull(10 + i, Types.NULL);
		}

		// wtr:lodXSolid
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
					solidGeometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), waterBodyId);
					solidProperty.unsetSolid();
				} else {
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.WATERBODY.getName(),
								waterBodyId, 
								href, 
								"lod" + (i + 1) + "_solid_id"));
					}
				}
			}

			if (solidGeometryId != 0)
				psWaterBody.setLong(12 + i, solidGeometryId);
			else
				psWaterBody.setNull(12 + i, Types.NULL);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psWaterBody.setLong(16, featureType.getObjectClassId());

		psWaterBody.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.WATERBODY);

		// wtr:boundedBy
		if (waterBody.isSetBoundedBySurface()) {
			for (BoundedByWaterSurfaceProperty property : waterBody.getBoundedBySurface()) {
				AbstractWaterBoundarySurface boundarySurface = property.getWaterBoundarySurface();

				if (boundarySurface != null) {
					boundarySurfaceImporter.doImport(boundarySurface, waterBody, waterBodyId);
					property.unsetWaterBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.WATERBOD_TO_WATERBND_SRF.getName(),
								waterBodyId,
								"WATERBODY_ID",
								href,
								"WATERBOUNDARY_SURFACE_ID"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(waterBody, waterBodyId, featureType);

		return waterBodyId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psWaterBody.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psWaterBody.close();
	}

}
