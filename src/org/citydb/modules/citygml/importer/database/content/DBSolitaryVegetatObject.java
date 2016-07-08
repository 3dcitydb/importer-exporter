/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBSolitaryVegetatObject implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psSolitVegObject;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBSolitaryVegetatObject(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into SOLITARY_VEGETAT_OBJECT (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("SPECIES, SPECIES_CODESPACE, HEIGHT, HEIGHT_UNIT, TRUNK_DIAMETER, TRUNK_DIAMETER_UNIT, CROWN_DIAMETER, CROWN_DIAMETER_UNIT, ")
		.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
		.append("LOD1_OTHER_GEOM, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
		.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psSolitVegObject = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
	}

	public long insert(SolitaryVegetationObject solVegObject) throws SQLException {
		long solVegObjectId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
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

		// veg:class
		if (solVegObject.isSetClazz() && solVegObject.getClazz().isSetValue()) {
			psSolitVegObject.setString(2, solVegObject.getClazz().getValue());
			psSolitVegObject.setString(3, solVegObject.getClazz().getCodeSpace());
		} else {
			psSolitVegObject.setNull(2, Types.VARCHAR);
			psSolitVegObject.setNull(3, Types.VARCHAR);
		}

		// veg:function
		if (solVegObject.isSetFunction()) {
			String[] function = Util.codeList2string(solVegObject.getFunction());
			psSolitVegObject.setString(4, function[0]);
			psSolitVegObject.setString(5, function[1]);
		} else {
			psSolitVegObject.setNull(4, Types.VARCHAR);
			psSolitVegObject.setNull(5, Types.VARCHAR);
		}

		// veg:usage
		if (solVegObject.isSetUsage()) {
			String[] usage = Util.codeList2string(solVegObject.getUsage());
			psSolitVegObject.setString(6, usage[0]);
			psSolitVegObject.setString(7, usage[1]);
		} else {
			psSolitVegObject.setNull(6, Types.VARCHAR);
			psSolitVegObject.setNull(7, Types.VARCHAR);
		}

		// veg:species
		if (solVegObject.isSetSpecies() && solVegObject.getSpecies().isSetValue()) {
			psSolitVegObject.setString(8, solVegObject.getSpecies().getValue());
			psSolitVegObject.setString(9, solVegObject.getSpecies().getCodeSpace());
		} else {
			psSolitVegObject.setNull(8, Types.VARCHAR);
			psSolitVegObject.setNull(9, Types.VARCHAR);
		}

		// veg:height
		if (solVegObject.isSetHeight() && solVegObject.getHeight().isSetValue()) {
			psSolitVegObject.setDouble(10, solVegObject.getHeight().getValue());
			psSolitVegObject.setString(11, solVegObject.getHeight().getUom());
		} else {
			psSolitVegObject.setNull(10, Types.NULL);
			psSolitVegObject.setNull(11, Types.VARCHAR);
		}
		
		// veg:trunkDiameter
		if (solVegObject.isSetTrunkDiameter() && solVegObject.getTrunkDiameter().isSetValue()) {
			psSolitVegObject.setDouble(12, solVegObject.getTrunkDiameter().getValue());
			psSolitVegObject.setString(13, solVegObject.getTrunkDiameter().getUom());
		} else {
			psSolitVegObject.setNull(12, Types.NULL);
			psSolitVegObject.setNull(13, Types.VARCHAR);
		}

		// veg:crownDiameter
		if (solVegObject.isSetCrownDiameter() && solVegObject.getCrownDiameter().isSetValue()) {
			psSolitVegObject.setDouble(14, solVegObject.getCrownDiameter().getValue());
			psSolitVegObject.setString(15, solVegObject.getCrownDiameter().getUom());
		} else {
			psSolitVegObject.setNull(14, Types.NULL);
			psSolitVegObject.setNull(15, Types.VARCHAR);
		}

		// Geometry
		for (int i = 0; i < 4; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = solVegObject.getLod1Geometry();
				break;
			case 1:
				geometryProperty = solVegObject.getLod2Geometry();
				break;
			case 2:
				geometryProperty = solVegObject.getLod3Geometry();
				break;
			case 3:
				geometryProperty = solVegObject.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.insert(abstractGeometry, solVegObjectId);
					else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								solVegObject.getCityGMLClass(), 
								solVegObject.getId()));
						msg.append(": Unsupported geometry type ");
						msg.append(abstractGeometry.getGMLClass()).append('.');
						
						LOG.error(msg.toString());
					}
					
					geometryProperty.unsetGeometry();
				} else {
					// xlink
					String href = geometryProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								solVegObjectId, 
								TableEnum.SOLITARY_VEGETAT_OBJECT, 
								"LOD" + (i + 1) + "_BREP_ID"));
					}
				}
			}

			if (geometryId != 0)
				psSolitVegObject.setLong(16 + i, geometryId);
			else
				psSolitVegObject.setNull(16 + i, Types.NULL);

			if (geometryObject != null)
				psSolitVegObject.setObject(20 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psSolitVegObject.setNull(20 + i, nullGeometryType, nullGeometryTypeName);
		}
		
		// implicit geometry
		for (int i = 0; i < 4; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = solVegObject.getLod1ImplicitRepresentation();
				break;
			case 1:
				implicit = solVegObject.getLod2ImplicitRepresentation();
				break;
			case 2:
				implicit = solVegObject.getLod3ImplicitRepresentation();
				break;
			case 3:
				implicit = solVegObject.getLod4ImplicitRepresentation();
				break;
			}

			if (implicit != null) {
				if (implicit.isSetObject()) {
					ImplicitGeometry geometry = implicit.getObject();

					// reference Point
					if (geometry.isSetReferencePoint())
						pointGeom = otherGeometryImporter.getPoint(geometry.getReferencePoint());

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

			if (implicitId != 0)
				psSolitVegObject.setLong(24 + i, implicitId);
			else
				psSolitVegObject.setNull(24 + i, Types.NULL);

			if (pointGeom != null)
				psSolitVegObject.setObject(28 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psSolitVegObject.setNull(28 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psSolitVegObject.setString(32 + i, matrixString);
			else
				psSolitVegObject.setNull(32 + i, Types.VARCHAR);
		}

		psSolitVegObject.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.SOLITARY_VEGETAT_OBJECT);

		// insert local appearance
		cityObjectImporter.insertAppearance(solVegObject, solVegObjectId);

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
