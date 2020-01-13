/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBRoom implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psRoom;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBBuildingFurniture buildingFurnitureImporter;
	private DBBuildingInstallation buildingInstallationImporter;
	private AttributeValueJoiner valueJoiner;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;

	public DBRoom(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".room (id, class, class_codespace, function, function_codespace, usage, usage_codespace, building_id, " +
				"lod4_multi_surface_id, lod4_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psRoom = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBThematicSurface.class);
		buildingFurnitureImporter = importer.getImporter(DBBuildingFurniture.class);
		buildingInstallationImporter = importer.getImporter(DBBuildingInstallation.class);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(Room room) throws CityGMLImportException, SQLException {
		return doImport(room, 0);
	}

	public long doImport(Room room, long buildingId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(room);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long roomId = cityObjectImporter.doImport(room, featureType);

		// import room information
		// primary id
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
			valueJoiner.join(room.getFunction(), Code::getValue, Code::getCodeSpace);
			psRoom.setString(4, valueJoiner.result(0));
			psRoom.setString(5, valueJoiner.result(1));
		} else {
			psRoom.setNull(4, Types.VARCHAR);
			psRoom.setNull(5, Types.VARCHAR);
		}

		// bldg:usage
		if (room.isSetUsage()) {
			valueJoiner.join(room.getUsage(), Code::getValue, Code::getCodeSpace);
			psRoom.setString(6, valueJoiner.result(0));
			psRoom.setString(7, valueJoiner.result(1));
		} else {
			psRoom.setNull(6, Types.VARCHAR);
			psRoom.setNull(7, Types.VARCHAR);
		}

		// parent building id
		if (buildingId != 0)
			psRoom.setLong(8, buildingId);
		else
			psRoom.setNull(8, Types.NULL);

		// bldg:lod4MultiSurface
		long geometryId = 0;
		if (room.isSetLod4MultiSurface()) {
			MultiSurfaceProperty multiSurfacePropery = room.getLod4MultiSurface();

			if (multiSurfacePropery.isSetMultiSurface()) {
				geometryId = surfaceGeometryImporter.doImport(multiSurfacePropery.getMultiSurface(), roomId);
				multiSurfacePropery.unsetMultiSurface();
			} else {
				String href = multiSurfacePropery.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.ROOM.getName(),
							roomId, 
							href, 
							"lod4_multi_surface_id"));
				}
			}
		} 

		if (geometryId != 0)
			psRoom.setLong(9, geometryId);
		else
			psRoom.setNull(9, Types.NULL);

		// bldg:lod4Solid
		geometryId = 0;
		if (room.isSetLod4Solid()) {
			SolidProperty solidProperty = room.getLod4Solid();

			if (solidProperty.isSetSolid()) {
				geometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), roomId);
				solidProperty.unsetSolid();
			} else {
				String href = solidProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.ROOM.getName(),
							roomId,
							href, 
							"lod4_solid_id"));
				}
			}
		} 

		if (geometryId != 0)
			psRoom.setLong(10, geometryId);
		else
			psRoom.setNull(10, Types.NULL);

		// objectclass id
		if (hasObjectClassIdColumn)
			psRoom.setLong(11, featureType.getObjectClassId());

		psRoom.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.ROOM);

		// bldg:boundedBy
		if (room.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : room.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, room, roomId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.THEMATIC_SURFACE.getName(),
								href,
								roomId,
								"room_id"));
					}
				}
			}
		}

		// bldg:roomInstallation
		if (room.isSetRoomInstallation()) {
			for (IntBuildingInstallationProperty property : room.getRoomInstallation()) {
				IntBuildingInstallation installation = property.getIntBuildingInstallation();

				if (installation != null) {
					buildingInstallationImporter.doImport(installation, room, roomId);
					property.unsetIntBuildingInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BUILDING_INSTALLATION.getName(),
								href,
								roomId,
								"room_id"));
					}
				}
			}
		}

		// bldg:interiorFurniture
		if (room.isSetInteriorFurniture()) {
			for (InteriorFurnitureProperty property : room.getInteriorFurniture()) {
				BuildingFurniture furniture = property.getBuildingFurniture();

				if (furniture != null) {
					buildingFurnitureImporter.doImport(furniture, roomId);
					property.unsetBuildingFurniture();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BUILDING_INSTALLATION.getName(),
								href,
								roomId,
								"room_id"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(room, roomId, featureType);

		return roomId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psRoom.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psRoom.close();
	}

}
