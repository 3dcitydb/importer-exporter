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
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBuildingFurniture implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psBuildingFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private AttributeValueJoiner valueJoiner;	
	private int batchCounter;
	
	private boolean affineTransformation;
	private boolean hasObjectClassIdColumn;

	public DBBuildingFurniture(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".building_furniture (id, class, class_codespace, function, function_codespace, usage, usage_codespace, room_id, " +
				"lod4_brep_id, lod4_other_geom, " +
				"lod4_implicit_rep_id, lod4_implicit_ref_point, lod4_implicit_transformation" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psBuildingFurniture = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(BuildingFurniture buildingFurniture) throws CityGMLImportException, SQLException {
		return doImport(buildingFurniture, 0);
	}

	protected long doImport(BuildingFurniture buildingFurniture, long roomId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(buildingFurniture);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long buildingFurnitureId = cityObjectImporter.doImport(buildingFurniture, featureType);

		// import building furniture information
		// primary id
		psBuildingFurniture.setLong(1, buildingFurnitureId);

		// bldg:class
		if (buildingFurniture.isSetClazz() && buildingFurniture.getClazz().isSetValue()) {
			psBuildingFurniture.setString(2, buildingFurniture.getClazz().getValue());
			psBuildingFurniture.setString(3, buildingFurniture.getClazz().getCodeSpace());
		} else {
			psBuildingFurniture.setNull(2, Types.VARCHAR);
			psBuildingFurniture.setNull(3, Types.VARCHAR);
		}

		// bldg:function
		if (buildingFurniture.isSetFunction()) {
			valueJoiner.join(buildingFurniture.getFunction(), Code::getValue, Code::getCodeSpace);
			psBuildingFurniture.setString(4, valueJoiner.result(0));
			psBuildingFurniture.setString(5, valueJoiner.result(1));
		} else {
			psBuildingFurniture.setNull(4, Types.VARCHAR);
			psBuildingFurniture.setNull(5, Types.VARCHAR);
		}

		// bldg:usage
		if (buildingFurniture.isSetUsage()) {
			valueJoiner.join(buildingFurniture.getUsage(), Code::getValue, Code::getCodeSpace);
			psBuildingFurniture.setString(6, valueJoiner.result(0));
			psBuildingFurniture.setString(7, valueJoiner.result(1));
		} else {
			psBuildingFurniture.setNull(6, Types.VARCHAR);
			psBuildingFurniture.setNull(7, Types.VARCHAR);
		}

		// parent room id
		if (roomId != 0)
			psBuildingFurniture.setLong(8, roomId);
		else
			psBuildingFurniture.setNull(8, Types.NULL);

		// bldg:lod4Geometry		
		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (buildingFurniture.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = buildingFurniture.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, buildingFurnitureId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(buildingFurniture, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.BUILDING_FURNITURE.getName(),
							buildingFurnitureId, 
							href, 
							"lod4_brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psBuildingFurniture.setLong(9, geometryId);
		else
			psBuildingFurniture.setNull(9, Types.NULL);

		if (geometryObject != null)
			psBuildingFurniture.setObject(10, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBuildingFurniture.setNull(10, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// bldg:lod4ImplicitRepresentation
		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (buildingFurniture.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = buildingFurniture.getLod4ImplicitRepresentation();

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
			psBuildingFurniture.setLong(11, implicitId);
		else
			psBuildingFurniture.setNull(11, Types.NULL);

		if (pointGeom != null)
			psBuildingFurniture.setObject(12, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBuildingFurniture.setNull(12, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		if (matrixString != null)
			psBuildingFurniture.setString(13, matrixString);
		else
			psBuildingFurniture.setNull(13, Types.VARCHAR);

		// objectclass id
		if (hasObjectClassIdColumn)
			psBuildingFurniture.setLong(14, featureType.getObjectClassId());

		psBuildingFurniture.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BUILDING_FURNITURE);
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(buildingFurniture, buildingFurnitureId, featureType);

		return buildingFurnitureId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBuildingFurniture.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBuildingFurniture.close();
	}

}
