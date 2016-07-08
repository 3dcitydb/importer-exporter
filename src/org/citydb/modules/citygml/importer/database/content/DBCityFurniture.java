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
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

public class DBCityFurniture implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBCityFurniture(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into CITY_FURNITURE (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, ")
		.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, ")
		.append("LOD1_OTHER_GEOM, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
		.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psCityFurniture = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(CityFurniture cityFurniture) throws SQLException {
		long cityFurnitureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
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

		// frn:class
		if (cityFurniture.isSetClazz() && cityFurniture.getClazz().isSetValue()) {
			psCityFurniture.setString(2, cityFurniture.getClazz().getValue());
			psCityFurniture.setString(3, cityFurniture.getClazz().getCodeSpace());
		} else {
			psCityFurniture.setNull(2, Types.VARCHAR);
			psCityFurniture.setNull(3, Types.VARCHAR);
		}

		// frn:function
		if (cityFurniture.isSetFunction()) {
			String[] function = Util.codeList2string(cityFurniture.getFunction());
			psCityFurniture.setString(4, function[0]);
			psCityFurniture.setString(5, function[1]);
		} else {
			psCityFurniture.setNull(4, Types.VARCHAR);
			psCityFurniture.setNull(5, Types.VARCHAR);
		}

		// frn:usage
		if (cityFurniture.isSetUsage()) {
			String[] usage = Util.codeList2string(cityFurniture.getUsage());
			psCityFurniture.setString(6, usage[0]);
			psCityFurniture.setString(7, usage[1]);
		} else {
			psCityFurniture.setNull(6, Types.VARCHAR);
			psCityFurniture.setNull(7, Types.VARCHAR);
		}

		// Geometry
		// lodXTerrainIntersectionCurve
		for (int i = 0; i < 4; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = cityFurniture.getLod1TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = cityFurniture.getLod2TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = cityFurniture.getLod3TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = cityFurniture.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null)
				psCityFurniture.setObject(8 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn));
			else
				psCityFurniture.setNull(8 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXGeometry
		for (int i = 0; i < 4; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = cityFurniture.getLod1Geometry();
				break;
			case 1:
				geometryProperty = cityFurniture.getLod2Geometry();
				break;
			case 2:
				geometryProperty = cityFurniture.getLod3Geometry();
				break;
			case 3:
				geometryProperty = cityFurniture.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.insert(abstractGeometry, cityFurnitureId);
					else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								cityFurniture.getCityGMLClass(), 
								cityFurniture.getId()));
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
								cityFurnitureId, 
								TableEnum.CITY_FURNITURE, 
								"LOD" + (i + 1) + "_BREP_ID"));
					}
				}
			}

			if (geometryId != 0)
				psCityFurniture.setLong(12 + i, geometryId);
			else
				psCityFurniture.setNull(12 + i, Types.NULL);

			if (geometryObject != null)
				psCityFurniture.setObject(16 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psCityFurniture.setNull(16 + i, nullGeometryType, nullGeometryTypeName);
		}

		// implicit geometry
		for (int i = 0; i < 4; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = cityFurniture.getLod1ImplicitRepresentation();
				break;
			case 1:
				implicit = cityFurniture.getLod2ImplicitRepresentation();
				break;
			case 2:
				implicit = cityFurniture.getLod3ImplicitRepresentation();
				break;
			case 3:
				implicit = cityFurniture.getLod4ImplicitRepresentation();
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
					implicitId = implicitGeometryImporter.insert(geometry, cityFurnitureId);
				}
			}

			if (implicitId != 0)
				psCityFurniture.setLong(20 + i, implicitId);
			else
				psCityFurniture.setNull(20 + i, Types.NULL);

			if (pointGeom != null)
				psCityFurniture.setObject(24 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psCityFurniture.setNull(24 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psCityFurniture.setString(28 + i, matrixString);
			else
				psCityFurniture.setNull(28 + i, Types.VARCHAR);
		}

		psCityFurniture.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.CITY_FURNITURE);

		// insert local appearance
		cityObjectImporter.insertAppearance(cityFurniture, cityFurnitureId);

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
