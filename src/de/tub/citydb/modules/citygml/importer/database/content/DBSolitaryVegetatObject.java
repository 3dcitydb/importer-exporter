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

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBSolitaryVegetatObject implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psSolitVegObject;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBSdoGeometry sdoGeometry;
	
	private boolean affineTransformation;
	private int batchCounter;

	public DBSolitaryVegetatObject(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();

		psSolitVegObject = batchConn.prepareStatement("insert into SOLITARY_VEGETAT_OBJECT (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, SPECIES, FUNCTION, " +
				"HEIGHT, TRUNC_DIAMETER, CROWN_DIAMETER, " +
				"LOD1_GEOMETRY_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID, " +
				"LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, " +
				"LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, " +
				"LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(SolitaryVegetationObject solVegObject) throws SQLException {
		long solVegObjectId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (solVegObjectId != 0)
			success = insert(solVegObject, solVegObjectId);

		if (success)
			return solVegObjectId;
		else
			return 0;
	}

	private boolean insert(SolitaryVegetationObject solVegObject, long solVegObjectId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(solVegObject, solVegObjectId, true);
		if (cityObjectId == 0)
			return false;

		// Solitary vegetation object
		// ID
		psSolitVegObject.setLong(1, solVegObjectId);

		// gml:name
		if (solVegObject.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(solVegObject);

			psSolitVegObject.setString(2, dbGmlName[0]);
			psSolitVegObject.setString(3, dbGmlName[1]);
		} else {
			psSolitVegObject.setNull(2, Types.VARCHAR);
			psSolitVegObject.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (solVegObject.isSetDescription()) {
			String description = solVegObject.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psSolitVegObject.setString(4, description);
		} else {
			psSolitVegObject.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (solVegObject.isSetClazz())
			psSolitVegObject.setString(5, solVegObject.getClazz().trim());
		else
			psSolitVegObject.setNull(5, Types.VARCHAR);

		// species
		if (solVegObject.isSetSpecies())
			psSolitVegObject.setString(6, solVegObject.getSpecies());
		else
			psSolitVegObject.setNull(6, Types.VARCHAR);

		// citygml:function
		if (solVegObject.isSetFunction()) {
			psSolitVegObject.setString(7, Util.collection2string(solVegObject.getFunction(), " "));
		} else {
			psSolitVegObject.setNull(7, Types.VARCHAR);
		}

		// height
		if (solVegObject.isSetHeight() && solVegObject.getHeight().isSetValue()) {
			psSolitVegObject.setDouble(8, solVegObject.getHeight().getValue());
		} else {
			psSolitVegObject.setNull(8, Types.DOUBLE);
		}

		// trunc diameter
		if (solVegObject.isSetTrunkDiameter() && solVegObject.getTrunkDiameter().isSetValue()) {
			psSolitVegObject.setDouble(9, solVegObject.getTrunkDiameter().getValue());
		} else {
			psSolitVegObject.setNull(9, Types.DOUBLE);
		}

		// crown diameter
		if (solVegObject.isSetCrownDiameter() && solVegObject.getCrownDiameter().isSetValue()) {
			psSolitVegObject.setDouble(10, solVegObject.getCrownDiameter().getValue());
		} else {
			psSolitVegObject.setNull(10, Types.DOUBLE);
		}

		// Geometry
		for (int lod = 1; lod < 5; lod++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;

    		switch (lod) {
    		case 1:
    			geometryProperty = solVegObject.getLod1Geometry();
    			break;
    		case 2:
    			geometryProperty = solVegObject.getLod2Geometry();
    			break;
    		case 3:
    			geometryProperty = solVegObject.getLod3Geometry();
    			break;
    		case 4:
    			geometryProperty = solVegObject.getLod4Geometry();
    			break;
    		}

    		if (geometryProperty != null) {
    			if (geometryProperty.isSetGeometry()) {
    				geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), solVegObjectId);
    			} else {
    				// xlink
					String href = geometryProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						solVegObjectId,
        						TableEnum.SOLITARY_VEGETAT_OBJECT,
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
        			psSolitVegObject.setLong(11, geometryId);
        		else
        			psSolitVegObject.setNull(11, 0);
        		break;
    		case 2:
        		if (geometryId != 0)
        			psSolitVegObject.setLong(12, geometryId);
        		else
        			psSolitVegObject.setNull(12, 0);
        		break;
        	case 3:
        		if (geometryId != 0)
        			psSolitVegObject.setLong(13, geometryId);
        		else
        			psSolitVegObject.setNull(13, 0);
        		break;
        	case 4:
        		if (geometryId != 0)
        			psSolitVegObject.setLong(14, geometryId);
        		else
        			psSolitVegObject.setNull(14, 0);
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
				implicit = solVegObject.getLod1ImplicitRepresentation();
				break;
			case 2:
				implicit = solVegObject.getLod2ImplicitRepresentation();
				break;
			case 3:
				implicit = solVegObject.getLod3ImplicitRepresentation();
				break;
			case 4:
				implicit = solVegObject.getLod4ImplicitRepresentation();
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
					implicitId = implicitGeometryImporter.insert(geometry, solVegObjectId);
				}
			}

			switch (lod) {
			case 1:
				if (implicitId != 0)
					psSolitVegObject.setLong(15, implicitId);
				else
					psSolitVegObject.setNull(15, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psSolitVegObject.setObject(19, obj);
				} else
					psSolitVegObject.setNull(19, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psSolitVegObject.setString(23, matrixString);
				else
					psSolitVegObject.setNull(23, Types.VARCHAR);

				break;
			case 2:
				if (implicitId != 0)
					psSolitVegObject.setLong(16, implicitId);
				else
					psSolitVegObject.setNull(16, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psSolitVegObject.setObject(20, obj);
				} else
					psSolitVegObject.setNull(20, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psSolitVegObject.setString(24, matrixString);
				else
					psSolitVegObject.setNull(24, Types.VARCHAR);

				break;
			case 3:
				if (implicitId != 0)
					psSolitVegObject.setLong(17, implicitId);
				else
					psSolitVegObject.setNull(17, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psSolitVegObject.setObject(21, obj);
				} else
					psSolitVegObject.setNull(21, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psSolitVegObject.setString(25, matrixString);
				else
					psSolitVegObject.setNull(25, Types.VARCHAR);

				break;
			case 4:
				if (implicitId != 0)
					psSolitVegObject.setLong(18, implicitId);
				else
					psSolitVegObject.setNull(18, 0);

				if (pointGeom != null) {
					STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
					psSolitVegObject.setObject(22, obj);
				} else
					psSolitVegObject.setNull(22, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

				if (matrixString != null)
					psSolitVegObject.setString(26, matrixString);
				else
					psSolitVegObject.setNull(26, Types.VARCHAR);

				break;
			}
 		}

		psSolitVegObject.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.SOLITARY_VEGETAT_OBJECT);
		
		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psSolitVegObject.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psSolitVegObject.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SOLITARY_VEGETAT_OBJECT;
	}

}
