package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import de.tub.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import de.tub.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import de.tub.citygml4j.model.citygml.building.Room;
import de.tub.citygml4j.model.gml.MultiSurfaceProperty;
import de.tub.citygml4j.model.gml.SolidProperty;

public class DBRoom implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psRoom;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBBuildingFurniture buildingFurnitureImporter;
	private DBBuildingInstallation buildingInstallationImporter;

	private String gmlNameDelimiter;
	private int batchCounter;
	
	public DBRoom(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		
		psRoom = batchConn.prepareStatement("insert into ROOM (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, BUILDING_ID, LOD4_GEOMETRY_ID) values "+
				"(?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.THEMATIC_SURFACE);
		buildingFurnitureImporter = (DBBuildingFurniture)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING_FURNITURE);
		buildingInstallationImporter = (DBBuildingInstallation)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING_INSTALLATION);
	}

	public long insert(Room room, long buildingId) throws SQLException {
		long roomId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (roomId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(room, roomId);

		// Room
		// ID
		psRoom.setLong(1, roomId);

		// gml:name
		if (room.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(room, gmlNameDelimiter);

			psRoom.setString(2, dbGmlName[0]);
			psRoom.setString(3, dbGmlName[1]);
		} else {
			psRoom.setNull(2, Types.VARCHAR);
			psRoom.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (room.getDescription() != null) {
			String description = room.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psRoom.setString(4, description);
		} else {
			psRoom.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (room.getClazz() != null)
			psRoom.setString(5, room.getClazz().trim());
		else
			psRoom.setNull(5, Types.VARCHAR);

		// citygml:function
		if (room.getFunction() != null) {
			List<String> functionList = room.getFunction();
			psRoom.setString(6, Util.collection2string(functionList, " "));
		} else {
			psRoom.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (room.getUsage() != null) {
			List<String> usageList = room.getUsage();
			psRoom.setString(7, Util.collection2string(usageList, " "));
		} else {
			psRoom.setNull(7, Types.VARCHAR);
		}

		// BUILDING_ID
		psRoom.setLong(8, buildingId);

		// Geometry
		MultiSurfaceProperty multiSurfacePropery = room.getLod4MultiSurface();
		SolidProperty solidProperty = room.getLod4Solid();

		if (multiSurfacePropery != null && solidProperty != null) {
			System.out.println("Lod4MultiSurface and Lod4Solid is set for room. Just writing LoD4MultiSurface");
			solidProperty = null;
		}

		long geometryId = 0;

		if (multiSurfacePropery != null) {
			if (multiSurfacePropery.getMultiSurface() != null) {
				geometryId = surfaceGeometryImporter.insert(multiSurfacePropery.getMultiSurface(), roomId);
			} else {
				// xlink
				String href = multiSurfacePropery.getHref();

    			if (href != null && href.length() != 0) {
    				DBXlinkBasic xlink = new DBXlinkBasic(
    						roomId,
    						DBTableEnum.ROOM,
    						href,
    						DBTableEnum.SURFACE_GEOMETRY
    				);

    				xlink.setAttrName("LOD4_GEOMETRY_ID");
    				dbImporterManager.propagateXlink(xlink);
    			}
			}
		} else if (solidProperty != null) {
			if (solidProperty.getSolid() != null) {
				geometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), roomId);
			} else {
				// xlink
				String href = solidProperty.getHref();

    			if (href != null && href.length() != 0) {
    				DBXlinkBasic xlink = new DBXlinkBasic(
    						roomId,
    						DBTableEnum.ROOM,
    						href,
    						DBTableEnum.SURFACE_GEOMETRY
    				);

    				xlink.setAttrName("LOD4_GEOMETRY_ID");
    				dbImporterManager.propagateXlink(xlink);
    			}
			}
		}

		if (geometryId != 0)
			psRoom.setLong(9, geometryId);
		else
			psRoom.setNull(9, 0);

		psRoom.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.ROOM);

		// BoundarySurfaces
		List<BoundarySurfaceProperty> boundarySurfacePropertyList = room.getBoundedBySurfaces();
		if (boundarySurfacePropertyList != null) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : boundarySurfacePropertyList) {
				if (boundarySurfaceProperty.getObject() != null) {
					long id = thematicSurfaceImporter.insert(boundarySurfaceProperty.getObject(), room.getCityGMLClass(), roomId);
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

		// IntBuildingInstallation
		List<IntBuildingInstallationProperty> intBuildingInstPropertyList = room.getRoomInstallation();
		if (intBuildingInstPropertyList != null) {
			for (IntBuildingInstallationProperty intBuildingInstProperty : intBuildingInstPropertyList) {
				if (intBuildingInstProperty.getObject() != null) {
					long id = buildingInstallationImporter.insert(intBuildingInstProperty.getObject(), room.getCityGMLClass(), roomId);
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

		// BuildingFurniture
		List<InteriorFurnitureProperty> intFurniturePropertyList = room.getInteriorFurniture();
		if (intFurniturePropertyList != null) {
			for (InteriorFurnitureProperty intFurnitureProperty : intFurniturePropertyList) {
				if (intFurnitureProperty.getObject() != null) {
					long id = buildingFurnitureImporter.insert(intFurnitureProperty.getObject(), roomId);
					if (id == 0)
						System.out.println("Could not write BuildingFurniture");
				} else {
					// xlink
					String href = intFurnitureProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf BuildingFurniture wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
				}
			}

			intFurniturePropertyList = null;
		}

		return roomId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psRoom.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.ROOM;
	}

}
