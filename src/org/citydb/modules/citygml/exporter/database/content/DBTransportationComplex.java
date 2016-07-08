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

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.Railway;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.Square;
import org.citygml4j.model.citygml.transportation.Track;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

public class DBTransportationComplex implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTranComplex;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;

	private ProjectionPropertyFilter projectionFilter;

	public DBTransportationComplex(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.TRANSPORTATION_COMPLEX);
		projectionFilter.combine(exportFilter.getProjectionPropertyFilter(CityGMLClass.ROAD));
		projectionFilter.combine(exportFilter.getProjectionPropertyFilter(CityGMLClass.RAILWAY));
		projectionFilter.combine(exportFilter.getProjectionPropertyFilter(CityGMLClass.SQUARE));
		projectionFilter.combine(exportFilter.getProjectionPropertyFilter(CityGMLClass.TRACK));

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select tc.CLASS as TC_CLASS, tc.CLASS_CODESPACE as TC_CLASS_CODESPACE, tc.FUNCTION as TC_FUNCTION, tc.FUNCTION_CODESPACE as TC_FUNCTION_CODESPACE, tc.USAGE as TC_USAGE, tc.USAGE_CODESPACE as TC_USAGE_CODESPACE, ")
			.append("tc.LOD0_NETWORK, ")
			.append("tc.LOD1_MULTI_SURFACE_ID as TC_LOD1_MULTI_SURFACE_ID, tc.LOD2_MULTI_SURFACE_ID as TC_LOD2_MULTI_SURFACE_ID, tc.LOD3_MULTI_SURFACE_ID as TC_LOD3_MULTI_SURFACE_ID, tc.LOD4_MULTI_SURFACE_ID as TC_LOD4_MULTI_SURFACE_ID,  ")
			.append("ta.ID as TA_ID, ta.OBJECTCLASS_ID, ta.CLASS as TA_CLASS, ta.CLASS_CODESPACE as TA_CLASS_CODESPACE, ta.FUNCTION as TA_FUNCTION, ta.FUNCTION_CODESPACE as TA_FUNCTION_CODESPACE, ta.USAGE as TA_USAGE, ta.USAGE_CODESPACE as TA_USAGE_CODESPACE, ")
			.append("ta.SURFACE_MATERIAL, ta.SURFACE_MATERIAL_CODESPACE, ")
			.append("ta.LOD2_MULTI_SURFACE_ID as TA_LOD2_MULTI_SURFACE_ID, ta.LOD3_MULTI_SURFACE_ID as TA_LOD3_MULTI_SURFACE_ID, ta.LOD4_MULTI_SURFACE_ID as TA_LOD4_MULTI_SURFACE_ID ")
			.append("from TRANSPORTATION_COMPLEX tc left join TRAFFIC_AREA ta on tc.ID=ta.TRANSPORTATION_COMPLEX_ID where tc.ID=?");
			psTranComplex = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select tc.CLASS as TC_CLASS, tc.CLASS_CODESPACE as TC_CLASS_CODESPACE, tc.FUNCTION as TC_FUNCTION, tc.FUNCTION_CODESPACE as TC_FUNCTION_CODESPACE, tc.USAGE as TC_USAGE, tc.USAGE_CODESPACE as TC_USAGE_CODESPACE, ")
			.append(transformOrNull).append("(tc.LOD0_NETWORK, ").append(srid).append(") as LOD0_NETWORK, ")
			.append("tc.LOD1_MULTI_SURFACE_ID as TC_LOD1_MULTI_SURFACE_ID, tc.LOD2_MULTI_SURFACE_ID as TC_LOD2_MULTI_SURFACE_ID, tc.LOD3_MULTI_SURFACE_ID as TC_LOD3_MULTI_SURFACE_ID, tc.LOD4_MULTI_SURFACE_ID as TC_LOD4_MULTI_SURFACE_ID,  ")
			.append("ta.ID as TA_ID, ta.OBJECTCLASS_ID, ta.CLASS as TA_CLASS, ta.CLASS_CODESPACE as TA_CLASS_CODESPACE, ta.FUNCTION as TA_FUNCTION, ta.FUNCTION_CODESPACE as TA_FUNCTION_CODESPACE, ta.USAGE as TA_USAGE, ta.USAGE_CODESPACE as TA_USAGE_CODESPACE, ")
			.append("ta.SURFACE_MATERIAL, ta.SURFACE_MATERIAL_CODESPACE, ")
			.append("ta.LOD2_MULTI_SURFACE_ID as TA_LOD2_MULTI_SURFACE_ID, ta.LOD3_MULTI_SURFACE_ID as TA_LOD3_MULTI_SURFACE_ID, ta.LOD4_MULTI_SURFACE_ID as TA_LOD4_MULTI_SURFACE_ID ")
			.append("from TRANSPORTATION_COMPLEX tc left join TRAFFIC_AREA ta on tc.ID=ta.TRANSPORTATION_COMPLEX_ID where tc.ID=?");
			psTranComplex = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		TransportationComplex transComplex = null;
		long transComplexId = splitter.getPrimaryKey();

		switch (splitter.getCityObjectType()) {
		case ROAD:
			transComplex = new Road();
			break;
		case RAILWAY:
			transComplex = new Railway();
			break;
		case SQUARE:
			transComplex = new Square();
			break;
		case TRACK:
			transComplex = new Track();
			break;
		default:
			transComplex = new TransportationComplex();
		}

		// cityObject stuff
		boolean success = cityObjectExporter.read(transComplex, transComplexId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psTranComplex.setLong(1, transComplexId);
			rs = psTranComplex.executeQuery();

			boolean isInited = false;

			while (rs.next()) {
				if (!isInited) {
					if (projectionFilter.pass(CityGMLModuleType.TRANSPORTATION, "class")) {
						String clazz = rs.getString(1);
						if (clazz != null) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString(2));
							transComplex.setClazz(code);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.TRANSPORTATION, "function")) {
						String function = rs.getString(3);
						String functionCodeSpace = rs.getString(4);
						if (function != null)
							transComplex.setFunction(Util.string2codeList(function, functionCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.TRANSPORTATION, "usage")) {
						String usage = rs.getString(5);
						String usageCodeSpace = rs.getString(6);
						if (usage != null)
							transComplex.setUsage(Util.string2codeList(usage, usageCodeSpace));
					}

					// lod0Network
					if (projectionFilter.pass(CityGMLModuleType.TRANSPORTATION, "lod0Network")) {
						Object object = rs.getObject(7);
						if (!rs.wasNull() && object != null) {
							GeometryObject lod0Network = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(object);
							GeometricComplexProperty complexProperty = geometryExporter.getPointOrCurveComplexProperty(lod0Network, false);
							transComplex.addLod0Network(complexProperty);
						}
					}

					// multiSurface
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.TRANSPORTATION, new StringBuilder("lod").append(lod + 1).append("MultiSurface").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(8 + lod);
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
								transComplex.setLod1MultiSurface(multiSurfaceProperty);
								break;
							case 1:
								transComplex.setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 2:
								transComplex.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								transComplex.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}					

					isInited = true;
				}

				long trafficAreaId = rs.getLong(12);
				if (rs.wasNull())
					continue;

				AbstractTransportationObject transObject = null;
				int classId = rs.getInt(13);
				if (rs.wasNull() || classId == 0)
					continue;

				CityGMLClass type = Util.classId2cityObject(classId);
				switch (type) {
				case TRAFFIC_AREA:
					if (projectionFilter.pass(CityGMLModuleType.TRANSPORTATION, "trafficArea"))
						transObject = new TrafficArea();
					break;
				case AUXILIARY_TRAFFIC_AREA:
					if (projectionFilter.pass(CityGMLModuleType.TRANSPORTATION, "auxiliaryTrafficArea"))
						transObject = new AuxiliaryTrafficArea();
					break;
				default:
					continue;
				}

				if (transObject == null)
					continue;

				// cityobject stuff
				cityObjectExporter.read(transObject, trafficAreaId);

				String clazz = rs.getString(14);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(15));
					if (type == CityGMLClass.TRAFFIC_AREA)
						((TrafficArea)transObject).setClazz(code);
					else
						((AuxiliaryTrafficArea)transObject).setClazz(code);
				}

				String function = rs.getString(16);
				String functionCodeSpace = rs.getString(17);
				if (function != null) {
					if (type == CityGMLClass.TRAFFIC_AREA)
						((TrafficArea)transObject).setFunction(Util.string2codeList(function, functionCodeSpace));
					else
						((AuxiliaryTrafficArea)transObject).setFunction(Util.string2codeList(function, functionCodeSpace));
				}

				String usage = rs.getString(18);
				String usageCodeSpace = rs.getString(19);
				if (usage != null) {
					if (type == CityGMLClass.TRAFFIC_AREA)
						((TrafficArea)transObject).setUsage(Util.string2codeList(usage, usageCodeSpace));
					else
						((AuxiliaryTrafficArea)transObject).setUsage(Util.string2codeList(usage, usageCodeSpace));
				}

				String surfaceMaterial = rs.getString(20);
				if (surfaceMaterial != null) {
					Code code = new Code(surfaceMaterial);
					code.setCodeSpace(rs.getString(21));
					if (type == CityGMLClass.TRAFFIC_AREA)
						((TrafficArea)transObject).setSurfaceMaterial(code);
					else
						((AuxiliaryTrafficArea)transObject).setSurfaceMaterial(code);
				}

				// multiSurface
				for (int lod = 0; lod < 3; lod++) {
					long surfaceGeometryId = rs.getLong(22 + lod);
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
							if (type == CityGMLClass.TRAFFIC_AREA)
								((TrafficArea)transObject).setLod2MultiSurface(multiSurfaceProperty);
							else
								((AuxiliaryTrafficArea)transObject).setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 1:
							if (type == CityGMLClass.TRAFFIC_AREA)
								((TrafficArea)transObject).setLod3MultiSurface(multiSurfaceProperty);
							else
								((AuxiliaryTrafficArea)transObject).setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							if (type == CityGMLClass.TRAFFIC_AREA)
								((TrafficArea)transObject).setLod4MultiSurface(multiSurfaceProperty);
							else
								((AuxiliaryTrafficArea)transObject).setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				if (type == CityGMLClass.TRAFFIC_AREA) {
					TrafficAreaProperty trafficProperty = new TrafficAreaProperty();
					trafficProperty.setObject((TrafficArea)transObject);
					transComplex.addTrafficArea(trafficProperty);
				} else {
					AuxiliaryTrafficAreaProperty auxProperty  = new AuxiliaryTrafficAreaProperty();
					auxProperty.setObject((AuxiliaryTrafficArea)transObject);
					transComplex.addAuxiliaryTrafficArea(auxProperty);
				}
			}

			dbExporterManager.processFeature(transComplex);

			if (transComplex.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(transComplex.getId(), transComplexId, transComplex.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTranComplex.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TRANSPORTATION_COMPLEX;
	}

}
