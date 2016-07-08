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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElementProperty;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.BridgePart;
import org.citygml4j.model.citygml.bridge.BridgePartProperty;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.InteriorBridgeRoomProperty;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBBridge implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBridge;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBBridgeThematicSurface thematicSurfaceImporter;
	private DBBridgeConstrElement bridgeConstructionImporter;
	private DBBridgeInstallation bridgeInstallationImporter;
	private DBBridgeRoom roomImporter;
	private DBAddress addressImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;	

	public DBBridge(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into BRIDGE (ID, BRIDGE_PARENT_ID, BRIDGE_ROOT_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, YEAR_OF_CONSTRUCTION, YEAR_OF_DEMOLITION, IS_MOVABLE, ")
		.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, LOD2_MULTI_CURVE, LOD3_MULTI_CURVE, LOD4_MULTI_CURVE, ")
		.append("LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psBridge = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBBridgeThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_THEMATIC_SURFACE);
		bridgeConstructionImporter = (DBBridgeConstrElement)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_CONSTR_ELEMENT);
		bridgeInstallationImporter = (DBBridgeInstallation)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_INSTALLATION);
		roomImporter = (DBBridgeRoom)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_ROOM);
		addressImporter = (DBAddress)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(AbstractBridge bridge) throws SQLException {
		long bridgeId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (bridgeId != 0)
			success = insert(bridge, bridgeId, 0, bridgeId);

		if (success)
			return bridgeId;
		else
			return 0;
	}

	public boolean insert(AbstractBridge bridge,
			long bridgeId,
			long parentId,
			long rootId) throws SQLException {
		String origGmlId = bridge.getId();

		// CityObject
		long cityObjectId = cityObjectImporter.insert(bridge, bridgeId, parentId == 0);
		if (cityObjectId == 0)
			return false;

		// Bridge
		// ID
		psBridge.setLong(1, bridgeId);

		// BRIDGE_PARENT_ID
		if (parentId != 0)
			psBridge.setLong(2, parentId);
		else
			psBridge.setNull(2, Types.NULL);

		// BRIDGE_ROOT_ID
		psBridge.setLong(3, rootId);

		// brid:class
		if (bridge.isSetClazz() && bridge.getClazz().isSetValue()) {
			psBridge.setString(4, bridge.getClazz().getValue());
			psBridge.setString(5, bridge.getClazz().getCodeSpace());
		} else {
			psBridge.setNull(4, Types.VARCHAR);
			psBridge.setNull(5, Types.VARCHAR);
		}

		// brid:function
		if (bridge.isSetFunction()) {
			String[] function = Util.codeList2string(bridge.getFunction());
			psBridge.setString(6, function[0]);
			psBridge.setString(7, function[1]);
		} else {
			psBridge.setNull(6, Types.VARCHAR);
			psBridge.setNull(7, Types.VARCHAR);
		}

		// brid:usage
		if (bridge.isSetUsage()) {
			String[] usage = Util.codeList2string(bridge.getUsage());
			psBridge.setString(8, usage[0]);
			psBridge.setString(9, usage[1]);
		} else {
			psBridge.setNull(8, Types.VARCHAR);
			psBridge.setNull(9, Types.VARCHAR);
		}

		// brid:yearOfConstruction
		if (bridge.isSetYearOfConstruction()) {
			psBridge.setDate(10, new Date(bridge.getYearOfConstruction().getTime().getTime()));
		} else {
			psBridge.setNull(10, Types.DATE);
		}

		// brid:yearOfDemolition
		if (bridge.isSetYearOfDemolition()) {
			psBridge.setDate(11, new Date(bridge.getYearOfDemolition().getTime().getTime()));
		} else {
			psBridge.setNull(11, Types.DATE);
		}

		// brid:isMovable
		if (bridge.isSetIsMovable())
			psBridge.setInt(12, bridge.getIsMovable() ? 1 : 0);
		else
			psBridge.setNull(12, Types.NULL);

		// Geometry
		// lodXTerrainIntersectionCurve
		for (int i = 0; i < 4; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = bridge.getLod1TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = bridge.getLod2TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = bridge.getLod3TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = bridge.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBridge.setObject(13 + i, multiLineObj);
			} else
				psBridge.setNull(13 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXMultiCurve
		for (int i = 0; i < 3; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = bridge.getLod2MultiCurve();
				break;
			case 1:
				multiCurveProperty = bridge.getLod3MultiCurve();
				break;
			case 2:
				multiCurveProperty = bridge.getLod4MultiCurve();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBridge.setObject(17 + i, multiLineObj);
			} else
				psBridge.setNull(17 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXMultiSurface
		for (int i = 0; i < 4; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = bridge.getLod1MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = bridge.getLod2MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = bridge.getLod3MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = bridge.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), bridgeId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								bridgeId, 
								TableEnum.BRIDGE, 
								"LOD" + (i + 1) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psBridge.setLong(20 + i, multiGeometryId);
			else
				psBridge.setNull(20 + i, Types.NULL);
		}

		// lodXSolid
		for (int i = 0; i < 4; i++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (i) {
			case 0:
				solidProperty = bridge.getLod1Solid();
				break;
			case 1:
				solidProperty = bridge.getLod2Solid();
				break;
			case 2:
				solidProperty = bridge.getLod3Solid();
				break;
			case 3:
				solidProperty = bridge.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), bridgeId);
					solidProperty.unsetSolid();
				} else {
					// xlink
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								bridgeId, 
								TableEnum.BRIDGE, 
								"LOD" + (i + 1) + "_SOLID_ID"));
					}
				}
			}

			if (solidGeometryId != 0)
				psBridge.setLong(24 + i, solidGeometryId);
			else
				psBridge.setNull(24 + i, Types.NULL);
		}

		psBridge.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE);

		// BoundarySurfaces
		if (bridge.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : bridge.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, bridge.getCityGMLClass(), bridgeId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
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

		// BridgeConstructionElement
		if (bridge.isSetOuterBridgeConstructionElement()) {
			for (BridgeConstructionElementProperty constructionElementProperty : bridge.getOuterBridgeConstructionElement()) {
				BridgeConstructionElement constructionElement = constructionElementProperty.getBridgeConstructionElement();

				if (constructionElement != null) {
					String gmlId = constructionElement.getId();
					long id = bridgeConstructionImporter.insert(constructionElement, bridge.getCityGMLClass(), bridgeId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								constructionElement.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					constructionElementProperty.unsetBridgeConstructionElement();
				} else {
					// xlink
					String href = constructionElementProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BRIDGE_CONSTRUCTION_ELEMENT + " feature is not supported.");
					}
				}
			}
		}

		// BridgeInstallation
		if (bridge.isSetOuterBridgeInstallation()) {
			for (BridgeInstallationProperty bridgeInstProperty : bridge.getOuterBridgeInstallation()) {
				BridgeInstallation bridgeInst = bridgeInstProperty.getBridgeInstallation();

				if (bridgeInst != null) {
					String gmlId = bridgeInst.getId();
					long id = bridgeInstallationImporter.insert(bridgeInst, bridge.getCityGMLClass(), bridgeId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.BRIDGE_INSTALLATION, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					bridgeInstProperty.unsetBridgeInstallation();
				} else {
					// xlink
					String href = bridgeInstProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BRIDGE_INSTALLATION + " feature is not supported.");
					}
				}
			}
		}

		// IntBridgeInstallation
		if (bridge.isSetInteriorBridgeInstallation()) {
			for (IntBridgeInstallationProperty intBridgeInstProperty : bridge.getInteriorBridgeInstallation()) {
				IntBridgeInstallation intBridgeInst = intBridgeInstProperty.getIntBridgeInstallation();

				if (intBridgeInst != null) {
					String gmlId = intBridgeInst.getId();
					long id = bridgeInstallationImporter.insert(intBridgeInst, bridge.getCityGMLClass(), bridgeId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.INT_BRIDGE_INSTALLATION, 
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

		// Room
		if (bridge.isSetInteriorBridgeRoom()) {
			for (InteriorBridgeRoomProperty bridgeRoomProperty : bridge.getInteriorBridgeRoom()) {
				BridgeRoom bridgeRoom = bridgeRoomProperty.getBridgeRoom();

				if (bridgeRoom != null) {
					String gmlId = bridgeRoom.getId();
					long id = roomImporter.insert(bridgeRoom, bridgeId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.BRIDGE_ROOM, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					bridgeRoomProperty.unsetBridgeRoom();
				} else {
					// xlink
					String href = bridgeRoomProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BRIDGE_ROOM + " feature is not supported.");
					}
				}
			}
		}

		// BridgePart
		if (bridge.isSetConsistsOfBridgePart()) {
			for (BridgePartProperty bridgePartProperty : bridge.getConsistsOfBridgePart()) {
				BridgePart bridgePart = bridgePartProperty.getBridgePart();

				if (bridgePart != null) {
					long id = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);

					if (id != 0)
						insert(bridgePart, id, bridgeId, rootId);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.BRIDGE_PART, 
								bridgePart.getId()));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					bridgePartProperty.unsetBridgePart();
				} else {
					// xlink
					String href = bridgePartProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BRIDGE_PART + " feature is not supported.");
					}
				}
			}
		}

		// Address
		if (bridge.isSetAddress()) {
			for (AddressProperty addressProperty : bridge.getAddress()) {
				Address address = addressProperty.getAddress();

				if (address != null) {
					String gmlId = address.getId();
					long id = addressImporter.insertBridgeAddress(address, bridgeId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridge.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.ADDRESS, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					addressProperty.unsetAddress();
				} else {
					// xlink
					String href = addressProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								bridgeId,
								TableEnum.BRIDGE,
								href,
								TableEnum.ADDRESS
								));
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(bridge, bridgeId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBridge.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBridge.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BRIDGE;
	}

}
