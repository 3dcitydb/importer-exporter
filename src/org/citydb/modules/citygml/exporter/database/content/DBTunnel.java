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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.Tunnel;
import org.citygml4j.model.citygml.tunnel.TunnelPart;
import org.citygml4j.model.citygml.tunnel.TunnelPartProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

public class DBTunnel implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTunnel;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBTunnelThematicSurface thematicSurfaceExporter;
	private DBTunnelInstallation tunnelInstallationExporter;
	private DBTunnelHollowSpace hollowSpaceExporter;
	private DBOtherGeometry geometryExporter;

	private HashMap<Long, AbstractTunnel> tunnels;
	private ProjectionPropertyFilter projectionFilter;

	public DBTunnel(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.TUNNEL);

		init();
	}

	private void init() throws SQLException {
		tunnels = new HashMap<Long, AbstractTunnel>();
		String tunnelId = projectionFilter.pass(CityGMLModuleType.TUNNEL, "consistsOfTunnelPart") ? "TUNNEL_ROOT_ID" : "ID";

		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select ID, TUNNEL_PARENT_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, YEAR_OF_CONSTRUCTION, YEAR_OF_DEMOLITION, ")
			.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, ")
			.append("LOD2_MULTI_CURVE, LOD3_MULTI_CURVE, LOD4_MULTI_CURVE, ")
			.append("LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID, ")
			.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID ")
			.append("from TUNNEL where ").append(tunnelId).append(" = ?");
			psTunnel = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select ID, TUNNEL_PARENT_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, YEAR_OF_CONSTRUCTION, YEAR_OF_DEMOLITION, ")
			.append(transformOrNull).append("(LOD1_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD1_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD2_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD2_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD3_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD3_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD4_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD4_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD2_MULTI_CURVE, ").append(srid).append(") AS LOD2_MULTI_CURVE, ")
			.append(transformOrNull).append("(LOD3_MULTI_CURVE, ").append(srid).append(") AS LOD3_MULTI_CURVE, ")
			.append(transformOrNull).append("(LOD4_MULTI_CURVE, ").append(srid).append(") AS LOD4_MULTI_CURVE, ")
			.append("LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID, ")
			.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID ")
			.append("from TUNNEL where ").append(tunnelId).append(" = ?");
			psTunnel = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBTunnelThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.TUNNEL_THEMATIC_SURFACE);
		tunnelInstallationExporter = (DBTunnelInstallation)dbExporterManager.getDBExporter(DBExporterEnum.TUNNEL_INSTALLATION);
		hollowSpaceExporter = (DBTunnelHollowSpace)dbExporterManager.getDBExporter(DBExporterEnum.TUNNEL_HOLLOW_SPACE);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		ResultSet rs = null;

		try {
			long tunnelId = splitter.getPrimaryKey();
			psTunnel.setLong(1, tunnelId);
			rs = psTunnel.executeQuery();

			Tunnel root = new Tunnel();
			tunnels.put(tunnelId, root);

			while (rs.next()) {
				long id = rs.getLong(1);
				long parentId = rs.getLong(2);

				AbstractTunnel parentTunnel = null;
				AbstractTunnel abstractTunnel = null;

				// get or create parent tunnel
				if (parentId != 0) {
					parentTunnel = tunnels.get(parentId);
					if (parentTunnel == null) {
						parentTunnel = new TunnelPart();
						tunnels.put(parentId, parentTunnel);
					}
				}

				// get or create tunnel
				abstractTunnel = tunnels.get(id);
				if (abstractTunnel == null) {
					abstractTunnel = new TunnelPart();
					tunnels.put(id, abstractTunnel);
				}

				if (!abstractTunnel.hasLocalProperty("isCreated")) {
					abstractTunnel.setLocalProperty("isCreated", true);

					// do cityObject stuff
					boolean success = cityObjectExporter.read(abstractTunnel, id, parentId == 0, projectionFilter);
					if (!success)
						return false;

					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "class")) {
						String clazz = rs.getString(3);
						if (clazz != null) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString(4));
							abstractTunnel.setClazz(code);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "function")) {
						String function = rs.getString(5);
						String functionCodeSpace = rs.getString(6);
						if (function != null)
							abstractTunnel.setFunction(Util.string2codeList(function, functionCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "usage")) {
						String usage = rs.getString(7);
						String usageCodeSpace = rs.getString(8);
						if (usage != null)
							abstractTunnel.setUsage(Util.string2codeList(usage, usageCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "yearOfConstruction")) {
						Date yearOfConstruction = rs.getDate(9);				
						if (yearOfConstruction != null) {
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(yearOfConstruction);
							abstractTunnel.setYearOfConstruction(gregDate);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "yearOfDemolition")) {
						Date yearOfDemolition = rs.getDate(10);
						if (yearOfDemolition != null) {
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(yearOfDemolition);
							abstractTunnel.setYearOfDemolition(gregDate);
						}
					}

					// terrainIntersection
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.TUNNEL, new StringBuilder("lod").append(lod + 1).append("TerrainIntersection").toString()))
							continue;

						Object terrainIntersectionObj = rs.getObject(11 + lod);
						if (rs.wasNull() || terrainIntersectionObj == null)
							continue;

						GeometryObject terrainIntersection = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
						if (terrainIntersection != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(terrainIntersection, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									abstractTunnel.setLod1TerrainIntersection(multiCurveProperty);
									break;
								case 1:
									abstractTunnel.setLod2TerrainIntersection(multiCurveProperty);
									break;
								case 2:
									abstractTunnel.setLod3TerrainIntersection(multiCurveProperty);
									break;
								case 3:
									abstractTunnel.setLod4TerrainIntersection(multiCurveProperty);
									break;
								}
							}
						}
					}

					// multiCurve
					for (int lod = 0; lod < 3; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.TUNNEL, new StringBuilder("lod").append(lod + 2).append("MultiCurve").toString()))
							continue;

						Object multiCurveObj = rs.getObject(15 + lod);
						if (rs.wasNull() || multiCurveObj == null)
							continue;

						GeometryObject multiCurve = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
						if (multiCurve != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(multiCurve, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									abstractTunnel.setLod2MultiCurve(multiCurveProperty);
									break;
								case 1:
									abstractTunnel.setLod3MultiCurve(multiCurveProperty);
									break;
								case 2:
									abstractTunnel.setLod4MultiCurve(multiCurveProperty);
									break;
								}
							}
						}
					}

					// BoundarySurface
					// according to conformance requirement no. 3 of the Bridge version 2.0.0 module
					// geometry objects of _BoundarySurface elements have to be referenced by lodXSolid and
					// lodXMultiSurface properties. So we first export all _BoundarySurfaces
					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "boundedBy"))
						thematicSurfaceExporter.read(abstractTunnel, id);

					// solid
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.TUNNEL, new StringBuilder("lod").append(lod + 1).append("Solid").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(18 + lod);
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
								abstractTunnel.setLod1Solid(solidProperty);
								break;
							case 1:
								abstractTunnel.setLod2Solid(solidProperty);
								break;
							case 2:
								abstractTunnel.setLod3Solid(solidProperty);
								break;
							case 3:
								abstractTunnel.setLod4Solid(solidProperty);
								break;
							}
						}
					}

					// multiSurface
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.TUNNEL, new StringBuilder("lod").append(lod + 1).append("MultiSurface").toString()))
							continue;

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
								abstractTunnel.setLod1MultiSurface(multiSurfaceProperty);
								break;
							case 1:
								abstractTunnel.setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 2:
								abstractTunnel.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								abstractTunnel.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}

					// TunnelInstallation
					tunnelInstallationExporter.read(abstractTunnel, id, projectionFilter);

					// HollowSpace
					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "interiorHollowSpace"))
						hollowSpaceExporter.read(abstractTunnel, id);

					// add tunnel part to parent tunnel
					if (parentTunnel != null)
						parentTunnel.addConsistsOfTunnelPart(new TunnelPartProperty((TunnelPart)abstractTunnel));	
				}
			}

			dbExporterManager.processFeature(root);

			if (root.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(root.getId(), tunnelId, root.getCityGMLClass());

			return true;
		} finally {
			tunnels.clear();

			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTunnel.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TUNNEL;
	}

}
