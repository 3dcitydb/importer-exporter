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
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBBuilding implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psBuilding;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBBuildingInstallation buildingInstallationImporter;
	private DBRoom roomImporter;
	private DBAddress addressImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;	
	private int batchCounter;

	private boolean hasObjectClassIdColumn;
	private int nullGeometryType;
	private String nullGeometryTypeName;	

	public DBBuilding(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".building (id, building_parent_id, building_root_id, class, class_codespace, function, function_codespace, usage, usage_codespace, year_of_construction, year_of_demolition, " +
				"roof_type, roof_type_codespace, measured_height, measured_height_unit, storeys_above_ground, storeys_below_ground, storey_heights_above_ground, storey_heights_ag_unit, storey_heights_below_ground, storey_heights_bg_unit, " +
				"lod1_terrain_intersection, lod2_terrain_intersection, lod3_terrain_intersection, lod4_terrain_intersection, lod2_multi_curve, lod3_multi_curve, lod4_multi_curve, " +
				"lod0_footprint_id, lod0_roofprint_id, lod1_multi_surface_id, lod2_multi_surface_id, lod3_multi_surface_id, lod4_multi_surface_id, " +
				"lod1_solid_id, lod2_solid_id, lod3_solid_id, lod4_solid_id" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psBuilding = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBThematicSurface.class);
		buildingInstallationImporter = importer.getImporter(DBBuildingInstallation.class);
		roomImporter = importer.getImporter(DBRoom.class);
		addressImporter = importer.getImporter(DBAddress.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(AbstractBuilding building) throws CityGMLImportException, SQLException {
		return doImport(building, 0, 0);
	}

	public long doImport(AbstractBuilding building, long parentId, long rootId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(building);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long buildingId = cityObjectImporter.doImport(building, featureType);
		if (rootId == 0)
			rootId = buildingId;

		// import building information
		// primary id
		psBuilding.setLong(1, buildingId);

		// parent building id
		if (parentId != 0)
			psBuilding.setLong(2, parentId);
		else
			psBuilding.setNull(2, Types.NULL);

		// root building id
		psBuilding.setLong(3, rootId);

		// bldg:class
		if (building.isSetClazz() && building.getClazz().isSetValue()) {
			psBuilding.setString(4, building.getClazz().getValue());
			psBuilding.setString(5, building.getClazz().getCodeSpace());
		} else {
			psBuilding.setNull(4, Types.VARCHAR);
			psBuilding.setNull(5, Types.VARCHAR);
		}

		// bldg:function
		if (building.isSetFunction()) {
			valueJoiner.join(building.getFunction(), Code::getValue, Code::getCodeSpace);
			psBuilding.setString(6, valueJoiner.result(0));
			psBuilding.setString(7, valueJoiner.result(1));
		} else {
			psBuilding.setNull(6, Types.VARCHAR);
			psBuilding.setNull(7, Types.VARCHAR);
		}

		// bldg:usage
		if (building.isSetUsage()) {
			valueJoiner.join(building.getUsage(), Code::getValue, Code::getCodeSpace);
			psBuilding.setString(8, valueJoiner.result(0));
			psBuilding.setString(9, valueJoiner.result(1));
		} else {
			psBuilding.setNull(8, Types.VARCHAR);
			psBuilding.setNull(9, Types.VARCHAR);
		}

		// bldg:yearOfConstruction
		if (building.isSetYearOfConstruction()) {
			psBuilding.setDate(10, Date.valueOf(building.getYearOfConstruction()));
		} else {
			psBuilding.setNull(10, Types.DATE);
		}

		// bldg:yearOfDemolition
		if (building.isSetYearOfDemolition()) {
			psBuilding.setDate(11, Date.valueOf(building.getYearOfDemolition()));
		} else {
			psBuilding.setNull(11, Types.DATE);
		}

		// bldg:roofType
		if (building.isSetRoofType() && building.getRoofType().isSetValue()) {
			psBuilding.setString(12, building.getRoofType().getValue());
			psBuilding.setString(13, building.getRoofType().getCodeSpace());
		} else {
			psBuilding.setNull(12, Types.VARCHAR);
			psBuilding.setNull(13, Types.VARCHAR);
		}

		// bldg:measuredHeight
		if (building.isSetMeasuredHeight() && building.getMeasuredHeight().isSetValue()) {
			psBuilding.setDouble(14, building.getMeasuredHeight().getValue());
			psBuilding.setString(15, building.getMeasuredHeight().getUom());
		} else {
			psBuilding.setNull(14, Types.DOUBLE);
			psBuilding.setNull(15, Types.VARCHAR);
		}

		// bldg:storeysAboveGround
		if (building.isSetStoreysAboveGround()) {
			psBuilding.setInt(16, building.getStoreysAboveGround());
		} else {
			psBuilding.setNull(16, Types.NULL);
		}

		// bldg:storeysBelowGround
		if (building.isSetStoreysBelowGround()) {
			psBuilding.setInt(17, building.getStoreysBelowGround());
		} else {
			psBuilding.setNull(17, Types.NULL);
		}

		// bldg:storeyHeightsAboveGround
		if (building.isSetStoreyHeightsAboveGround()) {
			valueJoiner.join(" ", building.getStoreyHeightsAboveGround().getDoubleOrNull(),
					v -> v.isSetDouble() ? v.getDouble().toString() : v.getNull().getValue());
			
			psBuilding.setString(18, valueJoiner.result(0));
			psBuilding.setString(19, building.getStoreyHeightsAboveGround().getUom());
		} else {
			psBuilding.setNull(18, Types.VARCHAR);
			psBuilding.setNull(19, Types.VARCHAR);
		}

		// bldg:storeyHeightsBelowGround
		if (building.isSetStoreyHeightsBelowGround()) {
			valueJoiner.join(" ", building.getStoreyHeightsBelowGround().getDoubleOrNull(), 
					v -> v.isSetDouble() ? v.getDouble().toString() : v.getNull().getValue());

			psBuilding.setString(20, valueJoiner.result(0));
			psBuilding.setString(21, building.getStoreyHeightsBelowGround().getUom());
		} else {
			psBuilding.setNull(20, Types.VARCHAR);
			psBuilding.setNull(21, Types.VARCHAR);
		}

		// bldg:lodXTerrainIntersectionCurve
		for (int i = 0; i < 4; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = building.getLod1TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = building.getLod2TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = building.getLod3TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = building.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBuilding.setObject(22 + i, multiLineObj);
			} else
				psBuilding.setNull(22 + i, nullGeometryType, nullGeometryTypeName);
		}

		// bldg:lodXMultiCurve
		for (int i = 0; i < 3; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = building.getLod2MultiCurve();
				break;
			case 1:
				multiCurveProperty = building.getLod3MultiCurve();
				break;
			case 2:
				multiCurveProperty = building.getLod4MultiCurve();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBuilding.setObject(26 + i, multiLineObj);
			} else
				psBuilding.setNull(26 + i, nullGeometryType, nullGeometryTypeName);
		}

		// bldg:lod0FootPrint and bldg:lod0RoofEdge
		for (int i = 0; i < 2; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = building.getLod0FootPrint();
				break;
			case 1:
				multiSurfaceProperty = building.getLod0RoofEdge();
				break;			
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), buildingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BUILDING.getName(),
								buildingId, 
								href, 
								i == 0 ? "lod0_footprint_id" : "lod0_roofprint_id"));
					}
				}
			}

			if (multiSurfaceId != 0)
				psBuilding.setLong(29 + i, multiSurfaceId);
			else
				psBuilding.setNull(29 + i, Types.NULL);
		}

		// bldg:lodXMultiSurface
		for (int i = 0; i < 4; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = building.getLod1MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = building.getLod2MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = building.getLod3MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = building.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.doImport(multiSurfaceProperty.getMultiSurface(), buildingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					String href = multiSurfaceProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BUILDING.getName(),
								buildingId, 
								href, 
								"lod" + (i + 1) + "_multi_surface_id"));
					}
				}
			}

			if (multiGeometryId != 0)
				psBuilding.setLong(31 + i, multiGeometryId);
			else
				psBuilding.setNull(31 + i, Types.NULL);
		}

		// bldg:lodXSolid
		for (int i = 0; i < 4; i++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (i) {
			case 0:
				solidProperty = building.getLod1Solid();
				break;
			case 1:
				solidProperty = building.getLod2Solid();
				break;
			case 2:
				solidProperty = building.getLod3Solid();
				break;
			case 3:
				solidProperty = building.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), buildingId);
					solidProperty.unsetSolid();
				} else {
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								"building",
								buildingId, 
								href, 
								"lod" + (i + 1) + "_solid_id"));
					}
				}
			}

			if (solidGeometryId != 0)
				psBuilding.setLong(35 + i, solidGeometryId);
			else
				psBuilding.setNull(35 + i, Types.NULL);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psBuilding.setLong(39, featureType.getObjectClassId());

		psBuilding.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BUILDING);

		// bldg:boundedBy
		if (building.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : building.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, building, buildingId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.THEMATIC_SURFACE.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}

		// bldg:outerBuildingInstallation
		if (building.isSetOuterBuildingInstallation()) {
			for (BuildingInstallationProperty property : building.getOuterBuildingInstallation()) {
				BuildingInstallation installation = property.getBuildingInstallation();

				if (installation != null) {
					buildingInstallationImporter.doImport(installation, building, buildingId);
					property.unsetBuildingInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BUILDING_INSTALLATION.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}

		// bldg:interiorBuildingInstallation
		if (building.isSetInteriorBuildingInstallation()) {
			for (IntBuildingInstallationProperty property : building.getInteriorBuildingInstallation()) {
				IntBuildingInstallation installation = property.getIntBuildingInstallation();

				if (installation != null) {
					buildingInstallationImporter.doImport(installation, building, buildingId);
					property.unsetIntBuildingInstallation();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BUILDING_INSTALLATION.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}

		// bldg:interiorRoom
		if (building.isSetInteriorRoom()) {
			for (InteriorRoomProperty property : building.getInteriorRoom()) {
				Room room = property.getRoom();

				if (room != null) {
					roomImporter.doImport(room, buildingId);
					property.unsetRoom();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.ROOM.getName(),
								href,
								buildingId,
								"building_id"));
					}
				}
			}
		}

		// bldg:consistsOfBuildingPart
		if (building.isSetConsistsOfBuildingPart()) {
			for (BuildingPartProperty property : building.getConsistsOfBuildingPart()) {
				BuildingPart buildingPart = property.getBuildingPart();

				if (buildingPart != null) {
					doImport(buildingPart, buildingId, rootId);
					property.unsetBuildingPart();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0)
						importer.logOrThrowUnsupportedXLinkMessage(building, BuildingPart.class, href);
				}
			}
		}

		// bldg:address
		if (building.isSetAddress()) {
			for (AddressProperty property : building.getAddress()) {
				Address address = property.getAddress();

				if (address != null) {
					addressImporter.importBuildingAddress(address, buildingId);
					property.unsetAddress();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.ADDRESS_TO_BUILDING.getName(),
								buildingId,
								"BUILDING_ID",
								href,
								"ADDRESS_ID"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(building, buildingId, featureType);

		return buildingId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBuilding.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBuilding.close();
	}

}
