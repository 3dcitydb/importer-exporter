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
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.citygml.waterbody.WaterClosureSurface;
import org.citygml4j.model.citygml.waterbody.WaterGroundSurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBWaterBody implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psWaterBody;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBOtherGeometry geometryExporter;

	private boolean useXLink;
	private boolean appendOldGmlId;
	private boolean keepOldGmlId;
	private String gmlIdPrefix;
	private String infoSys;
	
	private ProjectionPropertyFilter projectionFilter;

	public DBWaterBody(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.WATER_BODY);

		init();
	}

	private void init() throws SQLException {
		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();
		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			keepOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetKeepGmlIdAsExternalReference();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
			infoSys = config.getInternal().getExportFileName();
		}	

		if (!config.getInternal().isTransformCoordinates()) {		
			StringBuilder query = new StringBuilder()
			.append("select wb.CLASS, wb.CLASS_CODESPACE, wb.FUNCTION, wb.FUNCTION_CODESPACE, wb.USAGE, wb.USAGE_CODESPACE, ")
			.append("wb.LOD0_MULTI_CURVE, wb.LOD1_MULTI_CURVE, ")
			.append("wb.LOD0_MULTI_SURFACE_ID, wb.LOD1_MULTI_SURFACE_ID, ")
			.append("wb.LOD1_SOLID_ID, wb.LOD2_SOLID_ID, wb.LOD3_SOLID_ID, wb.LOD4_SOLID_ID, ")
			.append("ws.ID, ws.OBJECTCLASS_ID, ws.WATER_LEVEL, ws.WATER_LEVEL_CODESPACE, ")
			.append("ws.LOD2_SURFACE_ID, ws.LOD3_SURFACE_ID, ws.LOD4_SURFACE_ID ")
			.append("from WATERBODY wb left join WATERBOD_TO_WATERBND_SRF w2s on wb.ID=w2s.WATERBODY_ID left join WATERBOUNDARY_SURFACE ws on ws.ID=w2s.WATERBOUNDARY_SURFACE_ID where wb.ID=?");
			psWaterBody = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select wb.CLASS, wb.CLASS_CODESPACE, wb.FUNCTION, wb.FUNCTION_CODESPACE, wb.USAGE, wb.USAGE_CODESPACE, ")
			.append(transformOrNull).append("(wb.LOD0_MULTI_CURVE, ").append(srid).append(") AS LOD0_MULTI_CURVE, ")
			.append(transformOrNull).append("(wb.LOD1_MULTI_CURVE, ").append(srid).append(") AS LOD1_MULTI_CURVE, ")
			.append("wb.LOD0_MULTI_SURFACE_ID, wb.LOD1_MULTI_SURFACE_ID, ")
			.append("wb.LOD1_SOLID_ID, wb.LOD2_SOLID_ID, wb.LOD3_SOLID_ID, wb.LOD4_SOLID_ID, ")
			.append("ws.ID, ws.OBJECTCLASS_ID, ws.WATER_LEVEL, ws.WATER_LEVEL_CODESPACE, ")
			.append("ws.LOD2_SURFACE_ID, ws.LOD3_SURFACE_ID, ws.LOD4_SURFACE_ID ")
			.append("from WATERBODY wb left join WATERBOD_TO_WATERBND_SRF w2s on wb.ID=w2s.WATERBODY_ID left join WATERBOUNDARY_SURFACE ws on ws.ID=w2s.WATERBOUNDARY_SURFACE_ID where wb.ID=?");
			psWaterBody = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		WaterBody waterBody = new WaterBody();
		long waterBodyId = splitter.getPrimaryKey();

		// cityObject stuff
		boolean success = cityObjectExporter.read(waterBody, waterBodyId, true, projectionFilter);
		if (!success)
			return false;

		ResultSet rs = null;

		try {
			psWaterBody.setLong(1, waterBodyId);
			rs = psWaterBody.executeQuery();
			boolean waterBodyRead = false;

			while (rs.next()) {

				if (!waterBodyRead) {
					if (projectionFilter.pass(CityGMLModuleType.WATER_BODY, "class")) {
					String clazz = rs.getString(1);
					if (clazz != null) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString(2));
						waterBody.setClazz(code);
					}
					}

					if (projectionFilter.pass(CityGMLModuleType.WATER_BODY, "function")) {
					String function = rs.getString(3);
					String functionCodeSpace = rs.getString(4);
					if (function != null)
						waterBody.setFunction(Util.string2codeList(function, functionCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.WATER_BODY, "usage")) {
					String usage = rs.getString(5);
					String usageCodeSpace = rs.getString(6);
					if (usage != null)
						waterBody.setUsage(Util.string2codeList(usage, usageCodeSpace));
					}
					
					// multiCurve
					for (int lod = 0; lod < 2; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.WATER_BODY, new StringBuilder("lod").append(lod).append("MultiCurve").toString()))
							continue;
						
						Object multiCurveObj = rs.getObject(7 + lod);
						if (rs.wasNull() || multiCurveObj == null)
							continue;

						GeometryObject multiCurve = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
						if (multiCurve != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(multiCurve, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									waterBody.setLod0MultiCurve(multiCurveProperty);
									break;
								case 1:
									waterBody.setLod1MultiCurve(multiCurveProperty);
									break;
								}
							}
						}
					}
					
					// multiSurface
					for (int lod = 0; lod < 2; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.WATER_BODY, new StringBuilder("lod").append(lod).append("MultiSurface").toString()))
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
								waterBody.setLod0MultiSurface(multiSurfaceProperty);
								break;
							case 1:
								waterBody.setLod1MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}
					
					// solid
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.WATER_BODY, new StringBuilder("lod").append(lod + 1).append("Solid").toString()))
							continue;
						
						long surfaceGeometryId = rs.getLong(11 + lod);
						if (rs.wasNull() || surfaceGeometryId == 0)
							continue;

						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
						if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
							SolidProperty solidProperty = new SolidProperty();
							if (geometry.getAbstractGeometry() != null)
								solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
							else
								solidProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 0:
								waterBody.setLod1Solid(solidProperty);
								break;
							case 1:
								waterBody.setLod2Solid(solidProperty);
								break;
							case 2:
								waterBody.setLod3Solid(solidProperty);
								break;
							case 3:
								waterBody.setLod4Solid(solidProperty);
								break;
							}
						}
					}

					waterBodyRead = true;
				}

				// water boundary surfaces
				if (projectionFilter.filter(CityGMLModuleType.WATER_BODY, "boundedBy"))
					break;
				
				long waterBoundarySurfaceId = rs.getLong(15);
				if (rs.wasNull())
					continue;

				// create new water boundary object
				AbstractWaterBoundarySurface waterBoundarySurface = null;
				int classId = rs.getInt(16);
				if (rs.wasNull() || classId == 0)
					continue;

				CityGMLClass type = Util.classId2cityObject(classId);
				switch (type) {
				case WATER_SURFACE:
					waterBoundarySurface = new WaterSurface();
					break;
				case WATER_GROUND_SURFACE:
					waterBoundarySurface = new WaterGroundSurface();
					break;
				case WATER_CLOSURE_SURFACE:
					waterBoundarySurface = new WaterClosureSurface();
					break;
				default:
					continue;
				}

				// cityobject stuff
				cityObjectExporter.read(waterBoundarySurface, waterBoundarySurfaceId);

				if (waterBoundarySurface.isSetId()) {
					// process xlink
					if (dbExporterManager.lookupAndPutGmlId(waterBoundarySurface.getId(), waterBoundarySurfaceId, CityGMLClass.ABSTRACT_WATER_BOUNDARY_SURFACE)) {
						if (useXLink) {
							BoundedByWaterSurfaceProperty boundedByProperty = new BoundedByWaterSurfaceProperty();
							boundedByProperty.setHref("#" + waterBoundarySurface.getId());

							waterBody.addBoundedBySurface(boundedByProperty);
							continue;
						} else {
							String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
							if (appendOldGmlId)
								newGmlId += '-' + waterBoundarySurface.getId();

							if (keepOldGmlId) {
								ExternalReference externalReference = new ExternalReference();
								externalReference.setInformationSystem(infoSys);

								ExternalObject externalObject = new ExternalObject();
								externalObject.setName(waterBoundarySurface.getId());

								externalReference.setExternalObject(externalObject);
								waterBoundarySurface.addExternalReference(externalReference);
							}

							waterBoundarySurface.setId(newGmlId);
						}
					}
				}

				if (type == CityGMLClass.WATER_SURFACE) {
					String waterLevel = rs.getString(17);
					if (waterLevel != null) {
						Code code = new Code(waterLevel);
						code.setCodeSpace(rs.getString(18));
						((WaterSurface)waterBoundarySurface).setWaterLevel(code);
					}
				}
				
				// multiSurface
				for (int lod = 0; lod < 3; lod++) {
					long surfaceGeometryId = rs.getLong(19 + lod);
					if (rs.wasNull() || surfaceGeometryId == 0)
						continue;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
					if (geometry != null && geometry.getType().isInstance(GMLClass.ABSTRACT_SURFACE)) {
						SurfaceProperty surfaceProperty = new SurfaceProperty();
						if (geometry.getAbstractGeometry() != null)
							surfaceProperty.setSurface((AbstractSurface)geometry.getAbstractGeometry());
						else
							surfaceProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 0:
							waterBoundarySurface.setLod2Surface(surfaceProperty);
							break;
						case 1:
							waterBoundarySurface.setLod3Surface(surfaceProperty);
							break;
						case 2:
							waterBoundarySurface.setLod4Surface(surfaceProperty);
							break;
						}
					}
				}

				BoundedByWaterSurfaceProperty boundedByProperty = new BoundedByWaterSurfaceProperty();
				boundedByProperty.setObject(waterBoundarySurface);
				waterBody.addBoundedBySurface(boundedByProperty);
			}

			dbExporterManager.processFeature(waterBody);

			if (waterBody.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(waterBody.getId(), waterBodyId, waterBody.getCityGMLClass());

			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psWaterBody.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.WATERBODY;
	}

}
