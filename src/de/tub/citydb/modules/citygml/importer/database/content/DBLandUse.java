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

import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBLandUse implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psLandUse;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	
	private int batchCounter;

	public DBLandUse(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psLandUse = batchConn.prepareStatement("insert into LAND_USE (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
				"LOD0_MULTI_SURFACE_ID, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(LandUse landUse) throws SQLException {
		long landUseId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (landUseId != 0)
			success = insert(landUse, landUseId);

		if (success)
			return landUseId;
		else
			return 0;
	}

	private boolean insert(LandUse landUse, long landUseId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(landUse, landUseId, true);
		if (cityObjectId == 0)
			return false;

		// LandUse
		// ID
		psLandUse.setLong(1, landUseId);

		// gml:name
		if (landUse.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(landUse);

			psLandUse.setString(2, dbGmlName[0]);
			psLandUse.setString(3, dbGmlName[1]);
		} else {
			psLandUse.setNull(2, Types.VARCHAR);
			psLandUse.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (landUse.isSetDescription()) {
			String description = landUse.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psLandUse.setString(4, description);
		} else {
			psLandUse.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (landUse.isSetClazz())
			psLandUse.setString(5, landUse.getClazz().trim());
		else
			psLandUse.setNull(5, Types.VARCHAR);

		// citygml:function
		if (landUse.isSetFunction()) {
			psLandUse.setString(6, Util.collection2string(landUse.getFunction(), " "));
		} else {
			psLandUse.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (landUse.isSetUsage()) {
			psLandUse.setString(7, Util.collection2string(landUse.getUsage(), " "));
		} else {
			psLandUse.setNull(7, Types.VARCHAR);
		}

		// Geometry
        for (int lod = 0; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 0:
    			multiSurfaceProperty = landUse.getLod0MultiSurface();
    			break;
    		case 1:
    			multiSurfaceProperty = landUse.getLod1MultiSurface();
    			break;
    		case 2:
    			multiSurfaceProperty = landUse.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = landUse.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = landUse.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), landUseId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						landUseId,
        						TableEnum.LAND_USE,
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
        		if (multiSurfaceId != 0)
        			psLandUse.setLong(8, multiSurfaceId);
        		else
        			psLandUse.setNull(8, 0);
        		break;
    		case 1:
        		if (multiSurfaceId != 0)
        			psLandUse.setLong(9, multiSurfaceId);
        		else
        			psLandUse.setNull(9, 0);
        		break;
    		case 2:
        		if (multiSurfaceId != 0)
        			psLandUse.setLong(10, multiSurfaceId);
        		else
        			psLandUse.setNull(10, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psLandUse.setLong(11, multiSurfaceId);
        		else
        			psLandUse.setNull(11, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psLandUse.setLong(12, multiSurfaceId);
        		else
        			psLandUse.setNull(12, 0);
        		break;
        	}
        }

        psLandUse.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.LAND_USE);
        
		return true;
	}


	@Override
	public void executeBatch() throws SQLException {
		psLandUse.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psLandUse.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.LAND_USE;
	}

}
