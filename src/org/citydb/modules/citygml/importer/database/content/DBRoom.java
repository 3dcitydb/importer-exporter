/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBRoom implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psRoom;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBBuildingFurniture buildingFurnitureImporter;
	private DBBuildingInstallation buildingInstallationImporter;

	private int batchCounter;

	public DBRoom(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into ROOM (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, BUILDING_ID, ")
		.append("LOD4_MULTI_SURFACE_ID, LOD4_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psRoom = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.THEMATIC_SURFACE);
		buildingFurnitureImporter = (DBBuildingFurniture)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING_FURNITURE);
		buildingInstallationImporter = (DBBuildingInstallation)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING_INSTALLATION);
	}

	public long insert(Room room, long buildingId) throws SQLException {
		long roomId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (roomId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(room, roomId);

		// Room
		// ID
		psRoom.setLong(1, roomId);

		// bldg:class
		if (room.isSetClazz() && room.getClazz().isSetValue()) {
			psRoom.setString(2, room.getClazz().getValue());
			psRoom.setString(3, room.getClazz().getCodeSpace());
		} else {
			psRoom.setNull(2, Types.VARCHAR);
			psRoom.setNull(3, Types.VARCHAR);
		}

		// bldg:function
		if (room.isSetFunction()) {
			String[] function = Util.codeList2string(room.getFunction());
			psRoom.setString(4, function[0]);
			psRoom.setString(5, function[1]);
		} else {
			psRoom.setNull(4, Types.VARCHAR);
			psRoom.setNull(5, Types.VARCHAR);
		}

		// bldg:usage
		if (room.isSetUsage()) {
			String[] usage = Util.codeList2string(room.getUsage());
			psRoom.setString(6, usage[0]);
			psRoom.setString(7, usage[1]);
		} else {
			psRoom.setNull(6, Types.VARCHAR);
			psRoom.setNull(7, Types.VARCHAR);
		}

		// BUILDING_ID
		psRoom.setLong(8, buildingId);

		// Geometry
		// lod4MultiSurface
		long geometryId = 0;
		
		if (room.isSetLod4MultiSurface()) {
			MultiSurfaceProperty multiSurfacePropery = room.getLod4MultiSurface();

			if (multiSurfacePropery.isSetMultiSurface()) {
				geometryId = surfaceGeometryImporter.insert(multiSurfacePropery.getMultiSurface(), roomId);
				multiSurfacePropery.unsetMultiSurface();
			} else {
				// xlink
				String href = multiSurfacePropery.getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
							href, 
							roomId, 
							TableEnum.ROOM, 
							"LOD4_MULTI_SURFACE_ID"));
				}
			}
		} 
		
		if (geometryId != 0)
			psRoom.setLong(9, geometryId);
		else
			psRoom.setNull(9, Types.NULL);

		// lod4Solid
		geometryId = 0;

		if (room.isSetLod4Solid()) {
			SolidProperty solidProperty = room.getLod4Solid();

			if (solidProperty.isSetSolid()) {
				geometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), roomId);
				solidProperty.unsetSolid();
			} else {
				// xlink
				String href = solidProperty.getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
							href, 
							roomId, 
							TableEnum.ROOM, 
							"LOD4_SOLID_ID"));
				}
			}
		} 
		
		if (geometryId != 0)
			psRoom.setLong(10, geometryId);
		else
			psRoom.setNull(10, Types.NULL);

		psRoom.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.ROOM);

		// BoundarySurfaces
		if (room.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : room.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, room.getCityGMLClass(), roomId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								room.getCityGMLClass(), 
								room.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					boundarySurfaceProperty.unsetBoundarySurface();
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BUILDING_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// IntBuildingInstallation
		if (room.isSetRoomInstallation()) {
			for (IntBuildingInstallationProperty intBuildingInstProperty : room.getRoomInstallation()) {
				IntBuildingInstallation intBuildingInst = intBuildingInstProperty.getObject();

				if (intBuildingInst != null) {
					String gmlId = intBuildingInst.getId();
					long id = buildingInstallationImporter.insert(intBuildingInst, room.getCityGMLClass(), roomId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								room.getCityGMLClass(), 
								room.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								intBuildingInst.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					intBuildingInstProperty.unsetIntBuildingInstallation();
				} else {
					// xlink
					String href = intBuildingInstProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.INT_BUILDING_INSTALLATION + " feature is not supported.");
					}
				}
			}
		}

		// BuildingFurniture
		if (room.isSetInteriorFurniture()) {
			for (InteriorFurnitureProperty intFurnitureProperty : room.getInteriorFurniture()) {
				BuildingFurniture furniture = intFurnitureProperty.getObject();

				if (furniture != null) {
					String gmlId = furniture.getId();
					long id = buildingFurnitureImporter.insert(furniture, roomId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								room.getCityGMLClass(), 
								room.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								furniture.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					intFurnitureProperty.unsetBuildingFurniture();
				} else {
					// xlink
					String href = intFurnitureProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BUILDING_FURNITURE + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(room, roomId);

		return roomId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psRoom.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psRoom.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.ROOM;
	}

}
