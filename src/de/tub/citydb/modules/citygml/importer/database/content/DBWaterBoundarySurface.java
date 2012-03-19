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

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBWaterBoundarySurface implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psWaterBoundarySurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBodToWaterBndSrf bodyToSurfaceImporter;
	
	private int batchCounter;
	
	public DBWaterBoundarySurface(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psWaterBoundarySurface = batchConn.prepareStatement("insert into WATERBOUNDARY_SURFACE (ID, NAME, NAME_CODESPACE, DESCRIPTION, TYPE, WATER_LEVEL, " +
				"LOD2_SURFACE_ID, LOD3_SURFACE_ID, LOD4_SURFACE_ID) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		bodyToSurfaceImporter = (DBWaterBodToWaterBndSrf)dbImporterManager.getDBImporter(DBImporterEnum.WATERBOD_TO_WATERBND_SRF);
	}

	public long insert(AbstractWaterBoundarySurface waterBoundarySurface, long parentId) throws SQLException {
		long waterBoundarySurfaceId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
    	if (waterBoundarySurfaceId == 0)
    		return 0;

		// CityObject
    	cityObjectImporter.insert(waterBoundarySurface, waterBoundarySurfaceId);

		// BoundarySurface
        // ID
    	psWaterBoundarySurface.setLong(1, waterBoundarySurfaceId);

		// gml:name
    	if (waterBoundarySurface.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(waterBoundarySurface);

			psWaterBoundarySurface.setString(2, dbGmlName[0]);
			psWaterBoundarySurface.setString(3, dbGmlName[1]);
		} else {
			psWaterBoundarySurface.setNull(2, Types.VARCHAR);
			psWaterBoundarySurface.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (waterBoundarySurface.isSetDescription()) {
			String description = waterBoundarySurface.getDescription().getValue();
			psWaterBoundarySurface.setString(4, description);
		} else {
			psWaterBoundarySurface.setNull(4, Types.VARCHAR);
		}

		// TYPE
		psWaterBoundarySurface.setString(5, TypeAttributeValueEnum.fromCityGMLClass(waterBoundarySurface.getCityGMLClass()).toString());

		// waterLevel
		if (waterBoundarySurface.getCityGMLClass() == CityGMLClass.WATER_SURFACE)
			psWaterBoundarySurface.setString(6, ((WaterSurface)waterBoundarySurface).getWaterLevel());
		else
			psWaterBoundarySurface.setNull(6, 0);

		// Geometry
        for (int lod = 2; lod < 5; lod++) {
        	SurfaceProperty surfaceProperty = null;
        	long abstractSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			surfaceProperty = waterBoundarySurface.getLod2Surface();
    			break;
    		case 3:
    			surfaceProperty = waterBoundarySurface.getLod3Surface();
    			break;
    		case 4:
    			surfaceProperty = waterBoundarySurface.getLod4Surface();
    			break;
    		}

    		if (surfaceProperty != null) {
    			if (surfaceProperty.isSetSurface()) {
    				abstractSurfaceId = surfaceGeometryImporter.insert(surfaceProperty.getSurface(), waterBoundarySurfaceId);
    			} else {
    				// xlink
					String href = surfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						waterBoundarySurfaceId,
        						TableEnum.WATERBOUNDARY_SURFACE,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
        	case 2:
        		if (abstractSurfaceId != 0)
        			psWaterBoundarySurface.setLong(7, abstractSurfaceId);
        		else
        			psWaterBoundarySurface.setNull(7, 0);
        		break;
        	case 3:
        		if (abstractSurfaceId != 0)
        			psWaterBoundarySurface.setLong(8, abstractSurfaceId);
        		else
        			psWaterBoundarySurface.setNull(8, 0);
        		break;
        	case 4:
        		if (abstractSurfaceId != 0)
        			psWaterBoundarySurface.setLong(9, abstractSurfaceId);
        		else
        			psWaterBoundarySurface.setNull(9, 0);
        		break;
        	}
        }

        psWaterBoundarySurface.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.WATERBOUNDARY_SURFACE);

        // boundary surface to waterBody
        bodyToSurfaceImporter.insert(waterBoundarySurfaceId, parentId);

        return waterBoundarySurfaceId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psWaterBoundarySurface.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psWaterBoundarySurface.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.WATERBOUNDARY_SURFACE;
	}

}
