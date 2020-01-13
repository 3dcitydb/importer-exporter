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
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBridgeFurniture implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psBridgeFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean hasObjectClassIdColumn;
	private boolean affineTransformation;

	public DBBridgeFurniture(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".bridge_furniture (id, class, class_codespace, function, function_codespace, usage, usage_codespace, bridge_room_id, " +
				"lod4_brep_id, lod4_other_geom, " +
				"lod4_implicit_rep_id, lod4_implicit_ref_point, lod4_implicit_transformation" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psBridgeFurniture = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(BridgeFurniture bridgeFurniture) throws CityGMLImportException, SQLException {
		return doImport(bridgeFurniture, 0);
	}

	protected long doImport(BridgeFurniture bridgeFurniture, long roomId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(bridgeFurniture);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");		

		// import city object information
		long bridgeFurnitureId = cityObjectImporter.doImport(bridgeFurniture, featureType);

		// import bridge furniture information
		// primary id
		psBridgeFurniture.setLong(1, bridgeFurnitureId);

		// brid:class
		if (bridgeFurniture.isSetClazz() && bridgeFurniture.getClazz().isSetValue()) {
			psBridgeFurniture.setString(2, bridgeFurniture.getClazz().getValue());
			psBridgeFurniture.setString(3, bridgeFurniture.getClazz().getCodeSpace());
		} else {
			psBridgeFurniture.setNull(2, Types.VARCHAR);
			psBridgeFurniture.setNull(3, Types.VARCHAR);
		}

		// brid:function
		if (bridgeFurniture.isSetFunction()) {
			valueJoiner.join(bridgeFurniture.getFunction(), Code::getValue, Code::getCodeSpace);
			psBridgeFurniture.setString(4, valueJoiner.result(0));
			psBridgeFurniture.setString(5, valueJoiner.result(1));
		} else {
			psBridgeFurniture.setNull(4, Types.VARCHAR);
			psBridgeFurniture.setNull(5, Types.VARCHAR);
		}

		// brid:usage
		if (bridgeFurniture.isSetUsage()) {
			valueJoiner.join(bridgeFurniture.getUsage(), Code::getValue, Code::getCodeSpace);
			psBridgeFurniture.setString(6, valueJoiner.result(0));
			psBridgeFurniture.setString(7, valueJoiner.result(1));
		} else {
			psBridgeFurniture.setNull(6, Types.VARCHAR);
			psBridgeFurniture.setNull(7, Types.VARCHAR);
		}

		// parent room id
		if (roomId != 0)
			psBridgeFurniture.setLong(8, roomId);
		else
			psBridgeFurniture.setNull(8, Types.NULL);

		// bridg:lod4Geometry		
		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (bridgeFurniture.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = bridgeFurniture.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, bridgeFurnitureId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(bridgeFurniture, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.BRIDGE_FURNITURE.getName(),
							bridgeFurnitureId, 
							href, 
							"lod4_brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psBridgeFurniture.setLong(9, geometryId);
		else
			psBridgeFurniture.setNull(9, Types.NULL);

		if (geometryObject != null)
			psBridgeFurniture.setObject(10, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBridgeFurniture.setNull(10, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// brid:lod4ImplicitRepresentation
		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (bridgeFurniture.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = bridgeFurniture.getLod4ImplicitRepresentation();

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
			psBridgeFurniture.setLong(11, implicitId);
		else
			psBridgeFurniture.setNull(11, Types.NULL);

		if (pointGeom != null)
			psBridgeFurniture.setObject(12, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBridgeFurniture.setNull(12, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		if (matrixString != null)
			psBridgeFurniture.setString(13, matrixString);
		else
			psBridgeFurniture.setNull(13, Types.VARCHAR);

		// objectclass id
		if (hasObjectClassIdColumn)
			psBridgeFurniture.setLong(14, featureType.getObjectClassId());

		psBridgeFurniture.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BRIDGE_FURNITURE);

		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(bridgeFurniture, bridgeFurnitureId, featureType);
		
		return bridgeFurnitureId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBridgeFurniture.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBridgeFurniture.close();
	}

}
