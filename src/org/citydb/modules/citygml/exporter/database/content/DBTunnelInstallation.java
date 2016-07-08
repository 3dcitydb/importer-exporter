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
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelInstallationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

public class DBTunnelInstallation implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTunnelInstallation;
	private PreparedStatement psHollowSpaceInstallation;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectReader;
	private DBTunnelThematicSurface thematicSurfaceExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBOtherGeometry geometryExporter;

	public DBTunnelInstallation(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select ID, OBJECTCLASS_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append("LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
			.append("LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
			.append("LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
			.append("LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from TUNNEL_INSTALLATION where ");

			psTunnelInstallation = connection.prepareStatement(query.toString() + "TUNNEL_ID = ?");
			psHollowSpaceInstallation = connection.prepareStatement(query.toString() + "TUNNEL_HOLLOW_SPACE_ID =  ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select ID, OBJECTCLASS_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append("LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
			.append(transformOrNull).append("(LOD2_OTHER_GEOM, ").append(srid).append(") AS LOD2_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD3_OTHER_GEOM, ").append(srid).append(") AS LOD3_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD4_OTHER_GEOM, ").append(srid).append(") AS LOD4_OTHER_GEOM, ")
			.append("LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
			.append(transformOrNull).append("(LOD2_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD2_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD3_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD3_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from TUNNEL_INSTALLATION where ");

			psTunnelInstallation = connection.prepareStatement(query.toString() + "TUNNEL_ID = ?");
			psHollowSpaceInstallation = connection.prepareStatement(query.toString() + "TUNNEL_HOLLOW_SPACE_ID =  ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectReader = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBTunnelThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.TUNNEL_THEMATIC_SURFACE);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public void read(AbstractTunnel tunnel, long parentId, ProjectionPropertyFilter projectionFilter) throws SQLException {
		ResultSet rs = null;

		try {
			psTunnelInstallation.setLong(1, parentId);
			rs = psTunnelInstallation.executeQuery();

			while (rs.next()) {
				long installationId = rs.getLong(1);

				int classId = rs.getInt(2);
				if (rs.wasNull() || classId == 0)
					continue;

				TunnelInstallation tunnelInstallation = null;
				IntTunnelInstallation intTunnelInstallation = null;

				CityGMLClass type = Util.classId2cityObject(classId);
				switch (type) {
				case TUNNEL_INSTALLATION:
					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "outerTunnelInstallation"))
						tunnelInstallation = new TunnelInstallation();
					break;
				case INT_TUNNEL_INSTALLATION:
					if (projectionFilter.pass(CityGMLModuleType.TUNNEL, "interiorTunnelInstallation"))
						intTunnelInstallation = new IntTunnelInstallation();
					break;
				default:
					continue;
				}
				
				if (tunnelInstallation == null && intTunnelInstallation == null)
					return;

				String clazz = rs.getString(3);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(4));
					if (tunnelInstallation != null)
						tunnelInstallation.setClazz(code);
					else
						intTunnelInstallation.setClazz(code);
				}

				String function = rs.getString(5);
				String functionCodeSpace = rs.getString(6);
				if (function != null) {
					if (tunnelInstallation != null)
						tunnelInstallation.setFunction(Util.string2codeList(function, functionCodeSpace));
					else
						intTunnelInstallation.setFunction(Util.string2codeList(function, functionCodeSpace));
				}

				String usage = rs.getString(7);
				String usageCodeSpace = rs.getString(8);
				if (usage != null) {
					if (tunnelInstallation != null)
						tunnelInstallation.setUsage(Util.string2codeList(usage, usageCodeSpace));
					else
						intTunnelInstallation.setUsage(Util.string2codeList(usage, usageCodeSpace));
				}

				// boundarySurface
				// geometry objects of _BoundarySurface elements have to be referenced by lodXGeometry 
				// So we first export all _BoundarySurfaces
				if (tunnelInstallation != null)
					thematicSurfaceExporter.read(tunnelInstallation, installationId);
				else
					thematicSurfaceExporter.read(intTunnelInstallation, installationId);					

				// geometry
				for (int lod = 0; lod < 3; lod++) {
					long surfaceGeometryId = rs.getLong(9 + lod);
					Object geomObj = rs.getObject(12 + lod);
					if (surfaceGeometryId == 0 && geomObj == null)
						continue;

					GeometryProperty<AbstractGeometry> geometryProperty = null;

					if (surfaceGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());
						}
					} else {
						GeometryObject geometry = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(geomObj);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							geometryProperty.setGeometry(geometryExporter.getPointOrCurveGeometry(geometry, true));
						}	
					}

					if (geometryProperty != null) {
						switch (lod) {
						case 0:
							if (tunnelInstallation != null)
								tunnelInstallation.setLod2Geometry(geometryProperty);
							break;
						case 1:
							if (tunnelInstallation != null)
								tunnelInstallation.setLod3Geometry(geometryProperty);
							break;
						case 2:
							if (tunnelInstallation != null)
								tunnelInstallation.setLod4Geometry(geometryProperty);
							else
								intTunnelInstallation.setLod4Geometry(geometryProperty);
							break;
						}
					}
				}

				// implicit geometry
				for (int lod = 0; lod < 3; lod++) {
					long implicitGeometryId = rs.getLong(15 + lod);
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(18 + lod);
					if (!rs.wasNull() && referencePointObj != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(21 + lod);

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 0:
							if (tunnelInstallation != null)
								tunnelInstallation.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 1:
							if (tunnelInstallation != null)
								tunnelInstallation.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							if (tunnelInstallation != null)
								tunnelInstallation.setLod4ImplicitRepresentation(implicitProperty);
							else
								intTunnelInstallation.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}

				if (tunnelInstallation != null) {
					cityObjectReader.read(tunnelInstallation, installationId);

					TunnelInstallationProperty bridgeInstProp = new TunnelInstallationProperty();
					bridgeInstProp.setObject(tunnelInstallation);
					tunnel.addOuterTunnelInstallation(bridgeInstProp);
				} else {
					cityObjectReader.read(intTunnelInstallation, installationId);

					IntTunnelInstallationProperty intInstProp = new IntTunnelInstallationProperty();
					intInstProp.setObject(intTunnelInstallation);
					tunnel.addInteriorTunnelInstallation(intInstProp);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public void read(HollowSpace hollowSpace, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psHollowSpaceInstallation.setLong(1, parentId);
			rs = psHollowSpaceInstallation.executeQuery();

			while (rs.next()) {
				long installationId = rs.getLong(1);

				int classId = rs.getInt(2);
				if (rs.wasNull() || classId == 0 || Util.classId2cityObject(classId) != CityGMLClass.INT_TUNNEL_INSTALLATION)
					continue;

				IntTunnelInstallation intTunnelInstallation = new IntTunnelInstallation();

				String clazz = rs.getString(3);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(4));
					intTunnelInstallation.setClazz(code);
				}

				String function = rs.getString(5);
				String functionCodeSpace = rs.getString(6);
				if (function != null)
					intTunnelInstallation.setFunction(Util.string2codeList(function, functionCodeSpace));

				String usage = rs.getString(7);
				String usageCodeSpace = rs.getString(8);
				if (usage != null)
					intTunnelInstallation.setUsage(Util.string2codeList(usage, usageCodeSpace));

				// boundarySurface
				// geometry objects of _BoundarySurface elements have to be referenced by lod4Geometry 
				// So we first export all _BoundarySurfaces
				thematicSurfaceExporter.read(intTunnelInstallation, installationId);	

				// geometry
				long surfaceGeometryId = rs.getLong(11);
				Object geomObj = rs.getObject(14);
				if (surfaceGeometryId != 0 || geomObj != null) {
					GeometryProperty<AbstractGeometry> geometryProperty = null;
					if (surfaceGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());
						}
					} else {
						GeometryObject geometry = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(geomObj);
						if (geometry != null) {
							geometryProperty = new GeometryProperty<AbstractGeometry>();
							geometryProperty.setGeometry(geometryExporter.getPointOrCurveGeometry(geometry, true));
						}	
					}

					if (geometryProperty != null)
						intTunnelInstallation.setLod4Geometry(geometryProperty);
				}

				// implicit geometry
				long implicitGeometryId = rs.getLong(17);
				if (implicitGeometryId != 0) {
					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(20);
					if (!rs.wasNull() && referencePointObj != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(23);

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);
						intTunnelInstallation.setLod4ImplicitRepresentation(implicitProperty);
					}
				}

				cityObjectReader.read(intTunnelInstallation, installationId);

				IntTunnelInstallationProperty intInstProp = new IntTunnelInstallationProperty();
				intInstProp.setObject(intTunnelInstallation);
				hollowSpace.addHollowSpaceInstallation(intInstProp);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psTunnelInstallation.close();
		psHollowSpaceInstallation.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.TUNNEL_INSTALLATION;
	}

}
