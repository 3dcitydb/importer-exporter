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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
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
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBBridge implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psBridge;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBBridgeThematicSurface thematicSurfaceImporter;
	private DBBridgeConstrElement bridgeConstructionImporter;
	private DBBridgeInstallation bridgeInstallationImporter;
	private DBBridgeRoom roomImporter;
	private DBAddress addressImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean hasObjectClassIdColumn;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBBridge(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".bridge (id, bridge_parent_id, bridge_root_id, class, class_codespace, function, function_codespace, usage, usage_codespace, year_of_construction, year_of_demolition, is_movable, " +
				"lod1_terrain_intersection, lod2_terrain_intersection, lod3_terrain_intersection, lod4_terrain_intersection, lod2_multi_curve, lod3_multi_curve, lod4_multi_curve, " +
				"lod1_multi_surface_id, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id, " +
				"lod1_solid_id, lod2_solid_id, lod3_solid_id, lod4_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psBridge = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBBridgeThematicSurface.class);
		bridgeConstructionImporter = importer.getImporter(DBBridgeConstrElement.class);
		bridgeInstallationImporter = importer.getImporter(DBBridgeInstallation.class);
		roomImporter = importer.getImporter(DBBridgeRoom.class);
		addressImporter = importer.getImporter(DBAddress.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(AbstractBridge bridge) throws CityGMLImportException, SQLException {
		return doImport(bridge, 0, 0);
	}

	public long doImport(AbstractBridge bridge, long parentId, long rootId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(bridge);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long bridgeId = cityObjectImporter.doImport(bridge, featureType);
		if (rootId == 0)
			rootId = bridgeId;

		// import bridge information
		// primary id
		psBridge.setLong(1, bridgeId);

		// parent bridge id
		if (parentId != 0)
			psBridge.setLong(2, parentId);
		else
			psBridge.setNull(2, Types.NULL);

		// root building id
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
			valueJoiner.join(bridge.getFunction(), Code::getValue, Code::getCodeSpace);
			psBridge.setString(6, valueJoiner.result(0));
			psBridge.setString(7, valueJoiner.result(1));
		} else {
			psBridge.setNull(6, Types.VARCHAR);
			psBridge.setNull(7, Types.VARCHAR);
		}

		// brid:usage
		if (bridge.isSetUsage()) {
			valueJoiner.join(bridge.getUsage(), Code::getValue, Code::getCodeSpace);
			psBridge.setString(8, valueJoiner.result(0));
			psBridge.setString(9, valueJoiner.result(1));
		} else {
			psBridge.setNull(8, Types.VARCHAR);
			psBridge.setNull(9, Types.VARCHAR);
		}

		// brid:yearOfConstruction
		if (bridge.isSetYearOfConstruction()) {
			psBridge.setDate(10, Date.valueOf(bridge.getYearOfConstruction()));
		} else {
			psBridge.setNull(10, Types.DATE);
		}

		// brid:yearOfDemolition
		if (bridge.isSetYearOfDemolition()) {
			psBridge.setDate(11, Date.valueOf(bridge.getYearOfDemolition()));
		} else {
			psBridge.setNull(11, Types.DATE);
		}

		// brid:isMovable
		if (bridge.isSetIsMovable())
			psBridge.setInt(12, bridge.getIsMovable() ? 1 : 0);
		else
			psBridge.setNull(12, Types.NULL);

		// brid:lodXTerrainIntersectionCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBridge.setObject(13 + i, multiLineObj);
			} else
				psBridge.setNull(13 + i, nullGeometryType, nullGeometryTypeName);
		}

		// brid:lodXMultiCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBridge.setObject(17 + i, multiLineObj);
			} else
				psBridge.setNull(17 + i, nullGeometryType, nullGeometryTypeName);
		}

		// brid:lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), bridgeId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BRIDGE.getName(),
								bridgeId, 
								href, 
								"lod" + (i + 1) + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psBridge.setLong(20 + i, multiGeometryId);
			else
				psBridge.setNull(20 + i, Types.NULL);
		}

		// brid:lodXSolid
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
					solidGeometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), bridgeId);
					solidProperty.unsetSolid();
				} else {
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BRIDGE.getName(),
								bridgeId, 
								href, 
								"lod" + (i + 1) + "_solid_id"));
					}
				}
			}

			if (solidGeometryId != 0)
				psBridge.setLong(24 + i, solidGeometryId);
			else
				psBridge.setNull(24 + i, Types.NULL);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psBridge.setLong(28, featureType.getObjectClassId());

		psBridge.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BRIDGE);

		// brid:boundedBy
		if (bridge.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : bridge.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, bridge, bridgeId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_THEMATIC_SURFACE.getName(),
								href,
								bridgeId,
								"bridge_id"));
					}
				}
			}
		}

		// brid:outerBridgeConstructionElement
		if (bridge.isSetOuterBridgeConstructionElement()) {
			for (BridgeConstructionElementProperty property : bridge.getOuterBridgeConstructionElement()) {
				BridgeConstructionElement construction = property.getBridgeConstructionElement();

				if (construction != null) {
					bridgeConstructionImporter.doImport(construction, bridge, bridgeId);
					property.unsetBridgeConstructionElement();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_CONSTR_ELEMENT.getName(),
								href,
								bridgeId,
								"bridge_id"));
					}
				}
			}
		}

		// bridg:outerBridgeInstallation
		if (bridge.isSetOuterBridgeInstallation()) {
			for (BridgeInstallationProperty property : bridge.getOuterBridgeInstallation()) {
				BridgeInstallation installation = property.getBridgeInstallation();

				if (installation != null) {
					bridgeInstallationImporter.doImport(installation, bridge, bridgeId);
					property.unsetBridgeInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_INSTALLATION.getName(),
								href,
								bridgeId,
								"bridge_id"));
					}
				}
			}
		}

		// brid:interiorBridgeInstallation
		if (bridge.isSetInteriorBridgeInstallation()) {
			for (IntBridgeInstallationProperty property : bridge.getInteriorBridgeInstallation()) {
				IntBridgeInstallation installation = property.getIntBridgeInstallation();

				if (installation != null) {
					bridgeInstallationImporter.doImport(installation, bridge, bridgeId);
					property.unsetIntBridgeInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_INSTALLATION.getName(),
								href,
								bridgeId,
								"bridge_id"));
					}
				}
			}
		}

		// brid:interiorBridgeRoom
		if (bridge.isSetInteriorBridgeRoom()) {
			for (InteriorBridgeRoomProperty property : bridge.getInteriorBridgeRoom()) {
				BridgeRoom room = property.getBridgeRoom();

				if (room != null) {
					roomImporter.doImport(room, bridgeId);
					property.unsetBridgeRoom();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_ROOM.getName(),
								href,
								bridgeId,
								"bridge_id"));
					}
				}
			}
		}

		// brid:consistsOfBridgePart
		if (bridge.isSetConsistsOfBridgePart()) {
			for (BridgePartProperty property : bridge.getConsistsOfBridgePart()) {
				BridgePart bridgePart = property.getBridgePart();

				if (bridgePart != null) {
					doImport(bridgePart, bridgeId, rootId);
					property.unsetBridgePart();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0)
						importer.logOrThrowUnsupportedXLinkMessage(bridge, BridgePart.class, href);
				}
			}
		}

		// bridg:address
		if (bridge.isSetAddress()) {
			for (AddressProperty property : bridge.getAddress()) {
				Address address = property.getAddress();

				if (address != null) {
					addressImporter.importBridgeAddress(address, bridgeId);
					property.unsetAddress();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.ADDRESS_TO_BRIDGE.getName(),
								bridgeId,
								"BRIDGE_ID",
								href,
								"ADDRESS_ID"));
					}
				}
			}
		}

		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(bridge, bridgeId, featureType);

		return bridgeId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBridge.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBridge.close();
	}

}
