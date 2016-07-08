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
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

public class DBGenericCityObject implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psGenericCityObject;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBGenericCityObject(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into GENERIC_CITYOBJECT (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD0_TERRAIN_INTERSECTION, LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, ")
		.append("LOD0_BREP_ID, LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
		.append("LOD0_OTHER_GEOM, LOD1_OTHER_GEOM, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
		.append("LOD0_IMPLICIT_REP_ID, LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD0_IMPLICIT_REF_POINT, LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD0_IMPLICIT_TRANSFORMATION, LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psGenericCityObject = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(GenericCityObject genericCityObject) throws SQLException {
		long genericCityObjectId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
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

		// genericCityObject
		// ID
		psGenericCityObject.setLong(1, cityObjectId);

		// gen:class
		if (genericCityObject.isSetClazz() && genericCityObject.getClazz().isSetValue()) {
			psGenericCityObject.setString(2, genericCityObject.getClazz().getValue());
			psGenericCityObject.setString(3, genericCityObject.getClazz().getCodeSpace());
		} else {
			psGenericCityObject.setNull(2, Types.VARCHAR);
			psGenericCityObject.setNull(3, Types.VARCHAR);
		}

		// gen:function
		if (genericCityObject.isSetFunction()) {
			String[] function = Util.codeList2string(genericCityObject.getFunction());
			psGenericCityObject.setString(4, function[0]);
			psGenericCityObject.setString(5, function[1]);
		} else {
			psGenericCityObject.setNull(4, Types.VARCHAR);
			psGenericCityObject.setNull(5, Types.VARCHAR);
		}

		// gen:usage
		if (genericCityObject.isSetUsage()) {
			String[] usage = Util.codeList2string(genericCityObject.getUsage());
			psGenericCityObject.setString(6, usage[0]);
			psGenericCityObject.setString(7, usage[1]);
		} else {
			psGenericCityObject.setNull(6, Types.VARCHAR);
			psGenericCityObject.setNull(7, Types.VARCHAR);
		}

		// Geometry
		// lodXTerrainIntersectionCurve
		for (int i = 0; i < 5; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
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

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psGenericCityObject.setObject(8 + i, multiLineObj);
			} else
				psGenericCityObject.setNull(8 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXGeometry
		for (int i = 0; i < 5; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
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
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.insert(abstractGeometry, genericCityObjectId);
					else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								genericCityObject.getCityGMLClass(), 
								genericCityObject.getId()));
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
								genericCityObjectId, 
								TableEnum.GENERIC_CITYOBJECT, 
								"LOD" + i + "_BREP_ID"));
					}
				}
			}

			if (geometryId != 0)
				psGenericCityObject.setLong(13 + i, geometryId);
			else
				psGenericCityObject.setNull(13 + i, Types.NULL);

			if (geometryObject != null)
				psGenericCityObject.setObject(18 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psGenericCityObject.setNull(18 + i, nullGeometryType, nullGeometryTypeName);
		}

		// implicit geometry
		for (int i = 0; i < 5; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
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
						pointGeom = otherGeometryImporter.getPoint(geometry.getReferencePoint());

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

			if (implicitId != 0)
				psGenericCityObject.setLong(23 + i, implicitId);
			else
				psGenericCityObject.setNull(23 + i, Types.NULL);

			if (pointGeom != null)
				psGenericCityObject.setObject(28 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psGenericCityObject.setNull(28 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psGenericCityObject.setString(33 + i, matrixString);
			else
				psGenericCityObject.setNull(33 + i, Types.VARCHAR);
		}

		psGenericCityObject.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.GENERIC_CITYOBJECT);

		// insert local appearance
		cityObjectImporter.insertAppearance(genericCityObject, genericCityObjectId);

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
