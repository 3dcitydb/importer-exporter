/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;

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
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.exporter.AddressMode;
import de.tub.citydb.modules.citygml.common.xal.AddressExportFactory;
import de.tub.citydb.modules.citygml.common.xal.AddressObject;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

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
	private FeatureClassFilter featureClassFilter;

	public DBBridge(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		if (!config.getInternal().isTransformCoordinates()) {
			StringBuilder query = new StringBuilder()
			.append("select b.ID, b.BRIDGE_PARENT_ID, b.CLASS, b.CLASS_CODESPACE, b.FUNCTION, b.FUNCTION_CODESPACE, b.USAGE, b.USAGE_CODESPACE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, b.IS_MOVABLE, ")
			.append("b.LOD1_TERRAIN_INTERSECTION, b.LOD2_TERRAIN_INTERSECTION, b.LOD3_TERRAIN_INTERSECTION, b.LOD4_TERRAIN_INTERSECTION, ")
			.append("b.LOD2_MULTI_CURVE, b.LOD3_MULTI_CURVE, b.LOD4_MULTI_CURVE, ")
			.append("b.LOD1_SOLID_ID, b.LOD2_SOLID_ID, b.LOD3_SOLID_ID, b.LOD4_SOLID_ID, ")
			.append("b.LOD1_MULTI_SURFACE_ID, b.LOD2_MULTI_SURFACE_ID, b.LOD3_MULTI_SURFACE_ID, b.LOD4_MULTI_SURFACE_ID, ")
			.append("a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT, a.XAL_SOURCE ")
			.append("from BRIDGE b left join ADDRESS_TO_BRIDGE a2b on b.ID=a2b.BRIDGE_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.BRIDGE_ROOT_ID = ?");
			psBridge = connection.prepareStatement(query.toString());
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			String transformOrNull = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("geodb_util.transform_or_null");

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
			.append(transformOrNull).append("(a.MULTI_POINT, ").append(srid).append(") AS MULTI_POINT, a.XAL_SOURCE ")
			.append("from BRIDGE b left join ADDRESS_TO_BRIDGE a2b on b.ID=a2b.BRIDGE_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.BRIDGE_ROOT_ID = ?");
			psBridge = connection.prepareStatement(query.toString());
		}

		bridges = new HashMap<Long, AbstractBridge>();

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBBridgeThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_THEMATIC_SURFACE);
		bridgeInstallationExporter = (DBBridgeInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_INSTALLATION);
		bridgeContrElemExporter = (DBBridgeConstrElement)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_CONSTR_ELEMENT);
		bridgeRoomExporter = (DBBridgeRoom)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_ROOM);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
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
					boolean success = cityObjectExporter.read(abstractBridge, id, parentId == 0);
					if (!success)
						return false;

					String clazz = rs.getString(3);
					if (clazz != null) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString(4));
						abstractBridge.setClazz(code);
					}

					String function = rs.getString(5);
					String functionCodeSpace = rs.getString(6);
					if (function != null)
						abstractBridge.setFunction(Util.string2codeList(function, functionCodeSpace));

					String usage = rs.getString(7);
					String usageCodeSpace = rs.getString(8);
					if (usage != null)
						abstractBridge.setUsage(Util.string2codeList(usage, usageCodeSpace));

					Date yearOfConstruction = rs.getDate(9);				
					if (yearOfConstruction != null) {
						GregorianCalendar gregDate = new GregorianCalendar();
						gregDate.setTime(yearOfConstruction);
						abstractBridge.setYearOfConstruction(gregDate);
					}

					Date yearOfDemolition = rs.getDate(10);
					if (yearOfDemolition != null) {
						GregorianCalendar gregDate = new GregorianCalendar();
						gregDate.setTime(yearOfDemolition);
						abstractBridge.setYearOfDemolition(gregDate);
					}

					boolean isMovable = rs.getBoolean(11);
					if (!rs.wasNull())
						abstractBridge.setIsMovable(isMovable);

					// terrainIntersection
					for (int lod = 0; lod < 4; lod++) {
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
					thematicSurfaceExporter.read(abstractBridge, id);

					// solid
					for (int lod = 0; lod < 4; lod++) {
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
					bridgeInstallationExporter.read(abstractBridge, id);

					// BridgeConstructionElement
					bridgeContrElemExporter.read(abstractBridge, id);

					// room
					bridgeRoomExporter.read(abstractBridge, id);

					// add bridge part to parent bridge
					if (parentBridge != null)
						parentBridge.addConsistsOfBridgePart(new BridgePartProperty((BridgePart)abstractBridge));	
				}

				// address
				rs.getLong(36);
				if (!rs.wasNull()) {
					AddressExportFactory factory = dbExporterManager.getAddressExportFactory();					
					AddressObject addressObject = factory.newAddressObject();

					fillAddressObject(addressObject, factory.getPrimaryMode(), rs);
					if (!addressObject.canCreate(factory.getPrimaryMode()) && factory.isUseFallback())
						fillAddressObject(addressObject, factory.getFallbackMode(), rs);

					if (addressObject.canCreate()) {
						// multiPointGeometry
						Object multiPointObj = rs.getObject(44);
						if (!rs.wasNull() && multiPointObj != null) {
							GeometryObject multiPoint = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getMultiPoint(multiPointObj);
							MultiPointProperty multiPointProperty = geometryExporter.getMultiPointProperty(multiPoint, false);
							if (multiPointProperty != null)
								addressObject.setMultiPointProperty(multiPointProperty);
						}

						// create xAL address
						AddressProperty addressProperty = factory.create(addressObject);
						if (addressProperty != null)
							abstractBridge.addAddress(addressProperty);
					}
				}	
			}

			bridges.clear();

			if (root.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
				dbExporterManager.putUID(root.getId(), bridgeId, root.getCityGMLClass());
			dbExporterManager.print(root);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	private void fillAddressObject(AddressObject addressObject, AddressMode mode, ResultSet rs) throws SQLException {
		if (mode == AddressMode.DB) {
			addressObject.setStreet(rs.getString(37));
			addressObject.setHouseNumber(rs.getString(38));
			addressObject.setPOBox(rs.getString(39));
			addressObject.setZipCode(rs.getString(40));
			addressObject.setCity(rs.getString(41));
			addressObject.setState(rs.getString(42));
			addressObject.setCountry(rs.getString(43));
		} else {
			String xal = rs.getString(45);
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
