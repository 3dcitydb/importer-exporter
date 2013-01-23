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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBTrafficArea implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTrafficArea;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private int batchCounter;
	
	public DBTrafficArea(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psTrafficArea = batchConn.prepareStatement("insert into TRAFFIC_AREA (ID, IS_AUXILIARY, NAME, NAME_CODESPACE, DESCRIPTION, FUNCTION, USAGE, " +
				"SURFACE_MATERIAL, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, " +
				"TRANSPORTATION_COMPLEX_ID) values "+
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(TrafficArea trafficArea, long parentId) throws SQLException {
		long trafficAreaId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (trafficAreaId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(trafficArea, trafficAreaId);

		// TrafficArea
		// ID
		psTrafficArea.setLong(1, trafficAreaId);

		// isAuxiliary
		psTrafficArea.setLong(2, 0);

		// gml:name
		if (trafficArea.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(trafficArea);

			psTrafficArea.setString(3, dbGmlName[0]);
			psTrafficArea.setString(4, dbGmlName[1]);
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (trafficArea.isSetDescription()) {
			String description = trafficArea.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psTrafficArea.setString(5, description);
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
		}

		// citygml:function
		if (trafficArea.isSetFunction()) {
			psTrafficArea.setString(6, Util.collection2string(trafficArea.getFunction(), " "));
		} else {
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (trafficArea.isSetUsage()) {
			psTrafficArea.setString(7, Util.collection2string(trafficArea.getUsage(), " "));
		} else {
			psTrafficArea.setNull(7, Types.VARCHAR);
		}

		// surface material
		if (trafficArea.isSetSurfaceMaterial())
			psTrafficArea.setString(8, trafficArea.getSurfaceMaterial());
		else
			psTrafficArea.setNull(8, Types.VARCHAR);

		// Geometry
        for (int lod = 2; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			multiSurfaceProperty = trafficArea.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = trafficArea.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = trafficArea.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), trafficAreaId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						trafficAreaId,
        						TableEnum.TRAFFIC_AREA,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
    		case 2:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(9, multiSurfaceId);
        		else
        			psTrafficArea.setNull(9, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(10, multiSurfaceId);
        		else
        			psTrafficArea.setNull(10, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(11, multiSurfaceId);
        		else
        			psTrafficArea.setNull(11, 0);
        		break;
        	}
        }

        // reference to transportation complex
        psTrafficArea.setLong(12, parentId);

        psTrafficArea.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.TRAFFIC_AREA);
        
		return trafficAreaId;
	}

	public long insert(AuxiliaryTrafficArea auxiliaryTrafficArea, long parentId) throws SQLException {
		long auxiliaryTrafficAreaId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (auxiliaryTrafficAreaId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(auxiliaryTrafficArea, auxiliaryTrafficAreaId);

		// TrafficArea
		// ID
		psTrafficArea.setLong(1, auxiliaryTrafficAreaId);

		// isAuxiliary
		psTrafficArea.setLong(2, 1);

		// gml:name
		if (auxiliaryTrafficArea.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(auxiliaryTrafficArea);

			psTrafficArea.setString(3, dbGmlName[0]);
			psTrafficArea.setString(4, dbGmlName[1]);
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (auxiliaryTrafficArea.isSetDescription()) {
			String description = auxiliaryTrafficArea.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psTrafficArea.setString(5, description);
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
		}

		// citygml:function
		if (auxiliaryTrafficArea.isSetFunction()) {
			psTrafficArea.setString(6, Util.collection2string(auxiliaryTrafficArea.getFunction(), " "));
		} else {
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		psTrafficArea.setNull(7, Types.VARCHAR);

		// surface material
		if (auxiliaryTrafficArea.isSetSurfaceMaterial())
			psTrafficArea.setString(8, auxiliaryTrafficArea.getSurfaceMaterial());
		else
			psTrafficArea.setNull(8, Types.VARCHAR);

		// Geometry
        for (int lod = 2; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			multiSurfaceProperty = auxiliaryTrafficArea.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = auxiliaryTrafficArea.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = auxiliaryTrafficArea.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), auxiliaryTrafficAreaId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						auxiliaryTrafficAreaId,
        						TableEnum.TRAFFIC_AREA,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
    		case 2:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(9, multiSurfaceId);
        		else
        			psTrafficArea.setNull(9, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(10, multiSurfaceId);
        		else
        			psTrafficArea.setNull(10, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(11, multiSurfaceId);
        		else
        			psTrafficArea.setNull(11, 0);
        		break;
        	}
        }

        // reference to transportation complex
        psTrafficArea.setLong(12, parentId);

        psTrafficArea.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.TRAFFIC_AREA);
        
		return auxiliaryTrafficAreaId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTrafficArea.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTrafficArea.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TRAFFIC_AREA;
	}

}
