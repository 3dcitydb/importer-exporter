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
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

public class DBCityFurniture implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psCityFurniture;
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

	public DBCityFurniture(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".city_furniture (id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"lod1_terrain_intersection, lod2_terrain_intersection, lod3_terrain_intersection, lod4_terrain_intersection, " +
				"lod1_brep_id, lod2_brep_id, lod3_brep_id, lod4_brep_id, " +
				"lod1_other_geom, lod2_other_geom, lod3_other_geom, lod4_other_geom, " +
				"lod1_implicit_rep_id, lod2_implicit_rep_id, lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod1_implicit_ref_point, lod2_implicit_ref_point, lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod1_implicit_transformation, lod2_implicit_transformation, lod3_implicit_transformation, lod4_implicit_transformation" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psCityFurniture = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(CityFurniture cityFurniture) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(cityFurniture);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long cityFurnitureId = cityObjectImporter.doImport(cityFurniture, featureType);

		// import city furniture information
		// primary id
		psCityFurniture.setLong(1, cityFurnitureId);

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
			valueJoiner.join(cityFurniture.getFunction(), Code::getValue, Code::getCodeSpace);
			psCityFurniture.setString(4, valueJoiner.result(0));
			psCityFurniture.setString(5, valueJoiner.result(1));
		} else {
			psCityFurniture.setNull(4, Types.VARCHAR);
			psCityFurniture.setNull(5, Types.VARCHAR);
		}

		// frn:usage
		if (cityFurniture.isSetUsage()) {
			valueJoiner.join(cityFurniture.getUsage(), Code::getValue, Code::getCodeSpace);
			psCityFurniture.setString(6, valueJoiner.result(0));
			psCityFurniture.setString(7, valueJoiner.result(1));
		} else {
			psCityFurniture.setNull(6, Types.VARCHAR);
			psCityFurniture.setNull(7, Types.VARCHAR);
		}

		// frn:lodXTerrainIntersectionCurve
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
				multiLine = geometryConverter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null)
				psCityFurniture.setObject(8 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn));
			else
				psCityFurniture.setNull(8 + i, nullGeometryType, nullGeometryTypeName);
		}

		// frn:lodXGeometry
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
					if (importer.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.doImport(abstractGeometry, cityFurnitureId);
					else if (importer.isPointOrLineGeometry(abstractGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
					else
						importer.logOrThrowUnsupportedGeometryMessage(cityFurniture, abstractGeometry);

					geometryProperty.unsetGeometry();
				} else {
					String href = geometryProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.CITY_FURNITURE.getName(),
								cityFurnitureId, 
								href, 
								"lod" + (i + 1) + "_brep_id"));
					}
				}
			}

			if (geometryId != 0)
				psCityFurniture.setLong(12 + i, geometryId);
			else
				psCityFurniture.setNull(12 + i, Types.NULL);

			if (geometryObject != null)
				psCityFurniture.setObject(16 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psCityFurniture.setNull(16 + i, nullGeometryType, nullGeometryTypeName);
		}

		// frn:lodXImplicitRepresentation
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
				psCityFurniture.setLong(20 + i, implicitId);
			else
				psCityFurniture.setNull(20 + i, Types.NULL);

			if (pointGeom != null)
				psCityFurniture.setObject(24 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psCityFurniture.setNull(24 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psCityFurniture.setString(28 + i, matrixString);
			else
				psCityFurniture.setNull(28 + i, Types.VARCHAR);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psCityFurniture.setLong(32, featureType.getObjectClassId());

		psCityFurniture.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.CITY_FURNITURE);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(cityFurniture, cityFurnitureId, featureType);

		return cityFurnitureId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psCityFurniture.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psCityFurniture.close();
	}

}
