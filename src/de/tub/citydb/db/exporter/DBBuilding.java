package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.impl.jaxb.gml._3_1_1.LengthImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.MeasureOrNullListImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiSurfacePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.SolidPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.impl.jaxb.xal._2.AddressDetailsImpl;
import org.citygml4j.impl.jaxb.xal._2.CountryImpl;
import org.citygml4j.impl.jaxb.xal._2.CountryNameImpl;
import org.citygml4j.impl.jaxb.xal._2.LocalityImpl;
import org.citygml4j.impl.jaxb.xal._2.LocalityNameImpl;
import org.citygml4j.impl.jaxb.xal._2.PostBoxImpl;
import org.citygml4j.impl.jaxb.xal._2.PostBoxNumberImpl;
import org.citygml4j.impl.jaxb.xal._2.PostalCodeImpl;
import org.citygml4j.impl.jaxb.xal._2.PostalCodeNumberImpl;
import org.citygml4j.impl.jaxb.xal._2.ThoroughfareImpl;
import org.citygml4j.impl.jaxb.xal._2.ThoroughfareNameImpl;
import org.citygml4j.impl.jaxb.xal._2.ThoroughfareNumberImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingModule;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.gml.AbstractSolid;
import org.citygml4j.model.gml.Length;
import org.citygml4j.model.gml.MeasureOrNullList;
import org.citygml4j.model.gml.MultiCurveProperty;
import org.citygml4j.model.gml.MultiPointProperty;
import org.citygml4j.model.gml.MultiSurface;
import org.citygml4j.model.gml.MultiSurfaceProperty;
import org.citygml4j.model.gml.SolidProperty;
import org.citygml4j.model.gml.StringOrRef;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.PostBox;
import org.citygml4j.model.xal.PostBoxNumber;
import org.citygml4j.model.xal.PostalCode;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.Thoroughfare;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.model.xal.ThoroughfareNumber;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBBuilding implements DBExporter {
	private final Logger LOG = Logger.getInstance();

	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
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
	private DatatypeFactory datatypeFactory;

	private BuildingModule bldg;
	private CoreModule core;
	private boolean transformCoords;

	public DBBuilding(Connection connection, CityGMLFactory cityGMLFactory, ExportFilter exportFilter, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.dbExporterManager = dbExporterManager;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.connection = connection;
		this.featureClassFilter = exportFilter.getFeatureClassFilter();

		init();
	}

	private void init() throws SQLException {
		bldg = config.getProject().getExporter().getModuleVersion().getBuilding().getModule();
		core = (CoreModule)bldg.getModuleDependencies().getModule(CityGMLModuleType.CORE);
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {
			psBuilding = connection.prepareStatement("select b.ID, b.BUILDING_PARENT_ID, b.NAME, b.NAME_CODESPACE, b.DESCRIPTION, b.CLASS, b.FUNCTION, " +
					"b.USAGE, b.YEAR_OF_CONSTRUCTION, b.YEAR_OF_DEMOLITION, b.ROOF_TYPE, b.MEASURED_HEIGHT, b.STOREYS_ABOVE_GROUND, b.STOREYS_BELOW_GROUND, " +
					"b.STOREY_HEIGHTS_ABOVE_GROUND, b.STOREY_HEIGHTS_BELOW_GROUND, b.LOD1_GEOMETRY_ID, b.LOD2_GEOMETRY_ID, b.LOD3_GEOMETRY_ID, b.LOD4_GEOMETRY_ID, " +
					"b.LOD1_TERRAIN_INTERSECTION, b.LOD2_TERRAIN_INTERSECTION, b.LOD3_TERRAIN_INTERSECTION, b.LOD4_TERRAIN_INTERSECTION, " +
					"b.LOD2_MULTI_CURVE, b.LOD3_MULTI_CURVE, b.LOD4_MULTI_CURVE, " +
					"a.ID as ADDR_ID, a.STREET, a.HOUSE_NUMBER, a.PO_BOX, a.ZIP_CODE, a.CITY, a.STATE, a.COUNTRY, a.MULTI_POINT " +
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
					"geodb_util.transform_or_null(a.MULTI_POINT, " + srid + ") AS MULTI_POINT " +
			"from BUILDING b left join ADDRESS_TO_BUILDING a2b on b.ID=a2b.BUILDING_ID left join ADDRESS a on a.ID=a2b.ADDRESS_ID where b.BUILDING_ROOT_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		thematicSurfaceExporter = (DBThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.THEMATIC_SURFACE);
		buildingInstallationExporter = (DBBuildingInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_INSTALLATION);
		roomExporter = (DBRoom)dbExporterManager.getDBExporter(DBExporterEnum.ROOM);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);		

		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			//
		}
	}

	public boolean read(DBSplittingResult splitter) throws SQLException, JAXBException {
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
				AddressProperty addressProperty = null;
				long addressId = rs.getLong("ADDR_ID");

				if (!rs.wasNull() && addressId > 0) {
					String streetAttr = rs.getString("STREET");
					String houseNoAttr = rs.getString("HOUSE_NUMBER");
					String poBoxAttr = rs.getString("PO_BOX");
					String zipCodeAttr = rs.getString("ZIP_CODE");
					String cityAttr = rs.getString("CITY");
					String stateAttr = rs.getString("STATE");
					String countryAttr = rs.getString("COUNTRY");

					if (streetAttr != null ||
							poBoxAttr != null ||
							zipCodeAttr != null ||
							cityAttr != null ||
							countryAttr != null) {

						AddressDetails addressDetails = new AddressDetailsImpl();
						Country country = new CountryImpl();

						// country name
						if (countryAttr != null) {
							CountryName countryName = new CountryNameImpl();
							countryName.setContent(countryAttr);
							country.addCountryName(countryName);
						}

						Locality locality = new LocalityImpl();
						locality.setType("Town");

						if (cityAttr != null) {
							LocalityName localityName = new LocalityNameImpl();
							localityName.setContent(cityAttr);
							locality.addLocalityName(localityName);
						}

						if (streetAttr != null) {
							Thoroughfare thoroughfare = new ThoroughfareImpl();
							thoroughfare.setType("Street");

							ThoroughfareName name = new ThoroughfareNameImpl();
							name.setContent(streetAttr);
							thoroughfare.addThoroughfareName(name);

							if (houseNoAttr != null) {
								ThoroughfareNumber number = new ThoroughfareNumberImpl();
								number.setContent(houseNoAttr);
								thoroughfare.addThoroughfareNumber(number);
							}

							locality.setThoroughfare(thoroughfare);
						}				

						if (zipCodeAttr != null) {
							PostalCode postalCode = new PostalCodeImpl();
							PostalCodeNumber zipNumber = new PostalCodeNumberImpl();
							zipNumber.setContent(zipCodeAttr);

							postalCode.addPostalCodeNumber(zipNumber);
							locality.setPostalCode(postalCode);
						}

						if (poBoxAttr != null) {
							PostBox postBox = new PostBoxImpl();
							PostBoxNumber postBoxNumber = new PostBoxNumberImpl();
							postBoxNumber.setContent(poBoxAttr);

							postBox.setPostBoxNumber(postBoxNumber);
							locality.setPostBox(postBox);
						}

						country.setLocality(locality);
						addressDetails.setCountry(country);

						XalAddressProperty xalAddressProperty = cityGMLFactory.createXalAddressProperty(core);
						xalAddressProperty.setAddressDetails(addressDetails);

						Address address = cityGMLFactory.createAddress(core);
						address.setXalAddress(xalAddressProperty);

						// multiPointGeometry
						STRUCT multiPointObj = (STRUCT)rs.getObject("MULTI_POINT");
						if (!rs.wasNull() && multiPointObj != null) {
							JGeometry multiPoint = JGeometry.load(multiPointObj);

							MultiPointProperty multiPointProperty = sdoGeometry.getMultiPointProperty(multiPoint, false);
							if (multiPointProperty != null) {
								address.setMultiPoint(multiPointProperty);
							}
						}					

						addressProperty = cityGMLFactory.createAddressProperty(core);
						addressProperty.setObject(address);
					}
				}

				if (addressProperty != null)
					buildingNode.addressProperty.add(addressProperty);			
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
			abstractBuilding = cityGMLFactory.createBuildingPart(bldg);
		} else {
			abstractBuilding = cityGMLFactory.createBuilding(bldg);
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

			if (datatypeFactory != null)
				abstractBuilding.setYearOfConstruction(datatypeFactory.newXMLGregorianCalendarDate(
						gregDate.get(Calendar.YEAR),
						gregDate.get(Calendar.MONTH) + 1,
						gregDate.get(Calendar.DAY_OF_MONTH),
						DatatypeConstants.FIELD_UNDEFINED));
			else
				LOG.error(Util.getFeatureSignature(abstractBuilding.getCityGMLClass(), abstractBuilding.getId()) + 
				": Failed to write attribute 'yearOfConstruction' due to an internal error.");
		}

		if (buildingNode.yearOfDemolition != null) {
			GregorianCalendar gregDate = new GregorianCalendar();
			gregDate.setTime(buildingNode.yearOfDemolition);

			if (datatypeFactory != null)
				abstractBuilding.setYearOfDemolition(datatypeFactory.newXMLGregorianCalendarDate(
						gregDate.get(Calendar.YEAR),
						gregDate.get(Calendar.MONTH) + 1,
						gregDate.get(Calendar.DAY_OF_MONTH),
						DatatypeConstants.FIELD_UNDEFINED));
			else
				LOG.error(Util.getFeatureSignature(abstractBuilding.getCityGMLClass(), abstractBuilding.getId()) + 
				": Failed to write attribute 'yearOfDemolition' due to an internal error.");
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
			List<Double> storeyHeightsAboveGroundList = new ArrayList<Double>();
			MeasureOrNullList measureList = new MeasureOrNullListImpl();
			Pattern p = Pattern.compile("\\s+");
			String[] measureStrings = p.split(buildingNode.storeyHeightsAboveGround.trim());

			for (String measureString : measureStrings) {
				try {
					storeyHeightsAboveGroundList.add(Double.parseDouble(measureString));
				} catch (NumberFormatException nfEx) {
					//
				}
			}

			measureList.setValue(storeyHeightsAboveGroundList);
			measureList.setUom("urn:ogc:def:uom:UCUM::m");
			abstractBuilding.setStoreyHeightsAboveGround(measureList);
		}

		if (buildingNode.storeyHeightsBelowGround != null) {
			List<Double> storeyHeightsBelowGroundList = new ArrayList<Double>();
			MeasureOrNullList measureList = new MeasureOrNullListImpl();
			Pattern p = Pattern.compile("\\s+");
			String[] measureStrings = p.split(buildingNode.storeyHeightsBelowGround.trim());

			for (String measureString : measureStrings) {
				try {
					storeyHeightsBelowGroundList.add(Double.parseDouble(measureString));
				} catch (NumberFormatException nfEx) {
					//
				}
			}

			measureList.setValue(storeyHeightsBelowGroundList);
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
		thematicSurfaceExporter.read(abstractBuilding, buildingNode.id, bldg);

		// surface geometry
		for (int lod = 1; lod < 5 ; lod++) {
			long lodSurfaceGeometryId = buildingNode.lodGeometryId[lod - 1];

			if (lodSurfaceGeometryId != 0) {
				DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodSurfaceGeometryId);

				if (geometry != null) {
					switch (geometry.getType()) {
					case COMPOSITESOLID:
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

					case MULTISURFACE:
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
		buildingInstallationExporter.read(abstractBuilding, buildingNode.id, bldg);

		// room
		roomExporter.read(abstractBuilding, buildingNode.id, bldg);

		// address 
		if (!buildingNode.addressProperty.isEmpty()) {
			for (AddressProperty addressProperty : buildingNode.addressProperty) 
				abstractBuilding.addAddress(addressProperty);
		}

		for (BuildingNode childNode : buildingNode.childNodes) {
			BuildingPart buildingPart = (BuildingPart)rebuildBuilding(childNode);
			BuildingPartProperty buildingPartProperty = cityGMLFactory.createBuildingPartProperty(bldg);
			buildingPartProperty.setObject(buildingPart);
			abstractBuilding.addConsistsOfBuildingPart(buildingPartProperty);
		}

		if (abstractBuilding.isSetId() && !featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP))
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

			} else {
				//identify hierarchy nodes and place them into the tree
				if (parentId == 0)
					buildingTree.put(buildingNode.id, buildingNode);
			}

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
