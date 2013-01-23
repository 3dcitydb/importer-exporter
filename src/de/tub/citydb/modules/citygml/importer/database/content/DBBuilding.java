/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

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

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

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
	private DBSdoGeometry sdoGeometry;

	private int batchCounter;

	public DBBuilding(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
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
			psBuilding.setLong(16, parentId);
		else
			psBuilding.setNull(16, 0);

		// BUILDING_ROOT_ID
		psBuilding.setLong(17, rootId);

		// gml:name
		if (building.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(building);

			psBuilding.setString(2, dbGmlName[0]);
			psBuilding.setString(3, dbGmlName[1]);
		} else {
			psBuilding.setNull(2, Types.VARCHAR);
			psBuilding.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (building.isSetDescription()) {
			String description = building.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuilding.setString(4, description);
		} else {
			psBuilding.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (building.isSetClazz()) {
			psBuilding.setString(5, building.getClazz().trim());
		} else {
			psBuilding.setNull(5, Types.VARCHAR);
		}

		// citygml:function
		if (building.isSetFunction()) {
			psBuilding.setString(6, Util.collection2string(building.getFunction(), " "));
		} else {
			psBuilding.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (building.isSetUsage()) {
			psBuilding.setString(7, Util.collection2string(building.getUsage(), " "));
		} else {
			psBuilding.setNull(7, Types.VARCHAR);
		}

		// citygml:yearOfConstruction
		if (building.isSetYearOfConstruction()) {
			psBuilding.setDate(8, new Date(building.getYearOfConstruction().getTime().getTime()));
		} else {
			psBuilding.setNull(8, Types.DATE);
		}

		// citygml:yearOfDemolition
		if (building.isSetYearOfDemolition()) {
			psBuilding.setDate(9, new Date(building.getYearOfDemolition().getTime().getTime()));
		} else {
			psBuilding.setNull(9, Types.DATE);
		}

		// citygml:roofType
		if (building.isSetRoofType()) {
			psBuilding.setString(10, building.getRoofType());
		} else {
			psBuilding.setNull(10, Types.VARCHAR);
		}

		// citygml:measuredHeight
		if (building.isSetMeasuredHeight() && building.getMeasuredHeight().isSetValue()) {
			psBuilding.setDouble(11, building.getMeasuredHeight().getValue());
		} else {
			psBuilding.setNull(11, Types.DOUBLE);
		}

		// citygml:storeysAboveGround
		if (building.isSetStoreysAboveGround()) {
			psBuilding.setInt(12, building.getStoreysAboveGround());
		} else {
			psBuilding.setNull(12, Types.INTEGER);
		}

		// citygml:storeysBelowGround
		if (building.isSetStoreysBelowGround()) {
			psBuilding.setInt(13, building.getStoreysBelowGround());
		} else {
			psBuilding.setNull(13, Types.INTEGER);
		}

		// citygml:storeyHeightsAboveGround
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
				
				psBuilding.setString(14, Util.collection2string(values, " "));
			} else
				psBuilding.setNull(14, Types.VARCHAR);
		} else {
			psBuilding.setNull(14, Types.VARCHAR);
		}

		// citygml:storeyHeightsBelowGround
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
				
				psBuilding.setString(15, Util.collection2string(values, " "));
			} else
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
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), buildingId);
				} else {
					// xlink
					String href = solidProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkBasic xlink = new DBXlinkBasic(
								buildingId,
								TableEnum.BUILDING,
								href,
								TableEnum.SURFACE_GEOMETRY
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
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), buildingId);
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkBasic xlink = new DBXlinkBasic(
								buildingId,
								TableEnum.BUILDING,
								href,
								TableEnum.SURFACE_GEOMETRY
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
				multiCurveProperty = building.getLod1TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = building.getLod2TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = building.getLod3TerrainIntersection();
				break;
			case 4:
				multiCurveProperty = building.getLod4TerrainIntersection();
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
						LOG.error("XLink reference '" + href + "' to BoundarySurface feature is not supported.");
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
						LOG.error("XLink reference '" + href + "' to BuildingInstallation feature is not supported.");
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
						LOG.error("XLink reference '" + href + "' to IntBuildingInstallation feature is not supported.");
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
								CityGMLClass.ROOM, 
								gmlId));
						
						LOG.error(msg.toString());
					}
					
					// free memory of nested feature
					roomProperty.unsetRoom();
				} else {
					// xlink
					String href = roomProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to Room feature is not supported.");
					}
				}
			}
		}

		// BuildingPart
		if (building.isSetConsistsOfBuildingPart()) {
			for (BuildingPartProperty buildingPartProperty : building.getConsistsOfBuildingPart()) {
				BuildingPart buildingPart = buildingPartProperty.getBuildingPart();
				
				if (buildingPart != null) {
					long id = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
					
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
						LOG.error("XLink reference '" + href + "' to BuildingPart feature is not supported.");
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
					long id = addressImporter.insert(address, buildingId);
					
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
