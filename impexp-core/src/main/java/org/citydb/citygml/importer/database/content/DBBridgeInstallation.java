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
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBridgeInstallation implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psBridgeInstallation;
	private DBCityObject cityObjectImporter;
	private DBBridgeThematicSurface thematicSurfaceImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private GeometryConverter geometryConverter;
	private DBImplicitGeometry implicitGeometryImporter;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private boolean affineTransformation;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBBridgeInstallation(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String stmt = "insert into " + schema + ".bridge_installation (id, objectclass_id, class, class_codespace, function, function_codespace, usage, usage_codespace, bridge_id, bridge_room_id, " +
				"lod2_brep_id, lod3_brep_id, lod4_brep_id, lod2_other_geom, lod3_other_geom, lod4_other_geom, " +
				"lod2_implicit_rep_id, lod3_implicit_rep_id, lod4_implicit_rep_id, " +
				"lod2_implicit_ref_point, lod3_implicit_ref_point, lod4_implicit_ref_point, " +
				"lod2_implicit_transformation, lod3_implicit_transformation, lod4_implicit_transformation) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		psBridgeInstallation = batchConn.prepareStatement(stmt);

		surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
		implicitGeometryImporter = importer.getImporter(DBImplicitGeometry.class);
		cityObjectImporter = importer.getImporter(DBCityObject.class);
		thematicSurfaceImporter = importer.getImporter(DBBridgeThematicSurface.class);
		geometryConverter = importer.getGeometryConverter();
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(BridgeInstallation bridgeInstallation) throws CityGMLImportException, SQLException {
		return doImport(bridgeInstallation, null, 0);
	}

	protected long doImport(IntBridgeInstallation intBridgeInstallation) throws CityGMLImportException, SQLException {
		return doImport(intBridgeInstallation, null, 0);
	}

	public long doImport(BridgeInstallation bridgeInstallation, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(bridgeInstallation);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long bridgeInstallationId = cityObjectImporter.doImport(bridgeInstallation, featureType);

		// import bridge installation information
		// primary id
		psBridgeInstallation.setLong(1, bridgeInstallationId);

		// objectclass id
		psBridgeInstallation.setLong(2, featureType.getObjectClassId());

		// brid:class
		if (bridgeInstallation.isSetClazz() && bridgeInstallation.getClazz().isSetValue()) {
			psBridgeInstallation.setString(3, bridgeInstallation.getClazz().getValue());
			psBridgeInstallation.setString(4, bridgeInstallation.getClazz().getCodeSpace());
		} else {
			psBridgeInstallation.setNull(3, Types.VARCHAR);
			psBridgeInstallation.setNull(4, Types.VARCHAR);
		}

		// brid:function
		if (bridgeInstallation.isSetFunction()) {
			valueJoiner.join(bridgeInstallation.getFunction(), Code::getValue, Code::getCodeSpace);
			psBridgeInstallation.setString(5, valueJoiner.result(0));
			psBridgeInstallation.setString(6, valueJoiner.result(1));
		} else {
			psBridgeInstallation.setNull(5, Types.VARCHAR);
			psBridgeInstallation.setNull(6, Types.VARCHAR);
		}

		// brid:usage
		if (bridgeInstallation.isSetUsage()) {
			valueJoiner.join(bridgeInstallation.getUsage(), Code::getValue, Code::getCodeSpace);
			psBridgeInstallation.setString(7, valueJoiner.result(0));
			psBridgeInstallation.setString(8, valueJoiner.result(1));
		} else {
			psBridgeInstallation.setNull(7, Types.VARCHAR);
			psBridgeInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		if (parent instanceof AbstractBridge) {
			psBridgeInstallation.setLong(9, parentId);
			psBridgeInstallation.setNull(10, Types.NULL);
		} else if (parent instanceof BridgeRoom) {
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setLong(10, parentId);
		} else {
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setNull(10, Types.NULL);
		}

		// brid:lodXGeometry
		for (int i = 0; i < 3; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = bridgeInstallation.getLod2Geometry();
				break;
			case 1:
				geometryProperty = bridgeInstallation.getLod3Geometry();
				break;
			case 2:
				geometryProperty = bridgeInstallation.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (importer.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.doImport(abstractGeometry, bridgeInstallationId);
					else if (importer.isPointOrLineGeometry(abstractGeometry))
						geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
					else
						importer.logOrThrowUnsupportedGeometryMessage(bridgeInstallation, abstractGeometry);

					geometryProperty.unsetGeometry();
				} else {
					String href = geometryProperty.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkSurfaceGeometry(
								TableEnum.BRIDGE_INSTALLATION.getName(),
								bridgeInstallationId, 
								href, 
								"lod" + (i + 2) + "_brep_id"));
					}
				}
			}

			if (geometryId != 0)
				psBridgeInstallation.setLong(11 + i, geometryId);
			else
				psBridgeInstallation.setNull(11 + i, Types.NULL);

			if (geometryObject != null)
				psBridgeInstallation.setObject(14 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psBridgeInstallation.setNull(14 + i, nullGeometryType, nullGeometryTypeName);
		}

		// brid:lodXImplicitRepresentation
		for (int i = 0; i < 3; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = bridgeInstallation.getLod2ImplicitRepresentation();
				break;
			case 1:
				implicit = bridgeInstallation.getLod3ImplicitRepresentation();
				break;
			case 2:
				implicit = bridgeInstallation.getLod4ImplicitRepresentation();
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
				psBridgeInstallation.setLong(17 + i, implicitId);
			else
				psBridgeInstallation.setNull(17 + i, Types.NULL);

			if (pointGeom != null)
				psBridgeInstallation.setObject(20 + i, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psBridgeInstallation.setNull(20 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psBridgeInstallation.setString(23 + i, matrixString);
			else
				psBridgeInstallation.setNull(23 + i, Types.VARCHAR);
		}

		psBridgeInstallation.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BRIDGE_INSTALLATION);

		// brid:boundedBy
		if (bridgeInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : bridgeInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, bridgeInstallation, bridgeInstallationId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_THEMATIC_SURFACE.getName(),
								href,
								bridgeInstallationId,
								"bridge_installation_id"));
					}
				}
			}
		}

		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(bridgeInstallation, bridgeInstallationId, featureType);
		
		return bridgeInstallationId;
	}

	public long doImport(IntBridgeInstallation intBridgeInstallation, AbstractCityObject parent, long parentId) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(intBridgeInstallation);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long intBridgeInstallationId = cityObjectImporter.doImport(intBridgeInstallation, featureType);

		// import interior bridge installation information
		// primary id
		psBridgeInstallation.setLong(1, intBridgeInstallationId);

		// objectclass id
		psBridgeInstallation.setLong(2, featureType.getObjectClassId());

		// brid:class
		if (intBridgeInstallation.isSetClazz() && intBridgeInstallation.getClazz().isSetValue()) {
			psBridgeInstallation.setString(3, intBridgeInstallation.getClazz().getValue());
			psBridgeInstallation.setString(4, intBridgeInstallation.getClazz().getCodeSpace());
		} else {
			psBridgeInstallation.setNull(3, Types.VARCHAR);
			psBridgeInstallation.setNull(4, Types.VARCHAR);
		}

		// brid:function
		if (intBridgeInstallation.isSetFunction()) {
			valueJoiner.join(intBridgeInstallation.getFunction(), Code::getValue, Code::getCodeSpace);
			psBridgeInstallation.setString(5, valueJoiner.result(0));
			psBridgeInstallation.setString(6, valueJoiner.result(1));
		} else {
			psBridgeInstallation.setNull(5, Types.VARCHAR);
			psBridgeInstallation.setNull(6, Types.VARCHAR);
		}

		// brid:usage
		if (intBridgeInstallation.isSetUsage()) {
			valueJoiner.join(intBridgeInstallation.getUsage(), Code::getValue, Code::getCodeSpace);
			psBridgeInstallation.setString(7, valueJoiner.result(0));
			psBridgeInstallation.setString(8, valueJoiner.result(1));
		} else {
			psBridgeInstallation.setNull(7, Types.VARCHAR);
			psBridgeInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		if (parent instanceof AbstractBridge) {
			psBridgeInstallation.setLong(9, parentId);
			psBridgeInstallation.setNull(10, Types.NULL);
		} else if (parent instanceof BridgeRoom) {
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setLong(10, parentId);
		} else {
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setNull(10, Types.NULL);
		}	

		// brid:lod4Geometry
		psBridgeInstallation.setNull(11, Types.NULL);
		psBridgeInstallation.setNull(12, Types.NULL);
		psBridgeInstallation.setNull(14, nullGeometryType, nullGeometryTypeName);
		psBridgeInstallation.setNull(15, nullGeometryType, nullGeometryTypeName);

		long geometryId = 0;
		GeometryObject geometryObject = null;

		if (intBridgeInstallation.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = intBridgeInstallation.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (importer.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.doImport(abstractGeometry, intBridgeInstallationId);
				else if (importer.isPointOrLineGeometry(abstractGeometry))
					geometryObject = geometryConverter.getPointOrCurveGeometry(abstractGeometry);
				else 
					importer.logOrThrowUnsupportedGeometryMessage(intBridgeInstallation, abstractGeometry);

				geometryProperty.unsetGeometry();
			} else {
				String href = geometryProperty.getHref();
				if (href != null && href.length() != 0) {
					importer.propagateXlink(new DBXlinkSurfaceGeometry(
							TableEnum.BRIDGE_INSTALLATION.getName(),
							intBridgeInstallationId, 
							href, 
							"lod4_brep_id"));
				}
			}
		}

		if (geometryId != 0)
			psBridgeInstallation.setLong(13, geometryId);
		else
			psBridgeInstallation.setNull(13, Types.NULL);

		if (geometryObject != null)
			psBridgeInstallation.setObject(16, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBridgeInstallation.setNull(16, nullGeometryType, nullGeometryTypeName);

		// brid:lod4ImplicitRepresentation
		psBridgeInstallation.setNull(17, Types.NULL);
		psBridgeInstallation.setNull(18, Types.NULL);
		psBridgeInstallation.setNull(20, nullGeometryType, nullGeometryTypeName);
		psBridgeInstallation.setNull(21, nullGeometryType, nullGeometryTypeName);
		psBridgeInstallation.setNull(23, Types.VARCHAR);
		psBridgeInstallation.setNull(24, Types.VARCHAR);

		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (intBridgeInstallation.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = intBridgeInstallation.getLod4ImplicitRepresentation();

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
			psBridgeInstallation.setLong(19, implicitId);
		else
			psBridgeInstallation.setNull(19, Types.NULL);

		if (pointGeom != null)
			psBridgeInstallation.setObject(22, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBridgeInstallation.setNull(22, nullGeometryType, nullGeometryTypeName);

		if (matrixString != null)
			psBridgeInstallation.setString(25, matrixString);
		else
			psBridgeInstallation.setNull(25, Types.VARCHAR);

		psBridgeInstallation.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.BRIDGE_INSTALLATION);

		// brid:boundedBy
		if (intBridgeInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty property : intBridgeInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

				if (boundarySurface != null) {
					thematicSurfaceImporter.doImport(boundarySurface, intBridgeInstallation, intBridgeInstallationId);
					property.unsetBoundarySurface();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.BRIDGE_THEMATIC_SURFACE.getName(),
								href,
								intBridgeInstallationId,
								"bridge_installation_id"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(intBridgeInstallation, intBridgeInstallationId, featureType);

		return intBridgeInstallationId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psBridgeInstallation.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psBridgeInstallation.close();
	}

}
