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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.exporter.AddressMode;
import org.citydb.modules.citygml.common.xal.AddressExportFactory;
import org.citydb.modules.citygml.common.xal.AddressObject;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.Bridge;
import org.citygml4j.model.citygml.bridge.BridgePart;
import org.citygml4j.model.citygml.bridge.BridgePartProperty;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBBridge implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBridge;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBridgeThematicSurface thematicSurfaceExporter;
	private DBBridgeInstallation bridgeInstallationExporter;
	private DBBridgeConstrElement bridgeContrElemExporter;
	private DBBridgeRoom bridgeRoomExporter;
	private DBOtherGeometry geometryExporter;

	private HashMap<Long, AbstractBridge> bridges;
	private ProjectionPropertyFilter projectionFilter;
	private boolean handleAddressGmlId;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private String gmlIdPrefix;

	public DBBridge(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.BRIDGE);

		init();
	}

	private void init() throws SQLException {
		handleAddressGmlId = dbExporterManager.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();

		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
		}

		bridges = new HashMap<Long, AbstractBridge>();
		String bridgeId = projectionFilter.pass(CityGMLModuleType.BRIDGE, "consistsOfBridgePart") ? "BRIDGE_ROOT_ID" : "ID";

		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
					.append("select b.ID, b.BRIDGE_PARENT_ID, b.CLASS, b.CLASS_CODESPACE, b.FUNCTION, b.FUNCTION_CODESPACE, b.USAGE, b.USAGE_CODESPACE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, b.IS_MOVABLE, ")
					.append("b.LOD1_TERRAIN_INTERSECTION, b.LOD2_TERRAIN_INTERSECTION, b.LOD3_TERRAIN_INTERSECTION, b.LOD4_TERRAIN_INTERSECTION, ")
					.append("b.LOD2_MULTI_CURVE, b.LOD3_MULTI_CURVE, b.LOD4_MULTI_CURVE, ")
					.append("b.LOD1_SOLID_ID, b.LOD2_SOLID_ID, b.LOD3_SOLID_ID, b.LOD4_SOLID_ID, ")
					.append("b.LOD1_MULTI_SURFACE_ID, b.LOD2_MULTI_SURFACE_ID, b.LOD3_MULTI_SURFACE_ID, b.LOD4_MULTI_SURFACE_ID, ")
					.append("a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT, a.XAL_SOURCE").append(handleAddressGmlId ? ", a.GMLID " : " ")
					.append("from BRIDGE b left join ADDRESS_TO_BRIDGE a2b on b.ID=a2b.BRIDGE_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.").append(bridgeId).append(" = ?");
			psBridge = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
					.append("select b.ID, b.BRIDGE_PARENT_ID, b.CLASS, b.CLASS_CODESPACE, b.FUNCTION, b.FUNCTION_CODESPACE, b.USAGE, b.USAGE_CODESPACE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, b.IS_MOVABLE, ")
					.append(transformOrNull).append("(b.LOD1_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD1_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD2_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD2_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD3_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD3_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD4_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD4_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD2_MULTI_CURVE, ").append(srid).append(") AS LOD2_MULTI_CURVE, ")
					.append(transformOrNull).append("(b.LOD3_MULTI_CURVE, ").append(srid).append(") AS LOD3_MULTI_CURVE, ")
					.append(transformOrNull).append("(b.LOD4_MULTI_CURVE, ").append(srid).append(") AS LOD4_MULTI_CURVE, ")
					.append("b.LOD1_SOLID_ID, b.LOD2_SOLID_ID, b.LOD3_SOLID_ID, b.LOD4_SOLID_ID, ")
					.append("b.LOD1_MULTI_SURFACE_ID, b.LOD2_MULTI_SURFACE_ID, b.LOD3_MULTI_SURFACE_ID, b.LOD4_MULTI_SURFACE_ID, ")
					.append("a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, ")
					.append(transformOrNull).append("(a.MULTI_POINT, ").append(srid).append(") AS MULTI_POINT, a.XAL_SOURCE").append(handleAddressGmlId ? ", a.GMLID " : " ")
					.append("from BRIDGE b left join ADDRESS_TO_BRIDGE a2b on b.ID=a2b.BRIDGE_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.").append(bridgeId).append(" = ?");
			psBridge = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBBridgeThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_THEMATIC_SURFACE);
		bridgeInstallationExporter = (DBBridgeInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_INSTALLATION);
		bridgeContrElemExporter = (DBBridgeConstrElement)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_CONSTR_ELEMENT);
		bridgeRoomExporter = (DBBridgeRoom)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_ROOM);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		ResultSet rs = null;

		try {
			long bridgeId = splitter.getPrimaryKey();
			psBridge.setLong(1, bridgeId);
			rs = psBridge.executeQuery();

			Bridge root = new Bridge();
			bridges.put(bridgeId, root);

			while (rs.next()) {
				long id = rs.getLong(1);
				long parentId = rs.getLong(2);

				AbstractBridge parentBridge = null;
				AbstractBridge abstractBridge = null;

				// get or create parent bridge
				if (parentId != 0) {
					parentBridge = bridges.get(parentId);
					if (parentBridge == null) {
						parentBridge = new BridgePart();
						bridges.put(parentId, parentBridge);
					}
				}

				// get or create bridge
				abstractBridge = bridges.get(id);
				if (abstractBridge == null) {
					abstractBridge = new BridgePart();
					bridges.put(id, abstractBridge);
				}

				if (!abstractBridge.hasLocalProperty("isCreated")) {
					abstractBridge.setLocalProperty("isCreated", true);

					// do cityObject stuff
					boolean success = cityObjectExporter.read(abstractBridge, id, parentId == 0, projectionFilter);
					if (!success)
						return false;

					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "class")) {
						String clazz = rs.getString(3);
						if (clazz != null) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString(4));
							abstractBridge.setClazz(code);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "function")) {
						String function = rs.getString(5);
						String functionCodeSpace = rs.getString(6);
						if (function != null)
							abstractBridge.setFunction(Util.string2codeList(function, functionCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "usage")) {
						String usage = rs.getString(7);
						String usageCodeSpace = rs.getString(8);
						if (usage != null)
							abstractBridge.setUsage(Util.string2codeList(usage, usageCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "yearOfConstruction")) {
						Date yearOfConstruction = rs.getDate(9);				
						if (yearOfConstruction != null) {
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(yearOfConstruction);
							abstractBridge.setYearOfConstruction(gregDate);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "yearOfDemolition")) {
						Date yearOfDemolition = rs.getDate(10);
						if (yearOfDemolition != null) {
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(yearOfDemolition);
							abstractBridge.setYearOfDemolition(gregDate);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "isMovable")) {
						boolean isMovable = rs.getBoolean(11);
						if (!rs.wasNull())
							abstractBridge.setIsMovable(isMovable);
					}

					// terrainIntersection
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BRIDGE, new StringBuilder("lod").append(lod + 1).append("TerrainIntersection").toString()))
							continue;

						Object terrainIntersectionObj = rs.getObject(12 + lod);
						if (rs.wasNull() || terrainIntersectionObj == null)
							continue;

						GeometryObject terrainIntersection = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
						if (terrainIntersection != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(terrainIntersection, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									abstractBridge.setLod1TerrainIntersection(multiCurveProperty);
									break;
								case 1:
									abstractBridge.setLod2TerrainIntersection(multiCurveProperty);
									break;
								case 2:
									abstractBridge.setLod3TerrainIntersection(multiCurveProperty);
									break;
								case 3:
									abstractBridge.setLod4TerrainIntersection(multiCurveProperty);
									break;
								}
							}
						}
					}

					// multiCurve
					for (int lod = 0; lod < 3; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BRIDGE, new StringBuilder("lod").append(lod + 2).append("MultiCurve").toString()))
							continue;

						Object multiCurveObj = rs.getObject(16 + lod);
						if (rs.wasNull() || multiCurveObj == null)
							continue;

						GeometryObject multiCurve = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
						if (multiCurve != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(multiCurve, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									abstractBridge.setLod2MultiCurve(multiCurveProperty);
									break;
								case 1:
									abstractBridge.setLod3MultiCurve(multiCurveProperty);
									break;
								case 2:
									abstractBridge.setLod4MultiCurve(multiCurveProperty);
									break;
								}
							}
						}
					}

					// BoundarySurface
					// according to conformance requirement no. 3 of the Bridge version 2.0.0 module
					// geometry objects of _BoundarySurface elements have to be referenced by lodXSolid and
					// lodXMultiSurface properties. So we first export all _BoundarySurfaces
					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "boundedBy"))
						thematicSurfaceExporter.read(abstractBridge, id);

					// solid
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BRIDGE, new StringBuilder("lod").append(lod + 1).append("Solid").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(19 + lod);
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
								abstractBridge.setLod1Solid(solidProperty);
								break;
							case 1:
								abstractBridge.setLod2Solid(solidProperty);
								break;
							case 2:
								abstractBridge.setLod3Solid(solidProperty);
								break;
							case 3:
								abstractBridge.setLod4Solid(solidProperty);
								break;
							}
						}
					}

					// multiSurface
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BRIDGE, new StringBuilder("lod").append(lod + 1).append("MultiSurface").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(23 + lod);
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
								abstractBridge.setLod1MultiSurface(multiSurfaceProperty);
								break;
							case 1:
								abstractBridge.setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 2:
								abstractBridge.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								abstractBridge.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}

					// BridgeInstallation
					bridgeInstallationExporter.read(abstractBridge, id, projectionFilter);

					// BridgeConstructionElement
					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "outerBridgeConstruction"))
						bridgeContrElemExporter.read(abstractBridge, id);

					// room
					if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "interiorBridgeRoom"))
						bridgeRoomExporter.read(abstractBridge, id);

					// add bridge part to parent bridge
					if (parentBridge != null)
						parentBridge.addConsistsOfBridgePart(new BridgePartProperty((BridgePart)abstractBridge));	
				}

				// address
				if (projectionFilter.pass(CityGMLModuleType.BRIDGE, "address")) {
					long addressId = rs.getLong(27);
					if (!rs.wasNull()) {
						AddressExportFactory factory = dbExporterManager.getAddressExportFactory();					
						AddressObject addressObject = factory.newAddressObject();
						AddressProperty addressProperty = null;

						if (handleAddressGmlId) {
							String gmlId = rs.getString(37);
							if (gmlId != null && dbExporterManager.lookupAndPutGmlId(gmlId, addressId, CityGMLClass.ADDRESS)) {
								if (useXLink) {
									addressProperty = new AddressProperty();
									addressProperty.setHref("#" + gmlId);
									abstractBridge.addAddress(addressProperty);
								} else {
									String newGmlId = DefaultGMLIdManager.getInstance().generateUUID(gmlIdPrefix);
									if (appendOldGmlId)
										newGmlId += '-' + gmlId;

									addressObject.setGmlId(newGmlId);	
								}
							} else
								addressObject.setGmlId(gmlId);							
						}

						if (addressProperty == null) {
							fillAddressObject(addressObject, factory.getPrimaryMode(), rs);
							if (!addressObject.canCreate(factory.getPrimaryMode()) && factory.isUseFallback())
								fillAddressObject(addressObject, factory.getFallbackMode(), rs);

							if (addressObject.canCreate()) {
								// multiPointGeometry
								Object multiPointObj = rs.getObject(35);
								if (!rs.wasNull() && multiPointObj != null) {
									GeometryObject multiPoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(multiPointObj);
									MultiPointProperty multiPointProperty = geometryExporter.getMultiPointProperty(multiPoint, false);
									if (multiPointProperty != null)
										addressObject.setMultiPointProperty(multiPointProperty);
								}

								// create xAL address
								addressProperty = factory.create(addressObject);
								if (addressProperty != null)
									abstractBridge.addAddress(addressProperty);
							}
						}
					}
				}
			}

			dbExporterManager.processFeature(root);

			if (root.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(root.getId(), bridgeId, root.getCityGMLClass());

			return true;
		} finally {
			bridges.clear();

			if (rs != null)
				rs.close();
		}
	}

	private void fillAddressObject(AddressObject addressObject, AddressMode mode, ResultSet rs) throws SQLException {
		if (mode == AddressMode.DB) {
			addressObject.setStreet(rs.getString(28));
			addressObject.setHouseNumber(rs.getString(29));
			addressObject.setPOBox(rs.getString(30));
			addressObject.setZipCode(rs.getString(31));
			addressObject.setCity(rs.getString(32));
			addressObject.setState(rs.getString(33));
			addressObject.setCountry(rs.getString(34));
		} else {
			String xal = rs.getString(36);
			if (!rs.wasNull()) {
				Object object = dbExporterManager.unmarshal(new StringReader(xal));
				if (object instanceof AddressDetails)
					addressObject.setAddressDetails((AddressDetails)object);
			}
		}
	}

	@Override
	public void close() throws SQLException {
		psBridge.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BRIDGE;
	}

}
