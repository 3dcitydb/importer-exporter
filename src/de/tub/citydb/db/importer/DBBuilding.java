package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.building.AbstractBuilding;
import de.tub.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import de.tub.citygml4j.model.citygml.building.BuildingInstallationProperty;
import de.tub.citygml4j.model.citygml.building.BuildingPartProperty;
import de.tub.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import de.tub.citygml4j.model.citygml.building.InteriorRoomProperty;
import de.tub.citygml4j.model.citygml.core.AddressProperty;
import de.tub.citygml4j.model.gml.Length;
import de.tub.citygml4j.model.gml.MeasureOrNullList;
import de.tub.citygml4j.model.gml.MultiCurveProperty;
import de.tub.citygml4j.model.gml.MultiSurfaceProperty;
import de.tub.citygml4j.model.gml.SolidProperty;

public class DBBuilding implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBuilding;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBBuildingInstallation buildingInstallationImporter;
	private DBRoom roomImporter;
	private DBAddress addressImporter;
	private DBSdoGeometry sdoGeometry;
	
	private String gmlNameDelimiter;
	private int batchCounter;

	public DBBuilding(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		
		psBuilding = batchConn.prepareStatement("insert into BUILDING (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, YEAR_OF_CONSTRUCTION, YEAR_OF_DEMOLITION, ROOF_TYPE, MEASURED_HEIGHT, " +
				"STOREYS_ABOVE_GROUND, STOREYS_BELOW_GROUND, STOREY_HEIGHTS_ABOVE_GROUND, STOREY_HEIGHTS_BELOW_GROUND, BUILDING_PARENT_ID, BUILDING_ROOT_ID, LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, " +
				"LOD4_GEOMETRY_ID, LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, LOD2_MULTI_CURVE, LOD3_MULTI_CURVE, LOD4_MULTI_CURVE) values " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.THEMATIC_SURFACE);
		buildingInstallationImporter = (DBBuildingInstallation)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING_INSTALLATION);
		roomImporter = (DBRoom)dbImporterManager.getDBImporter(DBImporterEnum.ROOM);
		addressImporter = (DBAddress)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(AbstractBuilding building) throws SQLException {
		long buildingId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (buildingId != 0)
			success = insert(building, buildingId, 0, buildingId);

		if (success)
			return buildingId;
		else
			return 0;
	}

	private boolean insert(AbstractBuilding building,
			long buildingId,
			long parentId,
			long rootId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(building, buildingId);
		if (cityObjectId == 0)
			return false;

		// Building
		// ID
		psBuilding.setLong(1, buildingId);

		// BUILDING_PARENT_ID
		if (parentId != 0)
			psBuilding.setLong(16, parentId);
		else
			psBuilding.setNull(16, 0);

		// BUILDING_ROOT_ID
		psBuilding.setLong(17, rootId);

		// gml:name
		if (building.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(building, gmlNameDelimiter);

			psBuilding.setString(2, dbGmlName[0]);
			psBuilding.setString(3, dbGmlName[1]);
		} else {
			psBuilding.setNull(2, Types.VARCHAR);
			psBuilding.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (building.getDescription() != null) {
			String description = building.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuilding.setString(4, description);
		} else {
			psBuilding.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (building.getClazz() != null) {
			psBuilding.setString(5, building.getClazz().trim());
		} else {
			psBuilding.setNull(5, Types.VARCHAR);
		}

		// citygml:function
		if (building.getFunction() != null) {
			List<String> functionList = building.getFunction();
			psBuilding.setString(6, Util.collection2string(functionList, " "));
		} else {
			psBuilding.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (building.getUsage() != null) {
			List<String> usageList = building.getUsage();
			psBuilding.setString(7, Util.collection2string(usageList, " "));
		} else {
			psBuilding.setNull(7, Types.VARCHAR);
		}

		// citygml:yearOfConstruction
		if (building.getYearOfConstruction() != null) {
			psBuilding.setDate(8, new Date(building.getYearOfConstruction().toGregorianCalendar().getTime().getTime()));
		} else {
			psBuilding.setNull(8, Types.DATE);
		}

		// citygml:yearOfDemolition
		if (building.getYearOfDemolition() != null) {
			psBuilding.setDate(9, new Date(building.getYearOfDemolition().toGregorianCalendar().getTime().getTime()));
		} else {
			psBuilding.setNull(9, Types.DATE);
		}

		// citygml:roofType
		if (building.getRoofType() != null) {
			psBuilding.setString(10, building.getRoofType());
		} else {
			psBuilding.setNull(10, Types.VARCHAR);
		}

		// citygml:measuredHeight
		if (building.getMeasuredHeight() != null) {
			Length measuredHeightLength = building.getMeasuredHeight();
			psBuilding.setDouble(11, measuredHeightLength.getValue());
		} else {
			psBuilding.setNull(11, Types.DOUBLE);
		}

		// citygml:storeysAboveGround
		if (building.getStoreysAboveGround() != null) {
			psBuilding.setInt(12, building.getStoreysAboveGround());
		} else {
			psBuilding.setNull(12, Types.INTEGER);
		}

		// citygml:storeysBelowGround
		if (building.getStoreysBelowGround() != null) {
			psBuilding.setInt(13, building.getStoreysBelowGround());
		} else {
			psBuilding.setNull(13, Types.INTEGER);
		}

		// citygml:storeyHeightsAboveGround
		if (building.getStoreyHeightsAboveGround() != null) {
			MeasureOrNullList measureOrNullList = building.getStoreyHeightsAboveGround();
			List<Double> valuesList = measureOrNullList.getValue();
			if (valuesList != null)
				psBuilding.setString(14, Util.collection2string(valuesList, " "));
			else
				psBuilding.setNull(14, Types.VARCHAR);
		} else {
			psBuilding.setNull(14, Types.VARCHAR);
		}

		// citygml:storeyHeightsBelowGround
		if (building.getStoreyHeightsBelowGround() != null) {
			MeasureOrNullList measureOrNullList = building.getStoreyHeightsBelowGround();
			List<Double> valuesList = measureOrNullList.getValue();
			if (valuesList != null)
				psBuilding.setString(15, Util.collection2string(valuesList, " "));
			else
				psBuilding.setNull(15, Types.VARCHAR);
		} else {
			psBuilding.setNull(15, Types.VARCHAR);
		}

		// Geometry
		// lodXSolid
		boolean[] lodGeometry = new boolean[4];

		for (int lod = 1; lod < 5; lod++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (lod) {
			case 1:
				solidProperty = building.getLod1Solid();
				break;
			case 2:
				solidProperty = building.getLod2Solid();
				break;
			case 3:
				solidProperty = building.getLod3Solid();
				break;
			case 4:
				solidProperty = building.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.getSolid() != null) {
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), buildingId);
				} else {
					// xlink
					String href = solidProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						buildingId,
        						DBTableEnum.BUILDING,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_GEOMETRY_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 1:
				if (solidGeometryId != 0) {
					psBuilding.setLong(18, solidGeometryId);
					lodGeometry[0] = true;
				}
				else
					psBuilding.setNull(18, 0);
				break;
			case 2:
				if (solidGeometryId != 0) {
					psBuilding.setLong(19, solidGeometryId);
					lodGeometry[1] = true;
				}
				else
					psBuilding.setNull(19, 0);
				break;
			case 3:
				if (solidGeometryId != 0) {
					psBuilding.setLong(20, solidGeometryId);
					lodGeometry[2] = true;
				}
				else
					psBuilding.setNull(20, 0);
				break;
			case 4:
				if (solidGeometryId != 0) {
					psBuilding.setLong(21, solidGeometryId);
					lodGeometry[3] = true;
				}
				else
					psBuilding.setNull(21, 0);
				break;
			}
		}

		// lodXMultiSurface
		for (int lod = 1; lod < 5; lod++) {
			if (lodGeometry[lod - 1])
				continue;

			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (lod) {
			case 1:
				multiSurfaceProperty = building.getLod1MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = building.getLod2MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = building.getLod3MultiSurface();
				break;
			case 4:
				multiSurfaceProperty = building.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.getMultiSurface() != null) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), buildingId);
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						buildingId,
        						DBTableEnum.BUILDING,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_GEOMETRY_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 1:
				if (multiGeometryId != 0)
					psBuilding.setLong(18, multiGeometryId);
				else
					psBuilding.setNull(18, 0);
				break;
			case 2:
				if (multiGeometryId != 0)
					psBuilding.setLong(19, multiGeometryId);
				else
					psBuilding.setNull(19, 0);
				break;
			case 3:
				if (multiGeometryId != 0)
					psBuilding.setLong(20, multiGeometryId);
				else
					psBuilding.setNull(20, 0);
				break;
			case 4:
				if (multiGeometryId != 0)
					psBuilding.setLong(21, multiGeometryId);
				else
					psBuilding.setNull(21, 0);
				break;
			}
		}

		// lodXTerrainIntersectionCurve
		for (int lod = 1; lod < 5; lod++) {
			
			MultiCurveProperty multiCurveProperty = null;
			JGeometry multiLine = null;
			
			switch (lod) {
			case 1:
				multiCurveProperty = building.getLod1TerainIntersection();
				break;
			case 2:
				multiCurveProperty = building.getLod2TerainIntersection();
				break;
			case 3:
				multiCurveProperty = building.getLod3TerainIntersection();
				break;
			case 4:
				multiCurveProperty = building.getLod4TerainIntersection();
				break;
			}
			
			if (multiCurveProperty != null)
				multiLine = sdoGeometry.getMultiCurve(multiCurveProperty);

			switch (lod) {
			case 1:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(22, multiLineObj);
				} else
					psBuilding.setNull(22, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 2:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(23, multiLineObj);
				} else
					psBuilding.setNull(23, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 3:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(24, multiLineObj);
				} else
					psBuilding.setNull(24, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 4:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(25, multiLineObj);
				} else
					psBuilding.setNull(25, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			}
			
		}
		
		// lodXMultiCurve
		for (int lod = 2; lod < 5; lod++) {
			
			MultiCurveProperty multiCurveProperty = null;
			JGeometry multiLine = null;
			
			switch (lod) {
			case 2:
				multiCurveProperty = building.getLod2MultiCurve();
				break;
			case 3:
				multiCurveProperty = building.getLod3MultiCurve();
				break;
			case 4:
				multiCurveProperty = building.getLod4MultiCurve();
				break;
			}
			
			if (multiCurveProperty != null)
				multiLine = sdoGeometry.getMultiCurve(multiCurveProperty);

			switch (lod) {
			case 2:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(26, multiLineObj);
				} else
					psBuilding.setNull(26, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 3:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(27, multiLineObj);
				} else
					psBuilding.setNull(27, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 4:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psBuilding.setObject(28, multiLineObj);
				} else
					psBuilding.setNull(28, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			}
			
		}
		
		psBuilding.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING);

		// BoundarySurfaces
		List<BoundarySurfaceProperty> boundarySurfacePropertyList = building.getBoundedBySurfaces();
		if (boundarySurfacePropertyList != null) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : boundarySurfacePropertyList) {
				if (boundarySurfaceProperty.getObject() != null) {
					long id = thematicSurfaceImporter.insert(boundarySurfaceProperty.getObject(), building.getCityGMLClass(), buildingId);
					if (id == 0)
						System.out.println("Could not write BoundarySurface");
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf BoundarySurface wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
				}
			}

			boundarySurfacePropertyList = null;
		}

		// BuildingInstallation
		List<BuildingInstallationProperty> buildingInstPropertyList = building.getOuterBuildingInstallation();
		if (buildingInstPropertyList != null) {
			for (BuildingInstallationProperty buildingInstProperty : buildingInstPropertyList) {
				if (buildingInstProperty.getObject() != null) {
					long id = buildingInstallationImporter.insert(buildingInstProperty.getObject(), building.getCityGMLClass(), buildingId);
					if (id == 0)
						System.out.println("Could not write BuildingInstallation");
				} else {
					// xlink
					String href = buildingInstProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf BuildingInstallation wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
				}
			}

			buildingInstPropertyList = null;
		}

		// IntBuildingInstallation
		List<IntBuildingInstallationProperty> intBuildingInstPropertyList = building.getInteriorBuildingInstallation();
		if (intBuildingInstPropertyList != null) {
			for (IntBuildingInstallationProperty intBuildingInstProperty : intBuildingInstPropertyList) {
				if (intBuildingInstProperty.getObject() != null) {
					long id = buildingInstallationImporter.insert(intBuildingInstProperty.getObject(), building.getCityGMLClass(), buildingId);
					if (id == 0)
						System.out.println("Could not write IntBuildingInstallation");
				} else {
					// xlink
					String href = intBuildingInstProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf IntBuildingInstallation wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
				}
			}

			intBuildingInstPropertyList = null;
		}

		// Room
		List<InteriorRoomProperty> roomPropertyList = building.getInteriorRoom();
		if (roomPropertyList != null) {
			for (InteriorRoomProperty roomProperty : roomPropertyList) {
				if (roomProperty.getObject() != null) {
					long id = roomImporter.insert(roomProperty.getObject(), buildingId);
					if (id == 0)
						System.out.println("Could not write IntBuildingInstallation");
				} else {
					// xlink
					String href = roomProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf Room wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
				}
			}

			roomPropertyList = null;
		}

		// BuildingPart
		List<BuildingPartProperty> buildingPartPropertyList = building.getConsistsOfBuildingPart();
		if (buildingPartPropertyList != null) {
			for (BuildingPartProperty buildingPartProperty : buildingPartPropertyList) {
				if (buildingPartProperty.getObject() != null) {
					long id = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
					if (id != 0)
						insert((AbstractBuilding)buildingPartProperty.getObject(), id, buildingId, rootId);
					else
						System.out.println("Could not write BuildingPart");
				} else {
					// xlink
					String href = buildingPartProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf BuildingPart wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
				}
			}

			buildingPartPropertyList = null;
		}
		
		// Address
		List<AddressProperty> addressPropertyList = building.getAddress();
		if (addressPropertyList != null) {
			for (AddressProperty addressProperty : addressPropertyList) {
				if (addressProperty.getObject() != null) {
					long id = addressImporter.insert(addressProperty.getObject(), buildingId);
					if (id == 0)
						System.out.println("Could not write Address");
				} else {
					// xlink
					String href = addressProperty.getHref();

        			if (href != null && href.length() != 0) {
        				dbImporterManager.propagateXlink(new DBXlinkBasic(
        						buildingId,
        						DBTableEnum.BUILDING,
        						href,
        						DBTableEnum.ADDRESS
        				));
        			}
				}
			}
		}

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBuilding.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BUILDING;
	}

}
