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
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBTrafficArea implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psTrafficArea;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	public DBTrafficArea(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".traffic_area (id, objectclass_id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"surface_material, surface_material_codespace, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id, " +
				"transportation_complex_id) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		psTrafficArea = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(TrafficArea trafficArea) throws CityGMLImportException, SQLException {
		return doImport(trafficArea, 0);
	}

	protected long doImport(AuxiliaryTrafficArea auxiliaryTrafficArea) throws CityGMLImportException, SQLException {
		return doImport(auxiliaryTrafficArea, 0);
	}

	public long doImport(TrafficArea trafficArea, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(trafficArea);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long trafficAreaId = cityObjectImporter.doImport(trafficArea,  featureType);

		// import traffic area information
		// primary id
		psTrafficArea.setLong(1, trafficAreaId);

		// objectclass id
		psTrafficArea.setLong(2, featureType.getObjectClassId());

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
			valueJoiner.join(trafficArea.getFunction(), Code::getValue, Code::getCodeSpace);
			psTrafficArea.setString(5, valueJoiner.result(0));
			psTrafficArea.setString(6, valueJoiner.result(1));
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// tran:usage
		if (trafficArea.isSetUsage()) {
			valueJoiner.join(trafficArea.getUsage(), Code::getValue, Code::getCodeSpace);
			psTrafficArea.setString(7, valueJoiner.result(0));
			psTrafficArea.setString(8, valueJoiner.result(1));
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

		// tran:lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), trafficAreaId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.TRAFFIC_AREA.getName(),
								trafficAreaId, 
								href, 
								"lod" + (i + 2) + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTrafficArea.setLong(11 + i, multiGeometryId);
			else
				psTrafficArea.setNull(11 + i, Types.NULL);
		}

		// reference to transportation complex
		if (parentId != 0)
			psTrafficArea.setLong(14, parentId);
		else 
			psTrafficArea.setNull(14, Types.NULL);

		psTrafficArea.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TRAFFIC_AREA);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(trafficArea, trafficAreaId, featureType);

		return trafficAreaId;
	}

	public long doImport(AuxiliaryTrafficArea auxiliaryTrafficArea, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(auxiliaryTrafficArea);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long auxiliaryTrafficAreaId = cityObjectImporter.doImport(auxiliaryTrafficArea, featureType);

		// import auxiliary traffic area information
		// primary id
		psTrafficArea.setLong(1, auxiliaryTrafficAreaId);

		// objectclass id
		psTrafficArea.setLong(2, featureType.getObjectClassId());

		// tran:class
		if (auxiliaryTrafficArea.isSetClazz() && auxiliaryTrafficArea.getClazz().isSetValue()) {
			psTrafficArea.setString(3, auxiliaryTrafficArea.getClazz().getValue());
			psTrafficArea.setString(4, auxiliaryTrafficArea.getClazz().getCodeSpace());
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// tran:function
		if (auxiliaryTrafficArea.isSetFunction()) {
			valueJoiner.join(auxiliaryTrafficArea.getFunction(), Code::getValue, Code::getCodeSpace);
			psTrafficArea.setString(5, valueJoiner.result(0));
			psTrafficArea.setString(6, valueJoiner.result(1));
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// tran:usage
		if (auxiliaryTrafficArea.isSetUsage()) {
			valueJoiner.join(auxiliaryTrafficArea.getUsage(), Code::getValue, Code::getCodeSpace);
			psTrafficArea.setString(7, valueJoiner.result(0));
			psTrafficArea.setString(8, valueJoiner.result(1));
		} else {
			psTrafficArea.setNull(7, Types.VARCHAR);
			psTrafficArea.setNull(8, Types.VARCHAR);
		}

		// tran:surface material
		if (auxiliaryTrafficArea.isSetSurfaceMaterial() && auxiliaryTrafficArea.getSurfaceMaterial().isSetValue()) {
			psTrafficArea.setString(9, auxiliaryTrafficArea.getSurfaceMaterial().getValue());
			psTrafficArea.setString(10, auxiliaryTrafficArea.getSurfaceMaterial().getCodeSpace());
		} else {
			psTrafficArea.setNull(9, Types.VARCHAR);
			psTrafficArea.setNull(10, Types.VARCHAR);
		}

		// tran:lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), auxiliaryTrafficAreaId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.TRAFFIC_AREA.getName(),
								auxiliaryTrafficAreaId, 
								href, 
								"lod" + (i + 2) + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTrafficArea.setLong(11 + i, multiGeometryId);
			else
				psTrafficArea.setNull(11 + i, Types.NULL);
		}

		// reference to transportation complex
		if (parentId != 0)
			psTrafficArea.setLong(14, parentId);
		else
			psTrafficArea.setNull(14, Types.NULL);

		psTrafficArea.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TRAFFIC_AREA);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(auxiliaryTrafficArea, auxiliaryTrafficAreaId, featureType);

		return auxiliaryTrafficAreaId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psTrafficArea.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psTrafficArea.close();
	}

}
