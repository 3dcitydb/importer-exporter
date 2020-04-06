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
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBSolitaryVegetatObject implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psSolitVegObject;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean hasObjectClassIdColumn;
	private boolean affineTransformation;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBSolitaryVegetatObject(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".solitary_vegetat_object (id, class, class_codespace, function, function_codespace, usage, usage_codespace, " +
				"species, species_codespace, height, height_unit, trunk_diameter, trunk_diameter_unit, crown_diameter, crown_diameter_unit, " +
				"lod1_brep_id, lod2_brep_id, lod3_brep_id, lod4_brep_id, " +
				"lod1_other_geom, lod2_other_geom, lod3_other_geom, lod4_other_geom, " +
				"lod1_implicit_rep_id, lod2_implicit_rep_id, lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod1_implicit_ref_point, lod2_implicit_ref_point, lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod1_implicit_transformation, lod2_implicit_transformation, lod3_implicit_transformation, lod4_implicit_transformation" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psSolitVegObject = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(SolitaryVegetationObject vegetationObject) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(vegetationObject);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long vegetationObjectId = cityObjectImporter.doImport(vegetationObject, featureType);

		// import solitary vegetation object information
		// primary id
		psSolitVegObject.setLong(1, vegetationObjectId);

		// veg:class
		if (vegetationObject.isSetClazz() && vegetationObject.getClazz().isSetValue()) {
			psSolitVegObject.setString(2, vegetationObject.getClazz().getValue());
			psSolitVegObject.setString(3, vegetationObject.getClazz().getCodeSpace());
		} else {
			psSolitVegObject.setNull(2, Types.VARCHAR);
			psSolitVegObject.setNull(3, Types.VARCHAR);
		}

		// veg:function
		if (vegetationObject.isSetFunction()) {
			valueJoiner.join(vegetationObject.getFunction(), Code::getValue, Code::getCodeSpace);
			psSolitVegObject.setString(4, valueJoiner.result(0));
			psSolitVegObject.setString(5, valueJoiner.result(1));
		} else {
			psSolitVegObject.setNull(4, Types.VARCHAR);
			psSolitVegObject.setNull(5, Types.VARCHAR);
		}

		// veg:usage
		if (vegetationObject.isSetUsage()) {
			valueJoiner.join(vegetationObject.getUsage(), Code::getValue, Code::getCodeSpace);
			psSolitVegObject.setString(6, valueJoiner.result(0));
			psSolitVegObject.setString(7, valueJoiner.result(1));
		} else {
			psSolitVegObject.setNull(6, Types.VARCHAR);
			psSolitVegObject.setNull(7, Types.VARCHAR);
		}

		// veg:species
		if (vegetationObject.isSetSpecies() && vegetationObject.getSpecies().isSetValue()) {
			psSolitVegObject.setString(8, vegetationObject.getSpecies().getValue());
			psSolitVegObject.setString(9, vegetationObject.getSpecies().getCodeSpace());
		} else {
			psSolitVegObject.setNull(8, Types.VARCHAR);
			psSolitVegObject.setNull(9, Types.VARCHAR);
		}

		// veg:height
		if (vegetationObject.isSetHeight() && vegetationObject.getHeight().isSetValue()) {
			psSolitVegObject.setDouble(10, vegetationObject.getHeight().getValue());
			psSolitVegObject.setString(11, vegetationObject.getHeight().getUom());
		} else {
			psSolitVegObject.setNull(10, Types.NULL);
			psSolitVegObject.setNull(11, Types.VARCHAR);
		}

		// veg:trunkDiameter
		if (vegetationObject.isSetTrunkDiameter() && vegetationObject.getTrunkDiameter().isSetValue()) {
			psSolitVegObject.setDouble(12, vegetationObject.getTrunkDiameter().getValue());
			psSolitVegObject.setString(13, vegetationObject.getTrunkDiameter().getUom());
		} else {
			psSolitVegObject.setNull(12, Types.NULL);
			psSolitVegObject.setNull(13, Types.VARCHAR);
		}

		// veg:crownDiameter
		if (vegetationObject.isSetCrownDiameter() && vegetationObject.getCrownDiameter().isSetValue()) {
			psSolitVegObject.setDouble(14, vegetationObject.getCrownDiameter().getValue());
			psSolitVegObject.setString(15, vegetationObject.getCrownDiameter().getUom());
		} else {
			psSolitVegObject.setNull(14, Types.NULL);
			psSolitVegObject.setNull(15, Types.VARCHAR);
		}

		// veg:lodXGeometry
		for (int i = 0; i < 4; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = vegetationObject.getLod1Geometry();
				break;
			case 1:
				geometryProperty = vegetationObject.getLod2Geometry();
				break;
			case 2:
				geometryProperty = vegetationObject.getLod3Geometry();
				break;
			case 3:
				geometryProperty = vegetationObject.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (importer.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.doImport(abstractGeometry, vegetationObjectId);
					else if (importer.isPointOrLineGeometry(abstractGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
					else 
						importer.logOrThrowUnsupportedGeometryMessage(vegetationObject, abstractGeometry);

					geometryProperty.unsetGeometry();
				} else {
					String href = geometryProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.SOLITARY_VEGETAT_OBJECT.getName(),
								vegetationObjectId, 
								href, 
								"lod" + (i + 1) + "_brep_id"));
					}
				}
			}

			if (geometryId != 0)
				psSolitVegObject.setLong(16 + i, geometryId);
			else
				psSolitVegObject.setNull(16 + i, Types.NULL);

			if (geometryObject != null)
				psSolitVegObject.setObject(20 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psSolitVegObject.setNull(20 + i, nullGeometryType, nullGeometryTypeName);
		}

		// veg:lodXImplicitRepresentation
		for (int i = 0; i < 4; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = vegetationObject.getLod1ImplicitRepresentation();
				break;
			case 1:
				implicit = vegetationObject.getLod2ImplicitRepresentation();
				break;
			case 2:
				implicit = vegetationObject.getLod3ImplicitRepresentation();
				break;
			case 3:
				implicit = vegetationObject.getLod4ImplicitRepresentation();
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
				psSolitVegObject.setLong(24 + i, implicitId);
			else
				psSolitVegObject.setNull(24 + i, Types.NULL);

			if (pointGeom != null)
				psSolitVegObject.setObject(28 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psSolitVegObject.setNull(28 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psSolitVegObject.setString(32 + i, matrixString);
			else
				psSolitVegObject.setNull(32 + i, Types.VARCHAR);
		}

		// objectclass id
		if (hasObjectClassIdColumn)
			psSolitVegObject.setLong(36, featureType.getObjectClassId());

		psSolitVegObject.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.SOLITARY_VEGETAT_OBJECT);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(vegetationObject, vegetationObjectId, featureType);

		return vegetationObjectId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psSolitVegObject.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psSolitVegObject.close();
	}

}
