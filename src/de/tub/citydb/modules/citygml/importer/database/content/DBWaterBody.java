/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBWaterBody implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psWaterBody;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBoundarySurface boundarySurfaceImporter;
	private DBSdoGeometry sdoGeometry;
	
	private int batchCounter;

	public DBWaterBody(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psWaterBody = batchConn.prepareStatement("insert into WATERBODY (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
				"LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID, LOD4_SOLID_ID, " +
				"LOD0_MULTI_SURFACE_ID, LOD1_MULTI_SURFACE_ID, LOD0_MULTI_CURVE, LOD1_MULTI_CURVE) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		boundarySurfaceImporter = (DBWaterBoundarySurface)dbImporterManager.getDBImporter(DBImporterEnum.WATERBOUNDARY_SURFACE);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(WaterBody waterBody) throws SQLException {
		long waterBodyId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (waterBodyId != 0)
			success = insert(waterBody, waterBodyId);

		if (success)
			return waterBodyId;
		else
			return 0;
	}

	private boolean insert(WaterBody waterBody, long waterBodyId) throws SQLException {
		String origGmlId = waterBody.getId();
		
		// CityObject
		long cityObjectId = cityObjectImporter.insert(waterBody, waterBodyId, true);
		if (cityObjectId == 0)
			return false;

		// CityFurniture
		// ID
		psWaterBody.setLong(1, cityObjectId);

		// gml:name
		if (waterBody.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(waterBody);

			psWaterBody.setString(2, dbGmlName[0]);
			psWaterBody.setString(3, dbGmlName[1]);
		} else {
			psWaterBody.setNull(2, Types.VARCHAR);
			psWaterBody.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (waterBody.isSetDescription()) {
			String description = waterBody.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psWaterBody.setString(4, description);
		} else {
			psWaterBody.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (waterBody.isSetClazz())
			psWaterBody.setString(5, waterBody.getClazz().trim());
		else
			psWaterBody.setNull(5, Types.VARCHAR);

		// citygml:function
		if (waterBody.isSetFunction()) {
			psWaterBody.setString(6, Util.collection2string(waterBody.getFunction(), " "));
		} else {
			psWaterBody.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (waterBody.isSetUsage()) {
			psWaterBody.setString(7, Util.collection2string(waterBody.getUsage(), " "));
		} else {
			psWaterBody.setNull(7, Types.VARCHAR);
		}

		// Geometry
		// lodXSolid
		for (int lod = 1; lod < 5; lod++) {
			SolidProperty solidProperty = null;
			long solidGeometryId = 0;

			switch (lod) {
			case 1:
				solidProperty = waterBody.getLod1Solid();
				break;
			case 2:
				solidProperty = waterBody.getLod2Solid();
				break;
			case 3:
				solidProperty = waterBody.getLod3Solid();
				break;
			case 4:
				solidProperty = waterBody.getLod4Solid();
				break;
			}

			if (solidProperty != null) {
				if (solidProperty.isSetSolid()) {
					solidGeometryId = surfaceGeometryImporter.insert(solidProperty.getSolid(), waterBodyId);
				} else {
					// xlink
					String href = solidProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						waterBodyId,
        						TableEnum.WATERBODY,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_SOLID_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 1:
				if (solidGeometryId != 0)
					psWaterBody.setLong(8, solidGeometryId);
				else
					psWaterBody.setNull(8, 0);
				break;
			case 2:
				if (solidGeometryId != 0)
					psWaterBody.setLong(9, solidGeometryId);
				else
					psWaterBody.setNull(9, 0);
				break;
			case 3:
				if (solidGeometryId != 0)
					psWaterBody.setLong(10, solidGeometryId);
				else
					psWaterBody.setNull(10, 0);
				break;
			case 4:
				if (solidGeometryId != 0)
					psWaterBody.setLong(11, solidGeometryId);
				else
					psWaterBody.setNull(11, 0);
				break;
			}
		}

		// lodXMultiSurface
		for (int lod = 0; lod < 2; lod++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (lod) {
			case 0:
				multiSurfaceProperty = waterBody.getLod0MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = waterBody.getLod1MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), waterBodyId);
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						waterBodyId,
        						TableEnum.WATERBODY,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 0:
				if (multiGeometryId != 0)
					psWaterBody.setLong(12, multiGeometryId);
				else
					psWaterBody.setNull(12, 0);
				break;
			case 1:
				if (multiGeometryId != 0)
					psWaterBody.setLong(13, multiGeometryId);
				else
					psWaterBody.setNull(13, 0);
				break;
			}
		}

		// lodXMultiCurve
		for (int lod = 0; lod < 2; lod++) {
			
			MultiCurveProperty multiCurveProperty = null;
			JGeometry multiLine = null;
			
			switch (lod) {
			case 0:
				multiCurveProperty = waterBody.getLod0MultiCurve();
				break;
			case 1:
				multiCurveProperty = waterBody.getLod1MultiCurve();
				break;
			}
			
			if (multiCurveProperty != null)
				multiLine = sdoGeometry.getMultiCurve(multiCurveProperty);

			switch (lod) {
			case 0:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psWaterBody.setObject(14, multiLineObj);
				} else
					psWaterBody.setNull(14, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 1:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psWaterBody.setObject(15, multiLineObj);
				} else
					psWaterBody.setNull(15, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			}			
		}
		
		psWaterBody.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.WATERBODY);

		// boundary surfaces
		if (waterBody.isSetBoundedBySurface()) {
			for (BoundedByWaterSurfaceProperty waterSurfaceProperty : waterBody.getBoundedBySurface()) {
				AbstractWaterBoundarySurface boundarySurface = waterSurfaceProperty.getWaterBoundarySurface();
				
				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = boundarySurfaceImporter.insert(boundarySurface, waterBodyId);
					
					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								CityGMLClass.WATER_BODY, 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));
						
						LOG.error(msg.toString());
					}
					
					// free memory of nested feature
					waterSurfaceProperty.unsetWaterBoundarySurface();
				} else {
					// xlink
					String href = waterSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
        						waterBodyId,
        						TableEnum.WATERBODY,
        						href,
        						TableEnum.WATERBOUNDARY_SURFACE
        				));
					}
				}
			}
		}

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psWaterBody.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psWaterBody.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.WATERBODY;
	}
}
