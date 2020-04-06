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

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBuildingInstallation implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psBuildingInstallation;
	private DBCityObject cityObjectImporter;
	private DBThematicSurface thematicSurfaceImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean affineTransformation;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBBuildingInstallation(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".building_installation (id, objectclass_id, class, class_codespace, function, function_codespace, usage, usage_codespace, building_id, room_id, " +
				"lod2_brep_id, lod3_brep_id, lod4_brep_id, lod2_other_geom, lod3_other_geom, lod4_other_geom, " +
				"lod2_implicit_rep_id, lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod2_implicit_ref_point, lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod2_implicit_transformation, lod3_implicit_transformation, lod4_implicit_transformation) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		psBuildingInstallation = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBThematicSurface.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(BuildingInstallation buildingInstallation) throws CityGMLImportException, SQLException {
		return doImport(buildingInstallation, null, 0);
	}

	protected long doImport(IntBuildingInstallation intBuildingInstallation) throws CityGMLImportException, SQLException {
		return doImport(intBuildingInstallation, null, 0);
	}

	public long doImport(BuildingInstallation buildingInstallation, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(buildingInstallation);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long buildingInstallationId = cityObjectImporter.doImport(buildingInstallation, featureType);

		// import building installation information
		// primary id
		psBuildingInstallation.setLong(1, buildingInstallationId);

		// objectclass id
		psBuildingInstallation.setLong(2, featureType.getObjectClassId());

		// bldg:class
		if (buildingInstallation.isSetClazz() && buildingInstallation.getClazz().isSetValue()) {
			psBuildingInstallation.setString(3, buildingInstallation.getClazz().getValue());
			psBuildingInstallation.setString(4, buildingInstallation.getClazz().getCodeSpace());
		} else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// bldg:function
		if (buildingInstallation.isSetFunction()) {
			valueJoiner.join(buildingInstallation.getFunction(), Code::getValue, Code::getCodeSpace);
			psBuildingInstallation.setString(5, valueJoiner.result(0));
			psBuildingInstallation.setString(6, valueJoiner.result(1));
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
			psBuildingInstallation.setNull(6, Types.VARCHAR);
		}

		// bldg:usage
		if (buildingInstallation.isSetUsage()) {
			valueJoiner.join(buildingInstallation.getUsage(), Code::getValue, Code::getCodeSpace);
			psBuildingInstallation.setString(7, valueJoiner.result(0));
			psBuildingInstallation.setString(8, valueJoiner.result(1));
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
			psBuildingInstallation.setNull(8, Types.VARCHAR);
		}

		// parent id
		if (parent instanceof AbstractBuilding) {
			psBuildingInstallation.setLong(9, parentId);
			psBuildingInstallation.setNull(10, Types.NULL);
		} else if (parent instanceof Room) {
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setLong(10, parentId);
		} else {
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setNull(10, Types.NULL);
		}

		// bldg:lodXGeometry
		for (int i = 0; i < 3; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = buildingInstallation.getLod2Geometry();
				break;
			case 1:
				geometryProperty = buildingInstallation.getLod3Geometry();
				break;
			case 2:
				geometryProperty = buildingInstallation.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (importer.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.doImport(abstractGeometry, buildingInstallationId);
					else if (importer.isPointOrLineGeometry(abstractGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
					else 
						importer.logOrThrowUnsupportedGeometryMessage(buildingInstallation, abstractGeometry);

					geometryProperty.unsetGeometry();
				} else {
					String href = geometryProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BUILDING_INSTALLATION.getName(),
								buildingInstallationId, 
								href, 
								"lod" + (i + 2) + "_brep_id"));
					}
				}
			}

			if (geometryId != 0)
				psBuildingInstallation.setLong(11 + i, geometryId);
			else
				psBuildingInstallation.setNull(11 + i, Types.NULL);

			if (geometryObject != null)
				psBuildingInstallation.setObject(14 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psBuildingInstallation.setNull(14 + i, nullGeometryType, nullGeometryTypeName);
		}

		// bldg:lodXImplicitRepresentation
		for (int i = 0; i < 3; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = buildingInstallation.getLod2ImplicitRepresentation();
				break;
			case 1:
				implicit = buildingInstallation.getLod3ImplicitRepresentation();
				break;
			case 2:
				implicit = buildingInstallation.getLod4ImplicitRepresentation();
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
				psBuildingInstallation.setLong(17 + i, implicitId);
			else
				psBuildingInstallation.setNull(17 + i, Types.NULL);

			if (pointGeom != null)
				psBuildingInstallation.setObject(20 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psBuildingInstallation.setNull(20 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psBuildingInstallation.setString(23 + i, matrixString);
			else
				psBuildingInstallation.setNull(23 + i, Types.VARCHAR);
		}

		psBuildingInstallation.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BUILDING_INSTALLATION);

		// bldg:boundedBy
		if (buildingInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : buildingInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, buildingInstallation, buildingInstallationId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.THEMATIC_SURFACE.getName(),
								href,
								buildingInstallationId,
								"building_installation_id"));
					}
				}
			}
		}

		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(buildingInstallation, buildingInstallationId, featureType);
		
		return buildingInstallationId;
	}

	public long doImport(IntBuildingInstallation intBuildingInstallation, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(intBuildingInstallation);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long intBuildingInstallationId = cityObjectImporter.doImport(intBuildingInstallation, featureType);

		// import interior building installation information
		// primary id
		psBuildingInstallation.setLong(1, intBuildingInstallationId);

		// objectclass id
		psBuildingInstallation.setLong(2, featureType.getObjectClassId());

		// bldg:class
		if (intBuildingInstallation.isSetClazz() && intBuildingInstallation.getClazz().isSetValue()) {
			psBuildingInstallation.setString(3, intBuildingInstallation.getClazz().getValue());
			psBuildingInstallation.setString(4, intBuildingInstallation.getClazz().getCodeSpace());
		} else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// bldg:function
		if (intBuildingInstallation.isSetFunction()) {
			valueJoiner.join(intBuildingInstallation.getFunction(), Code::getValue, Code::getCodeSpace);
			psBuildingInstallation.setString(5, valueJoiner.result(0));
			psBuildingInstallation.setString(6, valueJoiner.result(1));
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
			psBuildingInstallation.setNull(6, Types.VARCHAR);
		}

		// bldg:usage
		if (intBuildingInstallation.isSetUsage()) {
			valueJoiner.join(intBuildingInstallation.getUsage(), Code::getValue, Code::getCodeSpace);
			psBuildingInstallation.setString(7, valueJoiner.result(0));
			psBuildingInstallation.setString(8, valueJoiner.result(1));
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
			psBuildingInstallation.setNull(8, Types.VARCHAR);
		}

		// parent id
		if (parent instanceof AbstractBuilding) {
			psBuildingInstallation.setLong(9, parentId);
			psBuildingInstallation.setNull(10, Types.NULL);
		} else if (parent instanceof Room) {
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setLong(10, parentId);
		} else {
			psBuildingInstallation.setNull(9, Types.NULL);
			psBuildingInstallation.setNull(10, Types.NULL);
		}	

		// bldg:lod4Geometry
		psBuildingInstallation.setNull(11, Types.NULL);
		psBuildingInstallation.setNull(12, Types.NULL);
		psBuildingInstallation.setNull(14, nullGeometryType, nullGeometryTypeName);
		psBuildingInstallation.setNull(15, nullGeometryType, nullGeometryTypeName);

		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (intBuildingInstallation.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = intBuildingInstallation.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, intBuildingInstallationId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(intBuildingInstallation, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.BUILDING_INSTALLATION.getName(),
							intBuildingInstallationId, 
							href, 
							"lod4_brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psBuildingInstallation.setLong(13, geometryId);
		else
			psBuildingInstallation.setNull(13, Types.NULL);

		if (geometryObject != null)
			psBuildingInstallation.setObject(16, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBuildingInstallation.setNull(16, nullGeometryType, nullGeometryTypeName);

		// bldg:lod4ImplicitRepresentation
		psBuildingInstallation.setNull(17, Types.NULL);
		psBuildingInstallation.setNull(18, Types.NULL);
		psBuildingInstallation.setNull(20, nullGeometryType, nullGeometryTypeName);
		psBuildingInstallation.setNull(21, nullGeometryType, nullGeometryTypeName);
		psBuildingInstallation.setNull(23, Types.VARCHAR);
		psBuildingInstallation.setNull(24, Types.VARCHAR);

		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (intBuildingInstallation.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = intBuildingInstallation.getLod4ImplicitRepresentation();

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
			psBuildingInstallation.setLong(19, implicitId);
		else
			psBuildingInstallation.setNull(19, Types.NULL);

		if (pointGeom != null)
			psBuildingInstallation.setObject(22, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBuildingInstallation.setNull(22, nullGeometryType, nullGeometryTypeName);

		if (matrixString != null)
			psBuildingInstallation.setString(25, matrixString);
		else
			psBuildingInstallation.setNull(25, Types.VARCHAR);

		psBuildingInstallation.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BUILDING_INSTALLATION);

		// bldg:boundedBy
		if (intBuildingInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : intBuildingInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, intBuildingInstallation, intBuildingInstallationId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.THEMATIC_SURFACE.getName(),
								href,
								intBuildingInstallationId,
								"building_installation_id"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(intBuildingInstallation, intBuildingInstallationId, featureType);

		return intBuildingInstallationId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBuildingInstallation.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBuildingInstallation.close();
	}

}
