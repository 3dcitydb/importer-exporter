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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
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
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBBuilding implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBuilding;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBBuildingInstallation buildingInstallationImporter;
	private DBRoom roomImporter;
	private DBAddress addressImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;	

	public DBBuilding(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into BUILDING (ID, BUILDING_PARENT_ID, BUILDING_ROOT_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, YEAR_OF_CONSTRUCTION, YEAR_OF_DEMOLITION, ")
		.append("ROOF_TYPE, ROOF_TYPE_CODESPACE, MEASURED_HEIGHT, MEASURED_HEIGHT_UNIT, STOREYS_ABOVE_GROUND, STOREYS_BELOW_GROUND, STOREY_HEIGHTS_ABOVE_GROUND, STOREY_HEIGHTS_AG_UNIT, STOREY_HEIGHTS_BELOW_GROUND, STOREY_HEIGHTS_BG_UNIT, ")
		.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, LOD2_MULTI_CURVE, LOD3_MULTI_CURVE, LOD4_MULTI_CURVE, ")
		.append("LOD0_FOOTPRINT_ID, LOD0_ROOFPRINT_ID, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psBuilding = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.THEMATIC_SURFACE);
		buildingInstallationImporter = (DBBuildingInstallation)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING_INSTALLATION);
		roomImporter = (DBRoom)dbImporterManager.getDBImporter(DBImporterEnum.ROOM);
		addressImporter = (DBAddress)dbImporterManager.getDBImporter(DBImporterEnum.ADDRESS);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(AbstractBuilding building) throws SQLException {
		long buildingId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		boolean success = false;

		if (buildingId != 0)
			success = insert(building, buildingId, 0, buildingId);

		if (success)
			return buildingId;
		else
			return 0;
	}

	public boolean insert(AbstractBuilding building,
			long buildingId,
			long parentId,
			long rootId) throws SQLException {
		String origGmlId = building.getId();

		// CityObject
		long cityObjectId = cityObjectImporter.insert(building, buildingId, parentId == 0);
		if (cityObjectId == 0)
			return false;

		// Building
		// ID
		psBuilding.setLong(1, buildingId);

		// BUILDING_PARENT_ID
		if (parentId != 0)
			psBuilding.setLong(2, parentId);
		else
			psBuilding.setNull(2, Types.NULL);

		// BUILDING_ROOT_ID
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
			String[] function = Util.codeList2string(building.getFunction());
			psBuilding.setString(6, function[0]);
			psBuilding.setString(7, function[1]);
		} else {
			psBuilding.setNull(6, Types.VARCHAR);
			psBuilding.setNull(7, Types.VARCHAR);
		}

		// bldg:usage
		if (building.isSetUsage()) {
			String[] usage = Util.codeList2string(building.getUsage());
			psBuilding.setString(8, usage[0]);
			psBuilding.setString(9, usage[1]);
		} else {
			psBuilding.setNull(8, Types.VARCHAR);
			psBuilding.setNull(9, Types.VARCHAR);
		}

		// bldg:yearOfConstruction
		if (building.isSetYearOfConstruction()) {
			psBuilding.setDate(10, new Date(building.getYearOfConstruction().getTime().getTime()));
		} else {
			psBuilding.setNull(10, Types.DATE);
		}

		// bldg:yearOfDemolition
		if (building.isSetYearOfDemolition()) {
			psBuilding.setDate(11, new Date(building.getYearOfDemolition().getTime().getTime()));
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
			psBuilding.setNull(16, Types.INTEGER);
		}

		// bldg:storeysBelowGround
		if (building.isSetStoreysBelowGround()) {
			psBuilding.setInt(17, building.getStoreysBelowGround());
		} else {
			psBuilding.setNull(17, Types.INTEGER);
		}

		// bldg:storeyHeightsAboveGround
		String heights = null;
		if (building.isSetStoreyHeightsAboveGround()) {
			MeasureOrNullList measureOrNullList = building.getStoreyHeightsAboveGround();
			if (measureOrNullList.isSetDoubleOrNull()) {
				List<String> values = new ArrayList<String>();				
				for (DoubleOrNull doubleOrNull : measureOrNullList.getDoubleOrNull()) {
					if (doubleOrNull.isSetDouble())
						values.add(String.valueOf(doubleOrNull.getDouble()));
					else
						doubleOrNull.getNull().getValue();			
				}

				heights = Util.collection2string(values, " ");
			} 
		}

		if (heights != null) {
			psBuilding.setString(18, heights);
			psBuilding.setString(19, building.getStoreyHeightsAboveGround().getUom());
		} else {
			psBuilding.setNull(18, Types.VARCHAR);
			psBuilding.setNull(19, Types.VARCHAR);
		}

		// bldg:storeyHeightsBelowGround
		heights = null;
		if (building.isSetStoreyHeightsBelowGround()) {
			MeasureOrNullList measureOrNullList = building.getStoreyHeightsBelowGround();
			if (measureOrNullList.isSetDoubleOrNull()) {
				List<String> values = new ArrayList<String>();				
				for (DoubleOrNull doubleOrNull : measureOrNullList.getDoubleOrNull()) {
					if (doubleOrNull.isSetDouble())
						values.add(String.valueOf(doubleOrNull.getDouble()));
					else
						doubleOrNull.getNull().getValue();			
				}

				heights = Util.collection2string(values, " ");
			} 
		}

		if (heights != null) {
			psBuilding.setString(20, heights);
			psBuilding.setString(21, building.getStoreyHeightsBelowGround().getUom());
		} else {
			psBuilding.setNull(20, Types.VARCHAR);
			psBuilding.setNull(21, Types.VARCHAR);
		}

		// Geometry
		// lodXTerrainIntersectionCurve
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
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBuilding.setObject(22 + i, multiLineObj);
			} else
				psBuilding.setNull(22 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXMultiCurve
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
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBuilding.setObject(26 + i, multiLineObj);
			} else
				psBuilding.setNull(26 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lod0FootPrint and lod0RoofEdge
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
					multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), buildingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								buildingId, 
								TableEnum.BUILDING, 
								i == 0 ? "LOD0_FOOTPRINT_ID" : "LOD0_ROOFPRINT_ID"));
					}
				}
			}

			if (multiSurfaceId != 0)
				psBuilding.setLong(29 + i, multiSurfaceId);
			else
				psBuilding.setNull(29 + i, Types.NULL);
		}

		// lodXMultiSurface
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
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), buildingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								buildingId, 
								TableEnum.BUILDING, 
								"LOD" + (i + 1) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psBuilding.setLong(31 + i, multiGeometryId);
			else
				psBuilding.setNull(31 + i, Types.NULL);
		}

		// lodXSolid
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
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), buildingId);
					solidProperty.unsetSolid();
				} else {
					// xlink
					String href = solidProperty.getHref();
					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								buildingId, 
								TableEnum.BUILDING, 
								"LOD" + (i + 1) + "_SOLID_ID"));
					}
				}
			}

			if (solidGeometryId != 0)
				psBuilding.setLong(35 + i, solidGeometryId);
			else
				psBuilding.setNull(35 + i, Types.NULL);
		}

		psBuilding.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING);

		// BoundarySurfaces
		if (building.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : building.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, building.getCityGMLClass(), buildingId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								building.getCityGMLClass(), 
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
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BUILDING_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// BuildingInstallation
		if (building.isSetOuterBuildingInstallation()) {
			for (BuildingInstallationProperty buildingInstProperty : building.getOuterBuildingInstallation()) {
				BuildingInstallation buildingInst = buildingInstProperty.getBuildingInstallation();

				if (buildingInst != null) {
					String gmlId = buildingInst.getId();
					long id = buildingInstallationImporter.insert(buildingInst, building.getCityGMLClass(), buildingId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								building.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.BUILDING_INSTALLATION, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					buildingInstProperty.unsetBuildingInstallation();
				} else {
					// xlink
					String href = buildingInstProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BUILDING_INSTALLATION + " feature is not supported.");
					}
				}
			}
		}

		// IntBuildingInstallation
		if (building.isSetInteriorBuildingInstallation()) {
			for (IntBuildingInstallationProperty intBuildingInstProperty : building.getInteriorBuildingInstallation()) {
				IntBuildingInstallation intBuildingInst = intBuildingInstProperty.getIntBuildingInstallation();

				if (intBuildingInst != null) {
					String gmlId = intBuildingInst.getId();
					long id = buildingInstallationImporter.insert(intBuildingInst, building.getCityGMLClass(), buildingId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								building.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.INT_BUILDING_INSTALLATION, 
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

		// Room
		if (building.isSetInteriorRoom()) {
			for (InteriorRoomProperty roomProperty : building.getInteriorRoom()) {
				Room room = roomProperty.getRoom();

				if (room != null) {
					String gmlId = room.getId();
					long id = roomImporter.insert(room, buildingId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								building.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.BUILDING_ROOM, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					roomProperty.unsetRoom();
				} else {
					// xlink
					String href = roomProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BUILDING_ROOM + " feature is not supported.");
					}
				}
			}
		}

		// BuildingPart
		if (building.isSetConsistsOfBuildingPart()) {
			for (BuildingPartProperty buildingPartProperty : building.getConsistsOfBuildingPart()) {
				BuildingPart buildingPart = buildingPartProperty.getBuildingPart();

				if (buildingPart != null) {
					long id = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);

					if (id != 0)
						insert(buildingPart, id, buildingId, rootId);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								building.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.BUILDING_PART, 
								buildingPart.getId()));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					buildingPartProperty.unsetBuildingPart();
				} else {
					// xlink
					String href = buildingPartProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.BUILDING_PART + " feature is not supported.");
					}
				}
			}
		}

		// Address
		if (building.isSetAddress()) {
			for (AddressProperty addressProperty : building.getAddress()) {
				Address address = addressProperty.getAddress();

				if (address != null) {
					String gmlId = address.getId();
					long id = addressImporter.insertBuildingAddress(address, buildingId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								building.getCityGMLClass(), 
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
								buildingId,
								TableEnum.BUILDING,
								href,
								TableEnum.ADDRESS
								));
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(building, buildingId);

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBuilding.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBuilding.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BUILDING;
	}

}
