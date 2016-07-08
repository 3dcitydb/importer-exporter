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
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.InteriorFurnitureProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBBridgeRoom implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psRoom;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBBridgeThematicSurface thematicSurfaceImporter;
	private DBBridgeFurniture bridgeFurnitureImporter;
	private DBBridgeInstallation bridgeInstallationImporter;

	private int batchCounter;

	public DBBridgeRoom(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into BRIDGE_ROOM (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, BRIDGE_ID, ")
		.append("LOD4_MULTI_SURFACE_ID, LOD4_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psRoom = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBBridgeThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_THEMATIC_SURFACE);
		bridgeFurnitureImporter = (DBBridgeFurniture)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_FURNITURE);
		bridgeInstallationImporter = (DBBridgeInstallation)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_INSTALLATION);
	}

	public long insert(BridgeRoom bridgeRoom, long bridgeId) throws SQLException {
		long bridgeRoomId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (bridgeRoomId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(bridgeRoom, bridgeRoomId);

		// BridgeRoom
		// ID
		psRoom.setLong(1, bridgeRoomId);

		// brid:class
		if (bridgeRoom.isSetClazz() && bridgeRoom.getClazz().isSetValue()) {
			psRoom.setString(2, bridgeRoom.getClazz().getValue());
			psRoom.setString(3, bridgeRoom.getClazz().getCodeSpace());
		} else {
			psRoom.setNull(2, Types.VARCHAR);
			psRoom.setNull(3, Types.VARCHAR);
		}

		// brid:function
		if (bridgeRoom.isSetFunction()) {
			String[] function = Util.codeList2string(bridgeRoom.getFunction());
			psRoom.setString(4, function[0]);
			psRoom.setString(5, function[1]);
		} else {
			psRoom.setNull(4, Types.VARCHAR);
			psRoom.setNull(5, Types.VARCHAR);
		}

		// brid:usage
		if (bridgeRoom.isSetUsage()) {
			String[] usage = Util.codeList2string(bridgeRoom.getUsage());
			psRoom.setString(6, usage[0]);
			psRoom.setString(7, usage[1]);
		} else {
			psRoom.setNull(6, Types.VARCHAR);
			psRoom.setNull(7, Types.VARCHAR);
		}

		// BRIDGE_ID
		psRoom.setLong(8, bridgeId);

		// Geometry
		// lod4MultiSurface
		long geometryId = 0;
		
		if (bridgeRoom.isSetLod4MultiSurface()) {
			MultiSurfaceProperty multiSurfacePropery = bridgeRoom.getLod4MultiSurface();

			if (multiSurfacePropery.isSetMultiSurface()) {
				geometryId = surfaceGeometryImporter.insert(multiSurfacePropery.getMultiSurface(), bridgeRoomId);
				multiSurfacePropery.unsetMultiSurface();
			} else {
				// xlink
				String href = multiSurfacePropery.getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
							href, 
							bridgeRoomId, 
							TableEnum.BRIDGE_ROOM, 
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

		if (bridgeRoom.isSetLod4Solid()) {
			SolidProperty solidProperty = bridgeRoom.getLod4Solid();

			if (solidProperty.isSetSolid()) {
				geometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), bridgeRoomId);
				solidProperty.unsetSolid();
			} else {
				// xlink
				String href = solidProperty.getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
							href, 
							bridgeRoomId, 
							TableEnum.BRIDGE_ROOM, 
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
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE_ROOM);

		// BoundarySurfaces
		if (bridgeRoom.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : bridgeRoom.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, bridgeRoom.getCityGMLClass(), bridgeRoomId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeRoom.getCityGMLClass(), 
								bridgeRoom.getId()));
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
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BRIDGE_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// IntBuildingInstallation
		if (bridgeRoom.isSetBridgeRoomInstallation()) {
			for (IntBridgeInstallationProperty intBridgeInstProperty : bridgeRoom.getBridgeRoomInstallation()) {
				IntBridgeInstallation intBrigdeInst = intBridgeInstProperty.getObject();

				if (intBrigdeInst != null) {
					String gmlId = intBrigdeInst.getId();
					long id = bridgeInstallationImporter.insert(intBrigdeInst, bridgeRoom.getCityGMLClass(), bridgeRoomId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeRoom.getCityGMLClass(), 
								bridgeRoom.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								intBrigdeInst.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					intBridgeInstProperty.unsetIntBridgeInstallation();
				} else {
					// xlink
					String href = intBridgeInstProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.INT_BRIDGE_INSTALLATION + " feature is not supported.");
					}
				}
			}
		}

		// BuildingFurniture
		if (bridgeRoom.isSetInteriorFurniture()) {
			for (InteriorFurnitureProperty intFurnitureProperty : bridgeRoom.getInteriorFurniture()) {
				BridgeFurniture furniture = intFurnitureProperty.getObject();

				if (furniture != null) {
					String gmlId = furniture.getId();
					long id = bridgeFurnitureImporter.insert(furniture, bridgeRoomId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeRoom.getCityGMLClass(), 
								bridgeRoom.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								furniture.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					intFurnitureProperty.unsetBridgeFurniture();
				} else {
					// xlink
					String href = intFurnitureProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BRIDGE_FURNITURE + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(bridgeRoom, bridgeRoomId);

		return bridgeRoomId;
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
		return DBImporterEnum.BRIDGE_ROOM;
	}

}
