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
import org.citydb.util.Util;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElementProperty;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

public class DBBridgeConstrElement implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBridgeConstrElement;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectReader;
	private DBBridgeThematicSurface thematicSurfaceExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private DBOtherGeometry geometryExporter;

	public DBBridgeConstrElement(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, ")
			.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
			.append("LOD1_OTHER_GEOM, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
			.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
			.append("LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from BRIDGE_CONSTR_ELEMENT where BRIDGE_ID = ?");

			psBridgeConstrElement = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
			.append("select ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
			.append(transformOrNull).append("(LOD1_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD1_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD2_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD2_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD3_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD3_TERRAIN_INTERSECTION, ")
			.append(transformOrNull).append("(LOD4_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD4_TERRAIN_INTERSECTION, ")
			.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
			.append(transformOrNull).append("(LOD1_OTHER_GEOM, ").append(srid).append(") AS LOD1_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD2_OTHER_GEOM, ").append(srid).append(") AS LOD2_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD3_OTHER_GEOM, ").append(srid).append(") AS LOD3_OTHER_GEOM, ")
			.append(transformOrNull).append("(LOD4_OTHER_GEOM, ").append(srid).append(") AS LOD4_OTHER_GEOM, ")
			.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
			.append(transformOrNull).append("(LOD1_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD2_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD2_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD2_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD3_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD3_IMPLICIT_REF_POINT, ")
			.append(transformOrNull).append("(LOD4_IMPLICIT_REF_POINT, ").append(srid).append(") AS LOD4_IMPLICIT_REF_POINT, ")
			.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION ")
			.append("from BRIDGE_CONSTR_ELEMENT where BRIDGE_ID = ?");

			psBridgeConstrElement = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectReader = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBBridgeThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_THEMATIC_SURFACE);
		implicitGeometryExporter = (DBImplicitGeometry)dbExporterManager.getDBExporter(DBExporterEnum.IMPLICIT_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public void read(AbstractBridge bridge, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psBridgeConstrElement.setLong(1, parentId);
			rs = psBridgeConstrElement.executeQuery();

			while (rs.next()) {
				long installationId = rs.getLong(1);
				BridgeConstructionElement bridgeConstrElement = new BridgeConstructionElement();

				String clazz = rs.getString(2);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(3));
					bridgeConstrElement.setClazz(code);
				}

				String function = rs.getString(4);
				String functionCodeSpace = rs.getString(5);
				if (function != null)
					bridgeConstrElement.setFunction(Util.string2codeList(function, functionCodeSpace));

				String usage = rs.getString(6);
				String usageCodeSpace = rs.getString(7);
				if (usage != null)
					bridgeConstrElement.setUsage(Util.string2codeList(usage, usageCodeSpace));

				// boundarySurface
				// geometry objects of _BoundarySurface elements have to be referenced by lodXGeometry 
				// So we first export all _BoundarySurfaces
				thematicSurfaceExporter.read(bridgeConstrElement, installationId);

				// terrainIntersection
				for (int lod = 0; lod < 4; lod++) {
					Object terrainIntersectionObj = rs.getObject(8 + lod);
					if (rs.wasNull() || terrainIntersectionObj == null)
						continue;

					GeometryObject terrainIntersection = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
					if (terrainIntersection != null) {
						MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(terrainIntersection, false);
						if (multiCurveProperty != null) {
							switch (lod) {
							case 0:
								bridgeConstrElement.setLod1TerrainIntersection(multiCurveProperty);
								break;
							case 1:
								bridgeConstrElement.setLod2TerrainIntersection(multiCurveProperty);
								break;
							case 2:
								bridgeConstrElement.setLod3TerrainIntersection(multiCurveProperty);
								break;
							case 3:
								bridgeConstrElement.setLod4TerrainIntersection(multiCurveProperty);
								break;
							}
						}
					}
				}

				// geometry
				for (int lod = 0; lod < 4; lod++) {
					long surfaceGeometryId = rs.getLong(12 + lod);
					Object geomObj = rs.getObject(16 + lod);
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
							bridgeConstrElement.setLod1Geometry(geometryProperty);
							break;
						case 1:
							bridgeConstrElement.setLod2Geometry(geometryProperty);
							break;
						case 2:
							bridgeConstrElement.setLod3Geometry(geometryProperty);
							break;
						case 3:
							bridgeConstrElement.setLod4Geometry(geometryProperty);
							break;
						}
					}
				}

				// implicit geometry
				for (int lod = 0; lod < 4; lod++) {
					long implicitGeometryId = rs.getLong(20 + lod);
					if (rs.wasNull())
						continue;

					GeometryObject referencePoint = null;
					Object referencePointObj = rs.getObject(24 + lod);
					if (!rs.wasNull() && referencePointObj != null)
						referencePoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

					String transformationMatrix = rs.getString(28 + lod);

					ImplicitGeometry implicit = implicitGeometryExporter.read(implicitGeometryId, referencePoint, transformationMatrix);
					if (implicit != null) {
						ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
						implicitProperty.setObject(implicit);

						switch (lod) {
						case 0:
							bridgeConstrElement.setLod1ImplicitRepresentation(implicitProperty);
							break;
						case 1:
							bridgeConstrElement.setLod2ImplicitRepresentation(implicitProperty);
							break;
						case 2:
							bridgeConstrElement.setLod3ImplicitRepresentation(implicitProperty);
							break;
						case 3:
							bridgeConstrElement.setLod4ImplicitRepresentation(implicitProperty);
							break;
						}
					}
				}

				cityObjectReader.read(bridgeConstrElement, installationId);

				BridgeConstructionElementProperty bridgeConstrElemProp = new BridgeConstructionElementProperty();
				bridgeConstrElemProp.setObject(bridgeConstrElement);
				bridge.addOuterBridgeConstructionElement(bridgeConstrElemProp);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psBridgeConstrElement.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BRIDGE_CONSTR_ELEMENT;
	}

}
