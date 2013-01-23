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
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBGenericCityObject implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;
	
	private PreparedStatement psGenericCityObject;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBSdoGeometry sdoGeometry;
	private DBCityObjectGenericAttrib genericAttributeImporter;

	private boolean affineTransformation;
	private int batchCounter;

	public DBGenericCityObject(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}
	
	private void init() throws SQLException {
		psGenericCityObject = batchConn.prepareStatement("insert into GENERIC_CITYOBJECT (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
				"LOD0_GEOMETRY_ID, LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
				"LOD0_IMPLICIT_REP_ID, LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, " +
				"LOD0_IMPLICIT_REF_POINT, LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, " +
				"LOD0_IMPLICIT_TRANSFORMATION, LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION, " +
				"LOD0_TERRAIN_INTERSECTION, LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
		genericAttributeImporter = (DBCityObjectGenericAttrib)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
	}
	
	public long insert(GenericCityObject genericCityObject) throws SQLException {
		long genericCityObjectId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (genericCityObjectId != 0)
			success = insert(genericCityObject, genericCityObjectId);

		if (success)
			return genericCityObjectId;
		else
			return 0;
	}
	
	private boolean insert(GenericCityObject genericCityObject, long genericCityObjectId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(genericCityObject, genericCityObjectId, true);
		if (cityObjectId == 0)
			return false;

		// CityFurniture
		// ID
		psGenericCityObject.setLong(1, cityObjectId);

		// gml:name
		if (genericCityObject.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(genericCityObject);

			psGenericCityObject.setString(2, dbGmlName[0]);
			psGenericCityObject.setString(3, dbGmlName[1]);
		} else {
			psGenericCityObject.setNull(2, Types.VARCHAR);
			psGenericCityObject.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (genericCityObject.isSetDescription()) {
			String description = genericCityObject.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psGenericCityObject.setString(4, description);
		} else {
			psGenericCityObject.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (genericCityObject.isSetClazz())
			psGenericCityObject.setString(5, genericCityObject.getClazz().trim());
		else
			psGenericCityObject.setNull(5, Types.VARCHAR);

		// citygml:function
		if (genericCityObject.isSetFunction()) {
			psGenericCityObject.setString(6, Util.collection2string(genericCityObject.getFunction(), " "));
		} else {
			psGenericCityObject.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (genericCityObject.isSetUsage()) {
			psGenericCityObject.setString(7, Util.collection2string(genericCityObject.getUsage(), " "));
		} else {
			psGenericCityObject.setNull(7, Types.VARCHAR);
		}
		
		// Geometry
		for (int lod = 0; lod < 5; lod++) {
        	GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
        	long geometryId = 0;

    		switch (lod) {
    		case 0:
    			geometryProperty = genericCityObject.getLod0Geometry();
    			break;
    		case 1:
    			geometryProperty = genericCityObject.getLod1Geometry();
    			break;
    		case 2:
    			geometryProperty = genericCityObject.getLod2Geometry();
    			break;
    		case 3:
    			geometryProperty = genericCityObject.getLod3Geometry();
    			break;
    		case 4:
    			geometryProperty = genericCityObject.getLod4Geometry();
    			break;
    		}

    		if (geometryProperty != null) {
    			if (geometryProperty.isSetGeometry()) {
    				
    				JGeometry geom = sdoGeometry.getPointOrCurveGeometry(geometryProperty.getGeometry());
    				if (geom != null) {
    					genericAttributeImporter.insert("LOD" + lod + "_Geometry", geom, genericCityObjectId);
    				} else
    					geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), genericCityObjectId);
    			
    			} else {
    				// xlink
					String href = geometryProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						genericCityObjectId,
        						TableEnum.GENERIC_CITYOBJECT,
        						href,
        						TableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_GEOMETRY_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
    		case 0:
        		if (geometryId != 0)
        			psGenericCityObject.setLong(8, geometryId);
        		else
        			psGenericCityObject.setNull(8, 0);
        		break;
    		case 1:
        		if (geometryId != 0)
        			psGenericCityObject.setLong(9, geometryId);
        		else
        			psGenericCityObject.setNull(9, 0);
        		break;
    		case 2:
        		if (geometryId != 0)
        			psGenericCityObject.setLong(10, geometryId);
        		else
        			psGenericCityObject.setNull(10, 0);
        		break;
        	case 3:
        		if (geometryId != 0)
        			psGenericCityObject.setLong(11, geometryId);
        		else
        			psGenericCityObject.setNull(11, 0);
        		break;
        	case 4:
        		if (geometryId != 0)
        			psGenericCityObject.setLong(12, geometryId);
        		else
        			psGenericCityObject.setNull(12, 0);
        		break;
        	}
        }

		for (int lod = 0; lod < 5; lod++) {
			ImplicitRepresentationProperty implicit = null;
			JGeometry pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (lod) {
			case 0:
				implicit = genericCityObject.getLod0ImplicitRepresentation();
				break;
			case 1:
				implicit = genericCityObject.getLod1ImplicitRepresentation();
				break;
			case 2:
				implicit = genericCityObject.getLod2ImplicitRepresentation();
				break;
			case 3:
				implicit = genericCityObject.getLod3ImplicitRepresentation();
				break;
			case 4:
				implicit = genericCityObject.getLod4ImplicitRepresentation();
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
					implicitId = implicitGeometryImporter.insert(geometry, genericCityObjectId);
				}
			}

			switch (lod) {
			case 0:
				if (implicitId != 0)
					psGenericCityObject.setLong(13, implicitId);
				else
					psGenericCityObject.setNull(13, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psGenericCityObject.setObject(18, obj);
				} else
					psGenericCityObject.setNull(18, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psGenericCityObject.setString(23, matrixString);
				else
					psGenericCityObject.setNull(23, Types.VARCHAR);

				break;
			case 1:
				if (implicitId != 0)
					psGenericCityObject.setLong(14, implicitId);
				else
					psGenericCityObject.setNull(14, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psGenericCityObject.setObject(19, obj);
				} else
					psGenericCityObject.setNull(19, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psGenericCityObject.setString(24, matrixString);
				else
					psGenericCityObject.setNull(24, Types.VARCHAR);

				break;
			case 2:
				if (implicitId != 0)
					psGenericCityObject.setLong(15, implicitId);
				else
					psGenericCityObject.setNull(15, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psGenericCityObject.setObject(20, obj);
				} else
					psGenericCityObject.setNull(20, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psGenericCityObject.setString(25, matrixString);
				else
					psGenericCityObject.setNull(25, Types.VARCHAR);

				break;
			case 3:
				if (implicitId != 0)
					psGenericCityObject.setLong(16, implicitId);
				else
					psGenericCityObject.setNull(16, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psGenericCityObject.setObject(21, obj);
				} else
					psGenericCityObject.setNull(21, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psGenericCityObject.setString(26, matrixString);
				else
					psGenericCityObject.setNull(26, Types.VARCHAR);

				break;
			case 4:
				if (implicitId != 0)
					psGenericCityObject.setLong(17, implicitId);
				else
					psGenericCityObject.setNull(17, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psGenericCityObject.setObject(22, obj);
				} else
					psGenericCityObject.setNull(22, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psGenericCityObject.setString(27, matrixString);
				else
					psGenericCityObject.setNull(27, Types.VARCHAR);

				break;
			}
 		}

		// lodXTerrainIntersectionCurve
		for (int lod = 0; lod < 5; lod++) {
			
			MultiCurveProperty multiCurveProperty = null;
			JGeometry multiLine = null;
			
			switch (lod) {
			case 0:
				multiCurveProperty = genericCityObject.getLod0TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = genericCityObject.getLod1TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = genericCityObject.getLod2TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = genericCityObject.getLod3TerrainIntersection();
				break;
			case 4:
				multiCurveProperty = genericCityObject.getLod4TerrainIntersection();
				break;
			}
			
			if (multiCurveProperty != null)
				multiLine = sdoGeometry.getMultiCurve(multiCurveProperty);

			switch (lod) {
			case 0:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psGenericCityObject.setObject(28, multiLineObj);
				} else
					psGenericCityObject.setNull(28, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 1:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psGenericCityObject.setObject(29, multiLineObj);
				} else
					psGenericCityObject.setNull(29, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 2:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psGenericCityObject.setObject(30, multiLineObj);
				} else
					psGenericCityObject.setNull(30, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 3:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psGenericCityObject.setObject(31, multiLineObj);
				} else
					psGenericCityObject.setNull(31, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			case 4:
				if (multiLine != null) {
					STRUCT multiLineObj = SyncJGeometry.syncStore(multiLine, batchConn);
					psGenericCityObject.setObject(32, multiLineObj);
				} else
					psGenericCityObject.setNull(32, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
					
				break;
			}	
		}
		
		psGenericCityObject.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.GENERIC_CITYOBJECT);

		return true;
	}
	
	@Override
	public void executeBatch() throws SQLException {
		psGenericCityObject.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psGenericCityObject.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.GENERIC_CITYOBJECT;
	}

}
