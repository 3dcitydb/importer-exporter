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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.config.Config;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

public class DBLandUse implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psLandUse;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	
	private ProjectionPropertyFilter projectionFilter;

	public DBLandUse(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.LAND_USE);

		init();
	}

	private void init() throws SQLException {
		StringBuilder query = new StringBuilder()
		.append("select CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD0_MULTI_SURFACE_ID, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID ")
		.append("from LAND_USE where ID = ?");
		psLandUse = connection.prepareStatement(query.toString());

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		LandUse landUse = new LandUse();
		long landUseId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(landUse, landUseId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psLandUse.setLong(1, landUseId);
			rs = psLandUse.executeQuery();

			if (rs.next()) {
				if (projectionFilter.pass(CityGMLModuleType.LAND_USE, "class")) {
				String clazz = rs.getString(1);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(2));
					landUse.setClazz(code);
				}
				}

				if (projectionFilter.pass(CityGMLModuleType.LAND_USE, "function")) {
				String function = rs.getString(3);
				String functionCodeSpace = rs.getString(4);
				if (function != null)
					landUse.setFunction(Util.string2codeList(function, functionCodeSpace));
				}

				if (projectionFilter.pass(CityGMLModuleType.LAND_USE, "usage")) {
				String usage = rs.getString(5);
				String usageCodeSpace = rs.getString(6);
				if (usage != null)
					landUse.setUsage(Util.string2codeList(usage, usageCodeSpace));
				}
				
				// multiSurface
				for (int lod = 0; lod < 5; lod++) {
					if (projectionFilter.filter(CityGMLModuleType.LAND_USE, new StringBuilder("lod").append(lod).append("MultiSurface").toString()))
						continue;
					
					long surfaceGeometryId = rs.getLong(7 + lod);
					if (rs.wasNull() || surfaceGeometryId == 0)
						continue;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.getAbstractGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 0:
							landUse.setLod0MultiSurface(multiSurfaceProperty);
							break;
						case 1:
							landUse.setLod1MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							landUse.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							landUse.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 4:
							landUse.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}
			}

			dbExporterManager.processFeature(landUse);

			if (landUse.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(landUse.getId(), landUseId, landUse.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psLandUse.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.LAND_USE;
	}

}
