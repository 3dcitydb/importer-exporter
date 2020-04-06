/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

public class DBGenericCityObject implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psGenericCityObject;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private GeometryConverter geometryConverter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean hasObjectClassIdColumn;
	private boolean affineTransformation;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBGenericCityObject(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".generic_cityobject (id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"lod0_terrain_intersection, lod1_terrain_intersection, lod2_terrain_intersection, lod3_terrain_intersection, lod4_terrain_intersection, " +
				"lod0_brep_id, lod1_brep_id, lod2_brep_id, lod3_brep_id, lod4_brep_id, " +
				"lod0_other_geom, lod1_other_geom, lod2_other_geom, lod3_other_geom, lod4_other_geom, " +
				"lod0_implicit_rep_id, lod1_implicit_rep_id, lod2_implicit_rep_id, lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod0_implicit_ref_point, lod1_implicit_ref_point, lod2_implicit_ref_point, lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod0_implicit_transformation, lod1_implicit_transformation, lod2_implicit_transformation, lod3_implicit_transformation, lod4_implicit_transformation" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psGenericCityObject = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(GenericCityObject genericCityObject) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(genericCityObject);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long genericCityObjectId = cityObjectImporter.doImport(genericCityObject, featureType);

		// import generic city object information
		// primary id
		psGenericCityObject.setLong(1, genericCityObjectId);

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
			valueJoiner.join(genericCityObject.getFunction(), Code::getValue, Code::getCodeSpace);
			psGenericCityObject.setString(4, valueJoiner.result(0));
			psGenericCityObject.setString(5, valueJoiner.result(1));
		} else {
			psGenericCityObject.setNull(4, Types.VARCHAR);
			psGenericCityObject.setNull(5, Types.VARCHAR);
		}

		// gen:usage
		if (genericCityObject.isSetUsage()) {
			valueJoiner.join(genericCityObject.getUsage(), Code::getValue, Code::getCodeSpace);
			psGenericCityObject.setString(6, valueJoiner.result(0));
			psGenericCityObject.setString(7, valueJoiner.result(1));
		} else {
			psGenericCityObject.setNull(6, Types.VARCHAR);
			psGenericCityObject.setNull(7, Types.VARCHAR);
		}

		// gen:lodXTerrainIntersectionCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psGenericCityObject.setObject(8 + i, multiLineObj);
			} else
				psGenericCityObject.setNull(8 + i, nullGeometryType, nullGeometryTypeName);
		}

		// gen:lodXGeometry
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
					if (importer.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.doImport(abstractGeometry, genericCityObjectId);
					else if (importer.isPointOrLineGeometry(abstractGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
					else 
						importer.logOrThrowUnsupportedGeometryMessage(genericCityObject, abstractGeometry);

					geometryProperty.unsetGeometry();
				} else {
					String href = geometryProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.GENERIC_CITYOBJECT.getName(),
								genericCityObjectId, 
								href, 
								"lod" + i + "_brep_id"));
					}
				}
			}

			if (geometryId != 0)
				psGenericCityObject.setLong(13 + i, geometryId);
			else
				psGenericCityObject.setNull(13 + i, Types.NULL);

			if (geometryObject != null)
				psGenericCityObject.setObject(18 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psGenericCityObject.setNull(18 + i, nullGeometryType, nullGeometryTypeName);
		}

		// gen:lodXImplicitRepresentation
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
						pointGeom = geometryConverter.getPoint(geometry.getReferencePoint());

					// transformation matrix
					if (geometry.isSetTransformationMatrix()) {
						Matrix matrix = geometry.getTransformationMatrix().getMatrix();
						if (affineTransformation)
							matrix = importer.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

						matrixString = valueJoiner.join(" ", matrix.toRowPackedList());
					}

					// reference to IMPLICIT_GEOMETRY
					implicitId = implicitGeometryImporter.doImport(geometry);
				}
			}

			if (implicitId != 0)
				psGenericCityObject.setLong(23 + i, implicitId);
			else
				psGenericCityObject.setNull(23 + i, Types.NULL);

			if (pointGeom != null)
				psGenericCityObject.setObject(28 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psGenericCityObject.setNull(28 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psGenericCityObject.setString(33 + i, matrixString);
			else
				psGenericCityObject.setNull(33 + i, Types.VARCHAR);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psGenericCityObject.setLong(38, featureType.getObjectClassId());

		psGenericCityObject.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.GENERIC_CITYOBJECT);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(genericCityObject, genericCityObjectId, featureType);

		return genericCityObjectId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psGenericCityObject.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psGenericCityObject.close();
	}

}
