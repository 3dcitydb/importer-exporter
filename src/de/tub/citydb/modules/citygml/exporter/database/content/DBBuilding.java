/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.citygml.building.BuildingImpl;
import org.citygml4j.impl.citygml.building.BuildingPartImpl;
import org.citygml4j.impl.citygml.building.BuildingPartPropertyImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.basicTypes.DoubleOrNullImpl;
import org.citygml4j.impl.gml.basicTypes.MeasureOrNullListImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSurfacePropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.SolidPropertyImpl;
import org.citygml4j.impl.gml.measures.LengthImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.exporter.AddressMode;
import de.tub.citydb.modules.citygml.common.xal.AddressExportFactory;
import de.tub.citydb.modules.citygml.common.xal.AddressObject;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.util.Util;

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
	private DBSdoGeometry sdoGeometry;
	private FeatureClassFilter featureClassFilter;

	private boolean transformCoords;

	public DBBuilding(Connection connection, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.config = config;
		this.connection = connection;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {
			psBuilding = connection.prepareStatement("select b.ID, b.BUILDING_PARENT_ID, b.NAME, b.NAME_CODESPACE, b.DESCRIPTION, b.CLASS, b.FUNCTION, " +
					"b.USAGE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, b.ROOF_TYPE, b.MEASURED_HEIGHT, b.STOREYS_ABOVE_GROUND, b.STOREYS_BELOW_GROUND, " +
					"b.STOREY_HEIGHTS_ABOVE_GROUND, b.STOREY_HEIGHTS_BELOW_GROUND, b.LOD1_GEOMETRY_ID, b.LOD2_GEOMETRY_ID, b.LOD3_GEOMETRY_ID, b.LOD4_GEOMETRY_ID, " +
					"b.LOD1_TERRAIN_INTERSECTION, b.LOD2_TERRAIN_INTERSECTION, b.LOD3_TERRAIN_INTERSECTION, b.LOD4_TERRAIN_INTERSECTION, " +
					"b.LOD2_MULTI_CURVE, b.LOD3_MULTI_CURVE, b.LOD4_MULTI_CURVE, " +
					"a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT, a.XAL_SOURCE " +
					"from BUILDING b left join ADDRESS_TO_BUILDING a2b on b.ID=a2b.BUILDING_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.BUILDING_ROOT_ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();

			psBuilding = connection.prepareStatement("select b.ID, b.BUILDING_PARENT_ID, b.NAME, b.NAME_CODESPACE, b.DESCRIPTION, b.CLASS, b.FUNCTION, " +
					"b.USAGE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, b.ROOF_TYPE, b.MEASURED_HEIGHT, b.STOREYS_ABOVE_GROUND, b.STOREYS_BELOW_GROUND, " +
					"b.STOREY_HEIGHTS_ABOVE_GROUND, b.STOREY_HEIGHTS_BELOW_GROUND, b.LOD1_GEOMETRY_ID, b.LOD2_GEOMETRY_ID, b.LOD3_GEOMETRY_ID, b.LOD4_GEOMETRY_ID, " +
					"geodb_util.transform_or_null(b.LOD1_TERRAIN_INTERSECTION, " + srid + ") AS LOD1_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(b.LOD2_TERRAIN_INTERSECTION, " + srid + ") AS LOD2_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(b.LOD3_TERRAIN_INTERSECTION, " + srid + ") AS LOD3_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(b.LOD4_TERRAIN_INTERSECTION, " + srid + ") AS LOD4_TERRAIN_INTERSECTION, " +
					"geodb_util.transform_or_null(b.LOD2_MULTI_CURVE, " + srid + ") AS LOD2_MULTI_CURVE, " +
					"geodb_util.transform_or_null(b.LOD3_MULTI_CURVE, " + srid + ") AS LOD3_MULTI_CURVE, " +
					"geodb_util.transform_or_null(b.LOD4_MULTI_CURVE, " + srid + ") AS LOD4_MULTI_CURVE, " +
					"a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, " +
					"geodb_util.transform_or_null(a.MULTI_POINT, " + srid + ") AS MULTI_POINT, a.XAL_SOURCE " +
					"from BUILDING b left join ADDRESS_TO_BUILDING a2b on b.ID=a2b.BUILDING_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.BUILDING_ROOT_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.THEMATIC_SURFACE);
		buildingInstallationExporter = (DBBuildingInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_INSTALLATION);
		roomExporter = (DBRoom)dbExporterManager.getDBExporter(DBExporterEnum.ROOM);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, CityGMLWriteException {
		ResultSet rs = null;

		try {
			long buildingRootId = splitter.getPrimaryKey();
			psBuilding.setLong(1, buildingRootId);
			rs = psBuilding.executeQuery();
			BuildingTree buildingTree = new BuildingTree();

			long currentBuildingId = 0;				

			while (rs.next()) {
				BuildingNode buildingNode = null;
				long id = rs.getLong("ID");

				if (currentBuildingId != id) {
					currentBuildingId = id;

					long parentId = rs.getLong("BUILDING_PARENT_ID");
					String gmlName = rs.getString("NAME");
					String gmlNameCodeSpace = rs.getString("NAME_CODESPACE");
					String description = rs.getString("DESCRIPTION");
					String clazz = rs.getString("CLASS");
					String function = rs.getString("FUNCTION");
					String usage = rs.getString("USAGE");
					Date yearOfConstruction = rs.getDate("YEAR_OF_CONSTRUCTION");
					Date yearOfDemolition = rs.getDate("YEAR_OF_DEMOLITION");
					String roofType = rs.getString("ROOF_TYPE");
					Double measuredHeight = rs.getDouble("MEASURED_HEIGHT");
					if (rs.wasNull())
						measuredHeight = null;
					Integer storeysAboveGround = rs.getInt("STOREYS_ABOVE_GROUND");
					if (rs.wasNull())
						storeysAboveGround = null;
					Integer storeysBelowGround = rs.getInt("STOREYS_BELOW_GROUND");
					if (rs.wasNull())
						storeysBelowGround = null;
					String storeyHeightsAboveGround = rs.getString("STOREY_HEIGHTS_ABOVE_GROUND");
					String storeyHeightsBelowGround = rs.getString("STOREY_HEIGHTS_BELOW_GROUND");
					long[] lodGeometryId = new long[4];
					JGeometry[] terrainIntersection = new JGeometry[4];
					JGeometry[] multiCurve = new JGeometry[3];

					for (int lod = 1; lod < 5 ; lod++) {
						long lodSurfaceGeometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");					
						if (!rs.wasNull() && lodSurfaceGeometryId != 0)
							lodGeometryId[lod - 1] = lodSurfaceGeometryId;

						STRUCT terrainIntersectionObj = (STRUCT)rs.getObject("LOD" + lod + "_TERRAIN_INTERSECTION");
						if (!rs.wasNull() && terrainIntersectionObj != null)
							terrainIntersection[lod - 1] = JGeometry.load(terrainIntersectionObj);

						if (lod >= 2) {
							STRUCT multiCurveObj = (STRUCT)rs.getObject("LOD" + lod + "_MULTI_CURVE");
							if (!rs.wasNull() && multiCurveObj != null)
								multiCurve[lod - 2] = JGeometry.load(multiCurveObj);
						}
					}

					// constructing BuildingNode
					buildingNode = new BuildingNode();
					buildingNode.id = id;
					buildingNode.parentId = parentId;
					buildingNode.name = gmlName;
					buildingNode.nameCodespace = gmlNameCodeSpace;
					buildingNode.description = description;
					buildingNode.clazz = clazz;
					buildingNode.function = function;
					buildingNode.usage = usage;
					buildingNode.yearOfConstruction = yearOfConstruction;
					buildingNode.yearOfDemolition = yearOfDemolition;
					buildingNode.roofType = roofType;
					buildingNode.measuredHeight = measuredHeight;
					buildingNode.storeysAboveGround = storeysAboveGround;
					buildingNode.storeysBelowGround = storeysBelowGround;
					buildingNode.storeyHeightsAboveGround = storeyHeightsAboveGround;
					buildingNode.storeyHeightsBelowGround = storeyHeightsBelowGround;
					buildingNode.lodGeometryId = lodGeometryId;
					buildingNode.terrainIntersection = terrainIntersection;
					buildingNode.multiCurve = multiCurve;

					// put it into buildingTree
					buildingTree.insertNode(buildingNode, parentId);
				} else {
					buildingNode = buildingTree.getNode(id);
				}

				// address information
				rs.getLong("ADDR_ID");
				if (!rs.wasNull()) {
					AddressExportFactory factory = dbExporterManager.getAddressExportFactory();					
					AddressObject addressObject = factory.newAddressObject();

					fillAddressObject(addressObject, factory.getPrimaryMode(), rs);
					if (!addressObject.canCreate(factory.getPrimaryMode()) && factory.isUseFallback())
						fillAddressObject(addressObject, factory.getFallbackMode(), rs);

					if (addressObject.canCreate()) {
						// multiPointGeometry
						STRUCT multiPointObj = (STRUCT)rs.getObject("MULTI_POINT");
						if (!rs.wasNull() && multiPointObj != null) {
							JGeometry multiPoint = JGeometry.load(multiPointObj);
							MultiPointProperty multiPointProperty = sdoGeometry.getMultiPointProperty(multiPoint, false);
							if (multiPointProperty != null)
								addressObject.setMultiPointProperty(multiPointProperty);
						}
						
						// create xAL address
						AddressProperty addressProperty = factory.create(addressObject);
						if (addressProperty != null)
							buildingNode.addressProperty.add(addressProperty);
					}
				}			
			}

			// interpret buildingTree as a single abstractBuilding
			Building building = null;

			if (buildingTree.root != 0) {
				building = (Building)rebuildBuilding(buildingTree.getNode(buildingTree.root));

				if (building == null)
					return false;

			} else
				return false;

			dbExporterManager.print(building);
			return true;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	private AbstractBuilding rebuildBuilding(BuildingNode buildingNode) throws SQLException {
		AbstractBuilding abstractBuilding = null;

		if (buildingNode.parentId != 0) {
			// we are dealing with a buildingPart
			abstractBuilding = new BuildingPartImpl();
		} else {
			abstractBuilding = new BuildingImpl();
		}

		// do cityObject stuff
		boolean success = cityObjectExporter.read(abstractBuilding, buildingNode.id, buildingNode.parentId == 0);
		if (!success)
			return null;

		String gmlName = buildingNode.name;
		String gmlNameCodespace = buildingNode.nameCodespace;

		Util.dbGmlName2featureName(abstractBuilding, gmlName, gmlNameCodespace);

		if (buildingNode.description != null) {
			StringOrRef stringOrRef = new StringOrRefImpl();
			stringOrRef.setValue(buildingNode.description);
			abstractBuilding.setDescription(stringOrRef);
		}

		if (buildingNode.clazz != null) {
			abstractBuilding.setClazz(buildingNode.clazz);
		}

		if (buildingNode.function != null) {
			Pattern p = Pattern.compile("\\s+");
			String[] functionList = p.split(buildingNode.function.trim());
			abstractBuilding.setFunction(Arrays.asList(functionList));
		}

		if (buildingNode.usage != null) {
			Pattern p = Pattern.compile("\\s+");
			String[] usageList = p.split(buildingNode.usage.trim());
			abstractBuilding.setUsage(Arrays.asList(usageList));
		}

		if (buildingNode.yearOfConstruction != null) {
			GregorianCalendar gregDate = new GregorianCalendar();
			gregDate.setTime(buildingNode.yearOfConstruction);
			abstractBuilding.setYearOfConstruction(gregDate);
		}

		if (buildingNode.yearOfDemolition != null) {
			GregorianCalendar gregDate = new GregorianCalendar();
			gregDate.setTime(buildingNode.yearOfDemolition);
			abstractBuilding.setYearOfDemolition(gregDate);
		}

		if (buildingNode.roofType != null) {
			abstractBuilding.setRoofType(buildingNode.roofType);
		}

		if (buildingNode.measuredHeight != null) {
			Length length = new LengthImpl();
			length.setValue(buildingNode.measuredHeight);
			length.setUom("urn:ogc:def:uom:UCUM::m");
			abstractBuilding.setMeasuredHeight(length);
		}

		if (buildingNode.storeysAboveGround != null) {
			abstractBuilding.setStoreysAboveGround(buildingNode.storeysAboveGround);
		}

		if (buildingNode.storeysBelowGround != null) {
			abstractBuilding.setStoreysBelowGround(buildingNode.storeysBelowGround);
		}

		if (buildingNode.storeyHeightsAboveGround != null) {
			List<DoubleOrNull> storeyHeightsAboveGroundList = new ArrayList<DoubleOrNull>();
			MeasureOrNullList measureList = new MeasureOrNullListImpl();
			Pattern p = Pattern.compile("\\s+");
			String[] measureStrings = p.split(buildingNode.storeyHeightsAboveGround.trim());

			for (String measureString : measureStrings) {
				try {
					storeyHeightsAboveGroundList.add(new DoubleOrNullImpl(Double.parseDouble(measureString)));
				} catch (NumberFormatException nfEx) {
					//
				}
			}

			measureList.setDoubleOrNull(storeyHeightsAboveGroundList);
			measureList.setUom("urn:ogc:def:uom:UCUM::m");
			abstractBuilding.setStoreyHeightsAboveGround(measureList);
		}

		if (buildingNode.storeyHeightsBelowGround != null) {
			List<DoubleOrNull> storeyHeightsBelowGroundList = new ArrayList<DoubleOrNull>();
			MeasureOrNullList measureList = new MeasureOrNullListImpl();
			Pattern p = Pattern.compile("\\s+");
			String[] measureStrings = p.split(buildingNode.storeyHeightsBelowGround.trim());

			for (String measureString : measureStrings) {
				try {
					storeyHeightsBelowGroundList.add(new DoubleOrNullImpl(Double.parseDouble(measureString)));
				} catch (NumberFormatException nfEx) {
					//
				}
			}

			measureList.setDoubleOrNull(storeyHeightsBelowGroundList);
			measureList.setUom("urn:ogc:def:uom:UCUM::m");
			abstractBuilding.setStoreyHeightsBelowGround(measureList);
		}

		// terrainIntersection
		for (int lod = 1; lod < 5; lod++) {
			JGeometry terrainIntersection = buildingNode.terrainIntersection[lod - 1];

			if (terrainIntersection != null) {
				MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(terrainIntersection, false);
				if (multiCurveProperty != null) {
					switch (lod) {
					case 1:
						abstractBuilding.setLod1TerrainIntersection(multiCurveProperty);
						break;
					case 2:
						abstractBuilding.setLod2TerrainIntersection(multiCurveProperty);
						break;
					case 3:
						abstractBuilding.setLod3TerrainIntersection(multiCurveProperty);
						break;
					case 4:
						abstractBuilding.setLod4TerrainIntersection(multiCurveProperty);
						break;
					}
				}
			}
		}

		// multiCurve
		for (int lod = 2; lod < 5; lod++) {
			JGeometry multiCurve = buildingNode.multiCurve[lod - 2];

			if (multiCurve != null) {
				MultiCurveProperty multiCurveProperty = sdoGeometry.getMultiCurveProperty(multiCurve, false);
				if (multiCurveProperty != null) {
					switch (lod) {
					case 2:
						abstractBuilding.setLod2MultiCurve(multiCurveProperty);
						break;
					case 3:
						abstractBuilding.setLod3MultiCurve(multiCurveProperty);
						break;
					case 4:
						abstractBuilding.setLod4MultiCurve(multiCurveProperty);
						break;
					}
				}
			}
		}

		// BoundarySurface
		// according to conformance requirement no. 3 of the Building version 1.0.0 module
		// geometry objects of _BoundarySurface elements have to be referenced by lodXSolid and
		// lodXMultiSurface properties. So we first export all _BoundarySurfaces
		thematicSurfaceExporter.read(abstractBuilding, buildingNode.id);

		// surface geometry
		for (int lod = 1; lod < 5 ; lod++) {
			long lodSurfaceGeometryId = buildingNode.lodGeometryId[lod - 1];

			if (lodSurfaceGeometryId != 0) {
				DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodSurfaceGeometryId);

				if (geometry != null) {
					switch (geometry.getType()) {
					case COMPOSITE_SOLID:
					case SOLID:
						SolidProperty solidProperty = new SolidPropertyImpl();

						if (geometry.getAbstractGeometry() != null)
							solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
						else
							solidProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 1:
							abstractBuilding.setLod1Solid(solidProperty);
							break;
						case 2:
							abstractBuilding.setLod2Solid(solidProperty);
							break;
						case 3:
							abstractBuilding.setLod3Solid(solidProperty);
							break;
						case 4:
							abstractBuilding.setLod4Solid(solidProperty);
							break;
						}

						break;

					case MULTI_SURFACE:
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

						if (geometry.getAbstractGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getTarget());

						switch (lod) {
						case 1:
							abstractBuilding.setLod1MultiSurface(multiSurfaceProperty);
							break;
						case 2:
							abstractBuilding.setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							abstractBuilding.setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 4:
							abstractBuilding.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}

						break;
					}

				}
			}
		}

		// BuildingInstallation
		buildingInstallationExporter.read(abstractBuilding, buildingNode.id);

		// room
		roomExporter.read(abstractBuilding, buildingNode.id);

		// address 
		if (!buildingNode.addressProperty.isEmpty()) {
			for (AddressProperty addressProperty : buildingNode.addressProperty) 
				abstractBuilding.addAddress(addressProperty);
		}

		for (BuildingNode childNode : buildingNode.childNodes) {
			BuildingPart buildingPart = (BuildingPart)rebuildBuilding(childNode);
			BuildingPartProperty buildingPartProperty = new BuildingPartPropertyImpl();
			buildingPartProperty.setObject(buildingPart);
			abstractBuilding.addConsistsOfBuildingPart(buildingPartProperty);
		}

		if (abstractBuilding.isSetId() && !featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP))
			dbExporterManager.putGmlId(abstractBuilding.getId(), buildingNode.id, abstractBuilding.getCityGMLClass());

		return abstractBuilding;
	}

	private class BuildingNode {
		protected long id;
		protected long parentId;
		protected String name;
		protected String nameCodespace;
		protected String description;
		protected String clazz;
		protected String function;
		protected String usage;
		protected Date yearOfConstruction;
		protected Date yearOfDemolition;
		protected String roofType;
		protected Double measuredHeight;
		protected Integer storeysAboveGround;
		protected Integer storeysBelowGround;
		protected String storeyHeightsAboveGround;
		protected String storeyHeightsBelowGround;
		protected long[] lodGeometryId;
		protected JGeometry[] terrainIntersection;
		protected JGeometry[] multiCurve;
		protected Vector<AddressProperty> addressProperty;
		protected Vector<BuildingNode> childNodes;

		public BuildingNode() {
			lodGeometryId = new long[4];
			terrainIntersection = new JGeometry[4];
			multiCurve = new JGeometry[3];
			addressProperty = new Vector<AddressProperty>();
			childNodes = new Vector<BuildingNode>();
		}
	}

	private void fillAddressObject(AddressObject addressObject, AddressMode mode, ResultSet rs) throws SQLException {
		if (mode == AddressMode.DB) {
			addressObject.setStreet(rs.getString("STREET"));
			addressObject.setHouseNumber(rs.getString("HOUSE_NUMBER"));
			addressObject.setPOBox(rs.getString("PO_BOX"));
			addressObject.setZipCode(rs.getString("ZIP_CODE"));
			addressObject.setCity(rs.getString("CITY"));
			addressObject.setCountry(rs.getString("COUNTRY"));
			addressObject.setState(rs.getString("STATE"));
		} else {
			Clob clob = rs.getClob("XAL_SOURCE");
			if (!rs.wasNull()) {
				Object object = dbExporterManager.unmarshal(clob.getCharacterStream());
				if (object instanceof AddressDetails)
					addressObject.setAddressDetails((AddressDetails)object);
			}
		}
	}

	private class BuildingTree {
		long root;
		private HashMap<Long, BuildingNode> buildingTree;

		public BuildingTree() {
			buildingTree = new HashMap<Long, BuildingNode>();
		}

		public void insertNode(BuildingNode buildingNode, long parentId) {

			if (parentId == 0)
				root = buildingNode.id;

			if (buildingTree.containsKey(buildingNode.id)) {
				// a previously inserted pseudo node...
				BuildingNode pseudoNode = buildingTree.get(buildingNode.id);
				pseudoNode.id = buildingNode.id;
				pseudoNode.parentId = buildingNode.parentId;
				pseudoNode.name = buildingNode.name;
				pseudoNode.nameCodespace= buildingNode.nameCodespace;
				pseudoNode.description = buildingNode.description;
				pseudoNode.clazz = buildingNode.clazz;
				pseudoNode.function = buildingNode.function;
				pseudoNode.usage = buildingNode.usage;
				pseudoNode.yearOfConstruction = buildingNode.yearOfConstruction;
				pseudoNode.yearOfDemolition = buildingNode.yearOfDemolition;
				pseudoNode.roofType = buildingNode.roofType;
				pseudoNode.measuredHeight = buildingNode.measuredHeight;
				pseudoNode.storeysAboveGround = buildingNode.storeysAboveGround;
				pseudoNode.storeysBelowGround = buildingNode.storeysBelowGround;
				pseudoNode.storeyHeightsAboveGround = buildingNode.storeyHeightsAboveGround;
				pseudoNode.storeyHeightsBelowGround = buildingNode.storeyHeightsBelowGround;
				pseudoNode.lodGeometryId = buildingNode.lodGeometryId;
				pseudoNode.terrainIntersection = buildingNode.terrainIntersection;
				pseudoNode.multiCurve = buildingNode.multiCurve;
				pseudoNode.addressProperty = buildingNode.addressProperty;

				buildingNode = pseudoNode;

			} else
				buildingTree.put(buildingNode.id, buildingNode);

			// make the node known to its parent...
			if (parentId != 0) {
				BuildingNode parentNode = buildingTree.get(parentId);

				if (parentNode == null) {
					// there is no entry so far, so lets create a
					// pseudo node
					parentNode = new BuildingNode();
					buildingTree.put(parentId, parentNode);
				}

				parentNode.childNodes.add(buildingNode);
			}
		}

		public BuildingNode getNode(long entryId) {
			return buildingTree.get(entryId);
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
