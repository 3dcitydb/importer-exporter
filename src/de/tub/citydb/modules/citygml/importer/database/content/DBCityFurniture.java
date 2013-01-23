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

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBCityFurniture implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBSdoGeometry sdoGeometry;
	
	private boolean affineTransformation;
	private int batchCounter;

	public DBCityFurniture(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		psCityFurniture = batchConn.prepareStatement("insert into CITY_FURNITURE (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, " +
				"LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
				"LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, " +
				"LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, " +
				"LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION, " +
				"LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(CityFurniture cityFurniture) throws SQLException {
		long cityFurnitureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (cityFurnitureId != 0)
			success = insert(cityFurniture, cityFurnitureId);

		if (success)
			return cityFurnitureId;
		else
			return 0;
	}

	private boolean insert(CityFurniture cityFurniture, long cityFurnitureId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(cityFurniture, cityFurnitureId, true);
		if (cityObjectId == 0)
			return false;

		// CityFurniture
		// ID
		psCityFurniture.setLong(1, cityObjectId);

		// gml:name
		if (cityFurniture.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(cityFurniture);

			psCityFurniture.setString(2, dbGmlName[0]);
			psCityFurniture.setString(3, dbGmlName[1]);
		} else {
			psCityFurniture.setNull(2, Types.VARCHAR);
			psCityFurniture.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (cityFurniture.isSetDescription()) {
			String description = cityFurniture.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psCityFurniture.setString(4, description);
		} else {
			psCityFurniture.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (cityFurniture.isSetClazz())
			psCityFurniture.setString(5, cityFurniture.getClazz().trim());
		else
			psCityFurniture.setNull(5, Types.VARCHAR);

		// citygml:function
		if (cityFurniture.isSetFunction()) {
			psCityFurniture.setString(6, Util.collection2string(cityFurniture.getFunction(), " "));
		} else {
			psCityFurniture.setNull(6, Types.VARCHAR);
		}

		// Geometry
		for (int lod = 1; lod < 5; lod++) {
        	GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
        	long geometryId = 0;

    		switch (lod) {
    		case 1:
    			geometryProperty = cityFurniture.getLod1Geometry();
    			break;
    		case 2:
    			geometryProperty = cityFurniture.getLod2Geometry();
    			break;
    		case 3:
    			geometryProperty = cityFurniture.getLod3Geometry();
    			break;
    		case 4:
    			geometryProperty = cityFurniture.getLod4Geometry();
    			break;
    		}

    		if (geometryProperty != null) {
    			if (geometryProperty.isSetGeometry()) {
    				geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), cityFurnitureId);
    			} else {
    				// xlink
					String href = geometryProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						cityFurnitureId,
        						TableEnum.CITY_FURNITURE,
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
        		if (geometryId != 0)
        			psCityFurniture.setLong(7, geometryId);
        		else
        			psCityFurniture.setNull(7, 0);
        		break;
    		case 2:
        		if (geometryId != 0)
        			psCityFurniture.setLong(8, geometryId);
        		else
        			psCityFurniture.setNull(8, 0);
        		break;
        	case 3:
        		if (geometryId != 0)
        			psCityFurniture.setLong(9, geometryId);
        		else
        			psCityFurniture.setNull(9, 0);
        		break;
        	case 4:
        		if (geometryId != 0)
        			psCityFurniture.setLong(10, geometryId);
        		else
        			psCityFurniture.setNull(10, 0);
        		break;
        	}
        }

		for (int lod = 1; lod < 5; lod++) {
			ImplicitRepresentationProperty implicit = null;
			JGeometry pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (lod) {
			case 1:
				implicit = cityFurniture.getLod1ImplicitRepresentation();
				break;
			case 2:
				implicit = cityFurniture.getLod2ImplicitRepresentation();
				break;
			case 3:
				implicit = cityFurniture.getLod3ImplicitRepresentation();
				break;
			case 4:
				implicit = cityFurniture.getLod4ImplicitRepresentation();
				break;
			}

			if (implicit != null) {
				if (implicit.isSetObject()) {
					ImplicitGeometry geometry = implicit.getObject();

					// reference Point
					if (geometry.isSetReferencePoint())
						pointGeom = sdoGeometry.getPoint(geometry.getReferencePoint());

					// transformation matrix
					if (geometry.isSetTransformationMatrix()) {
						Matrix matrix = geometry.getTransformationMatrix().getMatrix();
						if (affineTransformation)
							matrix = dbImporterManager.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);
						
						matrixString = Util.collection2string(matrix.toRowPackedList(), " ");
					}
					
					// reference to IMPLICIT_GEOMETRY
					implicitId = implicitGeometryImporter.insert(geometry, cityFurnitureId);
				}
			}

			switch (lod) {
			case 1:
				if (implicitId != 0)
					psCityFurniture.setLong(11, implicitId);
				else
					psCityFurniture.setNull(11, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psCityFurniture.setObject(15, obj);
				} else
					psCityFurniture.setNull(15, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psCityFurniture.setString(19, matrixString);
				else
					psCityFurniture.setNull(19, Types.VARCHAR);

				break;
			case 2:
				if (implicitId != 0)
					psCityFurniture.setLong(12, implicitId);
				else
					psCityFurniture.setNull(12, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psCityFurniture.setObject(16, obj);
				} else
					psCityFurniture.setNull(16, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psCityFurniture.setString(20, matrixString);
				else
					psCityFurniture.setNull(20, Types.VARCHAR);

				break;
			case 3:
				if (implicitId != 0)
					psCityFurniture.setLong(13, implicitId);
				else
					psCityFurniture.setNull(13, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psCityFurniture.setObject(17, obj);
				} else
					psCityFurniture.setNull(17, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psCityFurniture.setString(21, matrixString);
				else
					psCityFurniture.setNull(21, Types.VARCHAR);

				break;
			case 4:
				if (implicitId != 0)
					psCityFurniture.setLong(14, implicitId);
				else
					psCityFurniture.setNull(14, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psCityFurniture.setObject(18, obj);
				} else
					psCityFurniture.setNull(18, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psCityFurniture.setString(22, matrixString);
				else
					psCityFurniture.setNull(22, Types.VARCHAR);

				break;
			}
 		}

		// lodXTerrainIntersectionCurve
		for (int lod = 1; lod < 5; lod++) {
			
			MultiCurveProperty multiCurveProperty = null;
			JGeometry multiLine = null;
			
			switch (lod) {
			case 1:
				multiCurveProperty = cityFurniture.getLod1TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = cityFurniture.getLod2TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = cityFurniture.getLod3TerrainIntersection();
				break;
			case 4:
				multiCurveProperty = cityFurniture.getLod4TerrainIntersection();
				break;
			}
			
			if (multiCurveProperty != null)
				multiLine = sdoGeometry.getMultiCurve(multiCurveProperty);

			switch (lod) {
			case 1:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psCityFurniture.setObject(23, multiLineObj);
				} else
					psCityFurniture.setNull(23, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 2:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psCityFurniture.setObject(24, multiLineObj);
				} else
					psCityFurniture.setNull(24, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 3:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psCityFurniture.setObject(25, multiLineObj);
				} else
					psCityFurniture.setNull(25, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 4:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psCityFurniture.setObject(26, multiLineObj);
				} else
					psCityFurniture.setNull(26, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			}
		}
		
		psCityFurniture.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.CITY_FURNITURE);
		
		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psCityFurniture.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psCityFurniture.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITY_FURNITURE;
	}

}
