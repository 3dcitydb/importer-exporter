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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DBBuilding implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuilding;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBThematicSurface thematicSurfaceExporter;
	private DBBuildingInstallation buildingInstallationExporter;
	private DBRoom roomExporter;
	private DBOtherGeometry geometryExporter;

	private HashMap<Long, AbstractBuilding> buildings;
	private ProjectionPropertyFilter projectionFilter;
	private boolean handleAddressGmlId;
	private boolean useXLink;
	private boolean appendOldGmlId;
	private String gmlIdPrefix;

	public DBBuilding(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		projectionFilter = exportFilter.getProjectionPropertyFilter(CityGMLClass.BUILDING);

		init();
	}

	private void init() throws SQLException {
		handleAddressGmlId = dbExporterManager.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
		useXLink = config.getProject().getExporter().getXlink().getFeature().isModeXLink();

		if (!useXLink) {
			appendOldGmlId = config.getProject().getExporter().getXlink().getFeature().isSetAppendId();
			gmlIdPrefix = config.getProject().getExporter().getXlink().getFeature().getIdPrefix();
		}

		buildings = new HashMap<Long, AbstractBuilding>();
		String buildingId = projectionFilter.pass(CityGMLModuleType.BUILDING, "consistsOfBuildingPart") ? "BUILDING_ROOT_ID" : "ID";

		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
					.append("select b.ID, b.BUILDING_PARENT_ID, b.CLASS, b.CLASS_CODESPACE, b.FUNCTION, b.FUNCTION_CODESPACE, b.USAGE, b.USAGE_CODESPACE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, ")
					.append("b.ROOF_TYPE, b.ROOF_TYPE_CODESPACE, b.MEASURED_HEIGHT, b.MEASURED_HEIGHT_UNIT, b.STOREYS_ABOVE_GROUND, b.STOREYS_BELOW_GROUND, b.STOREY_HEIGHTS_ABOVE_GROUND, b.STOREY_HEIGHTS_AG_UNIT, b.STOREY_HEIGHTS_BELOW_GROUND, b.STOREY_HEIGHTS_BG_UNIT, ")
					.append("b.LOD1_TERRAIN_INTERSECTION, b.LOD2_TERRAIN_INTERSECTION, b.LOD3_TERRAIN_INTERSECTION, b.LOD4_TERRAIN_INTERSECTION, ")
					.append("b.LOD2_MULTI_CURVE, b.LOD3_MULTI_CURVE, b.LOD4_MULTI_CURVE, ")
					.append("LOD0_FOOTPRINT_ID, LOD0_ROOFPRINT_ID, ")
					.append("b.LOD1_SOLID_ID, b.LOD2_SOLID_ID, b.LOD3_SOLID_ID, b.LOD4_SOLID_ID, ")
					.append("b.LOD1_MULTI_SURFACE_ID, b.LOD2_MULTI_SURFACE_ID, b.LOD3_MULTI_SURFACE_ID, b.LOD4_MULTI_SURFACE_ID, ")
					.append("a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT, a.XAL_SOURCE").append(handleAddressGmlId ? ", a.GMLID " : " ")
					.append("from BUILDING b left join ADDRESS_TO_BUILDING a2b on b.ID=a2b.BUILDING_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.").append(buildingId).append(" = ?");
			psBuilding = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("citydb_srs.transform_or_null");

			StringBuilder query = new StringBuilder()
					.append("select b.ID, b.BUILDING_PARENT_ID, b.CLASS, b.CLASS_CODESPACE, b.FUNCTION, b.FUNCTION_CODESPACE, b.USAGE, b.USAGE_CODESPACE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, ")
					.append("b.ROOF_TYPE, b.ROOF_TYPE_CODESPACE, b.MEASURED_HEIGHT, b.MEASURED_HEIGHT_UNIT, b.STOREYS_ABOVE_GROUND, b.STOREYS_BELOW_GROUND, b.STOREY_HEIGHTS_ABOVE_GROUND, b.STOREY_HEIGHTS_AG_UNIT, b.STOREY_HEIGHTS_BELOW_GROUND, b.STOREY_HEIGHTS_BG_UNIT, ")
					.append(transformOrNull).append("(b.LOD1_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD1_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD2_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD2_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD3_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD3_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD4_TERRAIN_INTERSECTION, ").append(srid).append(") AS LOD4_TERRAIN_INTERSECTION, ")
					.append(transformOrNull).append("(b.LOD2_MULTI_CURVE, ").append(srid).append(") AS LOD2_MULTI_CURVE, ")
					.append(transformOrNull).append("(b.LOD3_MULTI_CURVE, ").append(srid).append(") AS LOD3_MULTI_CURVE, ")
					.append(transformOrNull).append("(b.LOD4_MULTI_CURVE, ").append(srid).append(") AS LOD4_MULTI_CURVE, ")
					.append("LOD0_FOOTPRINT_ID, LOD0_ROOFPRINT_ID, ")
					.append("b.LOD1_SOLID_ID, b.LOD2_SOLID_ID, b.LOD3_SOLID_ID, b.LOD4_SOLID_ID, ")
					.append("b.LOD1_MULTI_SURFACE_ID, b.LOD2_MULTI_SURFACE_ID, b.LOD3_MULTI_SURFACE_ID, b.LOD4_MULTI_SURFACE_ID, ")
					.append("a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, ")
					.append(transformOrNull).append("(a.MULTI_POINT, ").append(srid).append(") AS MULTI_POINT, a.XAL_SOURCE").append(handleAddressGmlId ? ", a.GMLID " : " ")
					.append("from BUILDING b left join ADDRESS_TO_BUILDING a2b on b.ID=a2b.BUILDING_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.").append(buildingId).append(" = ?");
			psBuilding = connection.prepareStatement(query.toString());
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.THEMATIC_SURFACE);
		buildingInstallationExporter = (DBBuildingInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_INSTALLATION);
		roomExporter = (DBRoom)dbExporterManager.getDBExporter(DBExporterEnum.ROOM);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, FeatureProcessException {
		ResultSet rs = null;

		try {
			long buildingId = splitter.getPrimaryKey();
			psBuilding.setLong(1, buildingId);
			rs = psBuilding.executeQuery();

			Building root = new Building();
			buildings.put(buildingId, root);

			while (rs.next()) {
				long id = rs.getLong(1);
				long parentId = rs.getLong(2);

				AbstractBuilding parentBuilding = null;
				AbstractBuilding abstractBuilding = null;

				// get or create parent building
				if (parentId != 0) {
					parentBuilding = buildings.get(parentId);
					if (parentBuilding == null) {
						parentBuilding = new BuildingPart();
						buildings.put(parentId, parentBuilding);
					}
				}

				// get or create building
				abstractBuilding = buildings.get(id);
				if (abstractBuilding == null) {
					abstractBuilding = new BuildingPart();
					buildings.put(id, abstractBuilding);
				}

				if (!abstractBuilding.hasLocalProperty("isCreated")) {
					abstractBuilding.setLocalProperty("isCreated", true);

					// do cityObject stuff
					boolean success = cityObjectExporter.read(abstractBuilding, id, parentId == 0, projectionFilter);
					if (!success)
						return false;

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "class")) {
						String clazz = rs.getString(3);
						if (clazz != null) {
							Code code = new Code(clazz);
							code.setCodeSpace(rs.getString(4));
							abstractBuilding.setClazz(code);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "function")) {
						String function = rs.getString(5);
						String functionCodeSpace = rs.getString(6);
						if (function != null)
							abstractBuilding.setFunction(Util.string2codeList(function, functionCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "usage")) {
						String usage = rs.getString(7);
						String usageCodeSpace = rs.getString(8);
						if (usage != null)
							abstractBuilding.setUsage(Util.string2codeList(usage, usageCodeSpace));
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "yearOfConstruction")) {
						Date yearOfConstruction = rs.getDate(9);				
						if (yearOfConstruction != null) {
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(yearOfConstruction);
							abstractBuilding.setYearOfConstruction(gregDate);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "yearOfDemolition")) {
						Date yearOfDemolition = rs.getDate(10);
						if (yearOfDemolition != null) {
							GregorianCalendar gregDate = new GregorianCalendar();
							gregDate.setTime(yearOfDemolition);
							abstractBuilding.setYearOfDemolition(gregDate);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "roofType")) {
						String roofType = rs.getString(11);
						if (roofType != null) {
							Code code = new Code(roofType);
							code.setCodeSpace(rs.getString(12));
							abstractBuilding.setRoofType(code);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "measuredHeight")) {
						double measuredHeight = rs.getDouble(13);
						if (!rs.wasNull()) {
							Length length = new Length();
							length.setValue(measuredHeight);
							length.setUom(rs.getString(14));
							abstractBuilding.setMeasuredHeight(length);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "storeysAboveGround")) {
						Integer storeysAboveGround = rs.getInt(15);
						if (!rs.wasNull())
							abstractBuilding.setStoreysAboveGround(storeysAboveGround);
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "storeysBelowGround")) {
						Integer storeysBelowGround = rs.getInt(16);
						if (!rs.wasNull())
							abstractBuilding.setStoreysBelowGround(storeysBelowGround);
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "storeyHeightsAboveGround")) {
						String storeyHeightsAboveGround = rs.getString(17);
						if (storeyHeightsAboveGround != null) {
							List<DoubleOrNull> storeyHeightsAboveGroundList = new ArrayList<DoubleOrNull>();
							MeasureOrNullList measureList = new MeasureOrNullList();
							Pattern p = Pattern.compile("\\s+");
							String[] measureStrings = p.split(storeyHeightsAboveGround.trim());

							for (String measureString : measureStrings) {
								try {
									storeyHeightsAboveGroundList.add(new DoubleOrNull(Double.parseDouble(measureString)));
								} catch (NumberFormatException nfEx) {
									//
								}
							}

							measureList.setDoubleOrNull(storeyHeightsAboveGroundList);
							measureList.setUom(rs.getString(18));
							abstractBuilding.setStoreyHeightsAboveGround(measureList);
						}
					}

					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "storeyHeightsBelowGround")) {
						String storeyHeightsBelowGround = rs.getString(19);
						if (storeyHeightsBelowGround != null) {
							List<DoubleOrNull> storeyHeightsBelowGroundList = new ArrayList<DoubleOrNull>();
							MeasureOrNullList measureList = new MeasureOrNullList();
							Pattern p = Pattern.compile("\\s+");
							String[] measureStrings = p.split(storeyHeightsBelowGround.trim());

							for (String measureString : measureStrings) {
								try {
									storeyHeightsBelowGroundList.add(new DoubleOrNull(Double.parseDouble(measureString)));
								} catch (NumberFormatException nfEx) {
									//
								}
							}

							measureList.setDoubleOrNull(storeyHeightsBelowGroundList);
							measureList.setUom(rs.getString(20));
							abstractBuilding.setStoreyHeightsBelowGround(measureList);
						}
					}

					// terrainIntersection
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BUILDING, new StringBuilder("lod").append(lod + 1).append("TerrainIntersection").toString()))
							continue;

						Object terrainIntersectionObj = rs.getObject(21 + lod);
						if (rs.wasNull() || terrainIntersectionObj == null)
							continue;

						GeometryObject terrainIntersection = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
						if (terrainIntersection != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(terrainIntersection, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									abstractBuilding.setLod1TerrainIntersection(multiCurveProperty);
									break;
								case 1:
									abstractBuilding.setLod2TerrainIntersection(multiCurveProperty);
									break;
								case 2:
									abstractBuilding.setLod3TerrainIntersection(multiCurveProperty);
									break;
								case 3:
									abstractBuilding.setLod4TerrainIntersection(multiCurveProperty);
									break;
								}
							}
						}
					}

					// multiCurve
					for (int lod = 0; lod < 3; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BUILDING, new StringBuilder("lod").append(lod + 2).append("MultiCurve").toString()))
							continue;

						Object multiCurveObj = rs.getObject(25 + lod);
						if (rs.wasNull() || multiCurveObj == null)
							continue;

						GeometryObject multiCurve = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
						if (multiCurve != null) {
							MultiCurveProperty multiCurveProperty = geometryExporter.getMultiCurveProperty(multiCurve, false);
							if (multiCurveProperty != null) {
								switch (lod) {
								case 0:
									abstractBuilding.setLod2MultiCurve(multiCurveProperty);
									break;
								case 1:
									abstractBuilding.setLod3MultiCurve(multiCurveProperty);
									break;
								case 2:
									abstractBuilding.setLod4MultiCurve(multiCurveProperty);
									break;
								}
							}
						}
					}

					// footPrint and roofEdge
					for (int i = 0; i < 2; i++) {
						if (i == 0 && projectionFilter.filter(CityGMLModuleType.BUILDING, new StringBuilder("lod0FootPrint").toString()))
							continue;
						else if (i == 1 && projectionFilter.filter(CityGMLModuleType.BUILDING, new StringBuilder("lod0RoofEdge").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(28 + i);
						if (rs.wasNull() || surfaceGeometryId == 0)
							continue;

						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
						if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							switch (i) {
							case 0:
								abstractBuilding.setLod0FootPrint(multiSurfaceProperty);
								break;
							case 1:
								abstractBuilding.setLod0RoofEdge(multiSurfaceProperty);
								break;
							}
						}
					}

					// BoundarySurface
					// according to conformance requirement no. 3 of the Building version 2.0.0 module
					// geometry objects of _BoundarySurface elements have to be referenced by lodXSolid and
					// lodXMultiSurface properties. So we first export all _BoundarySurfaces
					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "boundedBy"))
						thematicSurfaceExporter.read(abstractBuilding, id);

					// solid
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BUILDING, new StringBuilder("lod").append(lod + 1).append("Solid").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(30 + lod);
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
								abstractBuilding.setLod1Solid(solidProperty);
								break;
							case 1:
								abstractBuilding.setLod2Solid(solidProperty);
								break;
							case 2:
								abstractBuilding.setLod3Solid(solidProperty);
								break;
							case 3:
								abstractBuilding.setLod4Solid(solidProperty);
								break;
							}
						}
					}

					// multiSurface
					for (int lod = 0; lod < 4; lod++) {
						if (projectionFilter.filter(CityGMLModuleType.BUILDING, new StringBuilder("lod").append(lod + 1).append("MultiSurface").toString()))
							continue;

						long surfaceGeometryId = rs.getLong(34 + lod);
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
								abstractBuilding.setLod1MultiSurface(multiSurfaceProperty);
								break;
							case 1:
								abstractBuilding.setLod2MultiSurface(multiSurfaceProperty);
								break;
							case 2:
								abstractBuilding.setLod3MultiSurface(multiSurfaceProperty);
								break;
							case 3:
								abstractBuilding.setLod4MultiSurface(multiSurfaceProperty);
								break;
							}
						}
					}

					// BuildingInstallation
					buildingInstallationExporter.read(abstractBuilding, id, projectionFilter);

					// room
					if (projectionFilter.pass(CityGMLModuleType.BUILDING, "interiorRoom"))
						roomExporter.read(abstractBuilding, id);

					// add building part to parent building
					if (parentBuilding != null)
						parentBuilding.addConsistsOfBuildingPart(new BuildingPartProperty((BuildingPart)abstractBuilding));	
				}

				// address
				if (projectionFilter.pass(CityGMLModuleType.BUILDING, "address")) {
					long addressId = rs.getLong(38);
					if (!rs.wasNull()) {
						AddressExportFactory factory = dbExporterManager.getAddressExportFactory();
						AddressObject addressObject = factory.newAddressObject();
						AddressProperty addressProperty = null;

						if (handleAddressGmlId) {
							String gmlId = rs.getString(48);
							if (gmlId != null && dbExporterManager.lookupAndPutGmlId(gmlId, addressId, CityGMLClass.ADDRESS)) {
								if (useXLink) {
									addressProperty = new AddressProperty();
									addressProperty.setHref("#" + gmlId);
									abstractBuilding.addAddress(addressProperty);
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
								Object multiPointObj = rs.getObject(46);
								if (!rs.wasNull() && multiPointObj != null) {
									GeometryObject multiPoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(multiPointObj);
									MultiPointProperty multiPointProperty = geometryExporter.getMultiPointProperty(multiPoint, false);
									if (multiPointProperty != null)
										addressObject.setMultiPointProperty(multiPointProperty);
								}

								// create xAL address
								addressProperty = factory.create(addressObject);
								if (addressProperty != null)
									abstractBuilding.addAddress(addressProperty);
							}
						}
					}
				}
			}

			dbExporterManager.processFeature(root);

			if (root.isSetId() && config.getInternal().isRegisterGmlIdInCache())
				dbExporterManager.putUID(root.getId(), buildingId, root.getCityGMLClass());

			return true;
		} finally {
			buildings.clear();

			if (rs != null)
				rs.close();
		}
	}

	private void fillAddressObject(AddressObject addressObject, AddressMode mode, ResultSet rs) throws SQLException {
		if (mode == AddressMode.DB) {
			addressObject.setStreet(rs.getString(39));
			addressObject.setHouseNumber(rs.getString(40));
			addressObject.setPOBox(rs.getString(41));
			addressObject.setZipCode(rs.getString(42));
			addressObject.setCity(rs.getString(43));
			addressObject.setState(rs.getString(44));
			addressObject.setCountry(rs.getString(45));			
		} else {
			String xal = rs.getString(47);
			if (!rs.wasNull()) {
				Object object = dbExporterManager.unmarshal(new StringReader(xal));
				if (object instanceof AddressDetails)
					addressObject.setAddressDetails((AddressDetails)object);
			}
		}
	}

	@Override
	public void close() throws SQLException {
		psBuilding.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BUILDING;
	}

}
