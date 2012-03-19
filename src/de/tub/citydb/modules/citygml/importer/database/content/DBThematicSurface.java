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
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBThematicSurface implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psThematicSurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOpening openingImporter;

	private int batchCounter;
	
	public DBThematicSurface(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psThematicSurface = batchConn.prepareStatement("insert into THEMATIC_SURFACE (ID, NAME, NAME_CODESPACE, DESCRIPTION, TYPE, BUILDING_ID, ROOM_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		openingImporter = (DBOpening)dbImporterManager.getDBImporter(DBImporterEnum.OPENING);
	}

	public long insert(AbstractBoundarySurface boundarySurface, CityGMLClass parent, long parentId) throws SQLException {
		long boundarySurfaceId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
    	if (boundarySurfaceId == 0)
    		return 0;

    	String origGmlId = boundarySurface.getId();
    	
		// CityObject
    	cityObjectImporter.insert(boundarySurface, boundarySurfaceId);

		// BoundarySurface
        // ID
		psThematicSurface.setLong(1, boundarySurfaceId);

		// gml:name
		if (boundarySurface.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(boundarySurface);

			psThematicSurface.setString(2, dbGmlName[0]);
			psThematicSurface.setString(3, dbGmlName[1]);
		} else {
			psThematicSurface.setNull(2, Types.VARCHAR);
			psThematicSurface.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (boundarySurface.isSetDescription()) {
			String description = boundarySurface.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psThematicSurface.setString(4, description);
		} else {
			psThematicSurface.setNull(4, Types.VARCHAR);
		}

		// TYPE
        psThematicSurface.setString(5, TypeAttributeValueEnum.fromCityGMLClass(boundarySurface.getCityGMLClass()).toString());

        // parentId
		switch (parent) {
		case BUILDING:
		case BUILDING_PART:
			psThematicSurface.setLong(6, parentId);
			psThematicSurface.setNull(7, 0);
			break;
		case ROOM:
			psThematicSurface.setNull(6, 0);
			psThematicSurface.setLong(7, parentId);
			break;
		default:
			psThematicSurface.setNull(6, 0);
			psThematicSurface.setNull(7, 0);
		}

        // Geometry
        for (int lod = 2; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			multiSurfaceProperty = boundarySurface.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = boundarySurface.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = boundarySurface.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), boundarySurfaceId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						boundarySurfaceId,
        						TableEnum.THEMATIC_SURFACE,
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
        			psThematicSurface.setLong(8, multiSurfaceId);
        		else
        			psThematicSurface.setNull(8, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psThematicSurface.setLong(9, multiSurfaceId);
        		else
        			psThematicSurface.setNull(9, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psThematicSurface.setLong(10, multiSurfaceId);
        		else
        			psThematicSurface.setNull(10, 0);
        		break;
        	}
        }

        psThematicSurface.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.THEMATIC_SURFACE);

        // Openings
        if (boundarySurface.isSetOpening()) {
        	for (OpeningProperty openingProperty : boundarySurface.getOpening()) {
        		if (openingProperty.isSetOpening()) {
        			AbstractOpening opening = openingProperty.getOpening();
        			String gmlId = opening.getId();
        			long id = openingImporter.insert(opening, boundarySurfaceId);
        			
        			if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								opening.getCityGMLClass(), 
								gmlId));
						
						LOG.error(msg.toString());
					}
        			
        			// free memory of nested feature
        			openingProperty.unsetOpening();
        		} else {
        			// xlink
        			String href = openingProperty.getHref();

        			if (href != null && href.length() != 0) {
        				dbImporterManager.propagateXlink(new DBXlinkBasic(
        						boundarySurfaceId,
        						TableEnum.THEMATIC_SURFACE,
        						href,
        						TableEnum.OPENING
        				));
        			}
        		}
        	}
        }

    	return boundarySurfaceId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psThematicSurface.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psThematicSurface.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.THEMATIC_SURFACE;
	}

}
