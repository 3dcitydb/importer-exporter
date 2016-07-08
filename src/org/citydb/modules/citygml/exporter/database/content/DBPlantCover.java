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
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

public class DBPlantCover implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psPlantCover;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	
	private ProjectionPropertyFilter projectionFilter;

	public DBPlantCover(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.PLANT_COVER);

		init();
	}

	private void init() throws SQLException {
		StringBuilder query = new StringBuilder()
		.append("select CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, AVERAGE_HEIGHT, AVERAGE_HEIGHT_UNIT, ")
		.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD1_MULTI_SOLID_ID, LOD2_MULTI_SOLID_ID, LOD3_MULTI_SOLID_ID, LOD4_MULTI_SOLID_ID ")
		.append("from PLANT_COVER where ID = ?");
		psPlantCover = connection.prepareStatement(query.toString());

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		PlantCover plantCover = new PlantCover();
		long plantCoverId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(plantCover, plantCoverId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psPlantCover.setLong(1, plantCoverId);
			rs = psPlantCover.executeQuery();

			if (rs.next()) {
				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "class")) {
				String clazz = rs.getString(1);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(2));
					plantCover.setClazz(code);
				}
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "function")) {
				String function = rs.getString(3);
				String functionCodeSpace = rs.getString(4);
				if (function != null)
					plantCover.setFunction(Util.string2codeList(function, functionCodeSpace));
				}

				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "usage")) {
				String usage = rs.getString(5);
				String usageCodeSpace = rs.getString(6);
				if (usage != null)
					plantCover.setUsage(Util.string2codeList(usage, usageCodeSpace));
				}
				
				if (projectionFilter.pass(CityGMLModuleType.VEGETATION, "averageHeight")) {
				double averageHeight = rs.getDouble(7);
				if (!rs.wasNull()) {
					Length length = new Length();
					length.setValue(averageHeight);
					length.setUom(rs.getString(8));
					plantCover.setAverageHeight(length);
				}
				}

				// multiSurface
				for (int lod = 0; lod < 4; lod++) {
					if (projectionFilter.filter(CityGMLModuleType.VEGETATION, new StringBuilder("lod").append(lod + 1).append("MultiSurface").toString()))
						continue;
					
					long surfaceGeometryId = rs.getLong(9 + lod);
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
							plantCover.setLod1MultiSurface(multiSurfaceProperty);
							break;
						case 1:
							plantCover.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							plantCover.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							plantCover.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}
				
				// solid
				for (int lod = 0; lod < 4; lod++) {
					if (projectionFilter.filter(CityGMLModuleType.VEGETATION, new StringBuilder("lod").append(lod + 1).append("MultiSolid").toString()))
						continue;
					
					long surfaceGeometryId = rs.getLong(13 + lod);
					if (rs.wasNull() || surfaceGeometryId == 0)
						continue;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SOLID) {
						MultiSolidProperty multiSolidProperty = new MultiSolidProperty();
						if (geometry.getAbstractGeometry() != null)
							multiSolidProperty.setMultiSolid((MultiSolid)geometry.getAbstractGeometry());
						else
							multiSolidProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 0:
							plantCover.setLod1MultiSolid(multiSolidProperty);
							break;
						case 1:
							plantCover.setLod2MultiSolid(multiSolidProperty);
							break;
						case 2:
							plantCover.setLod3MultiSolid(multiSolidProperty);
							break;
						case 3:
							plantCover.setLod4MultiSolid(multiSolidProperty);
							break;
						}
					}
				}
			}

			dbExporterManager.processFeature(plantCover);

			if (plantCover.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(plantCover.getId(), plantCoverId, plantCover.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psPlantCover.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.PLANT_COVER;
	}

}
