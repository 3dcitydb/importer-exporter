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
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBridgeInstallation implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBridgeInstallation;
	private DBCityObject cityObjectImporter;
	private DBBridgeThematicSurface thematicSurfaceImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBBridgeInstallation(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into BRIDGE_INSTALLATION (ID, OBJECTCLASS_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, BRIDGE_ID, BRIDGE_ROOM_ID, ")
		.append("LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
		.append("LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psBridgeInstallation = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBBridgeThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_THEMATIC_SURFACE);
	}

	public long insert(BridgeInstallation bridgeInstallation, CityGMLClass parent, long parentId) throws SQLException {
		long bridgeInstallationId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (bridgeInstallationId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(bridgeInstallation, bridgeInstallationId);

		// BridgeInstallation
		// ID
		psBridgeInstallation.setLong(1, bridgeInstallationId);

		// OBJECTCLASS_ID
		psBridgeInstallation.setLong(2, Util.cityObject2classId(bridgeInstallation.getCityGMLClass()));

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
			String[] function = Util.codeList2string(bridgeInstallation.getFunction());
			psBridgeInstallation.setString(5, function[0]);
			psBridgeInstallation.setString(6, function[1]);
		} else {
			psBridgeInstallation.setNull(5, Types.VARCHAR);
			psBridgeInstallation.setNull(6, Types.VARCHAR);
		}

		// brid:usage
		if (bridgeInstallation.isSetUsage()) {
			String[] usage = Util.codeList2string(bridgeInstallation.getUsage());
			psBridgeInstallation.setString(7, usage[0]);
			psBridgeInstallation.setString(8, usage[1]);
		} else {
			psBridgeInstallation.setNull(7, Types.VARCHAR);
			psBridgeInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		switch (parent) {
		case BRIDGE:
		case BRIDGE_PART:
			psBridgeInstallation.setLong(9, parentId);
			psBridgeInstallation.setNull(10, Types.NULL);
			break;
		case BRIDGE_ROOM:
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setLong(10, parentId);
			break;
		default:
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setNull(10, Types.NULL);
		}

		// Geometry
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
					if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.insert(abstractGeometry, bridgeInstallationId);
					else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeInstallation.getCityGMLClass(), 
								bridgeInstallation.getId()));
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
								bridgeInstallationId, 
								TableEnum.BRIDGE_INSTALLATION, 
								"LOD" + (i + 2) + "_BREP_ID"));
					}
				}
			}

			if (geometryId != 0)
				psBridgeInstallation.setLong(11 + i, geometryId);
			else
				psBridgeInstallation.setNull(11 + i, Types.NULL);

			if (geometryObject != null)
				psBridgeInstallation.setObject(14 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psBridgeInstallation.setNull(14 + i, nullGeometryType, nullGeometryTypeName);
		}

		// implicit geometry
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
						pointGeom = otherGeometryImporter.getPoint(geometry.getReferencePoint());

					// transformation matrix
					if (geometry.isSetTransformationMatrix()) {
						Matrix matrix = geometry.getTransformationMatrix().getMatrix();
						if (affineTransformation)
							matrix = dbImporterManager.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

						matrixString = Util.collection2string(matrix.toRowPackedList(), " ");
					}

					// reference to IMPLICIT_GEOMETRY
					implicitId = implicitGeometryImporter.insert(geometry, bridgeInstallationId);
				}
			}

			if (implicitId != 0)
				psBridgeInstallation.setLong(17 + i, implicitId);
			else
				psBridgeInstallation.setNull(17 + i, Types.NULL);

			if (pointGeom != null)
				psBridgeInstallation.setObject(20 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psBridgeInstallation.setNull(20 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psBridgeInstallation.setString(23 + i, matrixString);
			else
				psBridgeInstallation.setNull(23 + i, Types.VARCHAR);
		}

		psBridgeInstallation.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE_INSTALLATION);

		// BoundarySurfaces
		if (bridgeInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : bridgeInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, bridgeInstallation.getCityGMLClass(), bridgeInstallationId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeInstallation.getCityGMLClass(), 
								bridgeInstallation.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					boundarySurfaceProperty.unsetBoundarySurface();
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BRIDGE_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}
		
		// insert local appearance
		cityObjectImporter.insertAppearance(bridgeInstallation, bridgeInstallationId);

		return bridgeInstallationId;
	}

	public long insert(IntBridgeInstallation intBridgeInstallation, CityGMLClass parent, long parentId) throws SQLException {
		long intBridgeInstallationId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (intBridgeInstallationId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(intBridgeInstallation, intBridgeInstallationId);

		// IntBridgeInstallation
		// ID
		psBridgeInstallation.setLong(1, intBridgeInstallationId);

		// OBJECTCLASS_ID
		psBridgeInstallation.setLong(2, Util.cityObject2classId(intBridgeInstallation.getCityGMLClass()));

		// class
		if (intBridgeInstallation.isSetClazz() && intBridgeInstallation.getClazz().isSetValue()) {
			psBridgeInstallation.setString(3, intBridgeInstallation.getClazz().getValue());
			psBridgeInstallation.setString(4, intBridgeInstallation.getClazz().getCodeSpace());
		} else {
			psBridgeInstallation.setNull(3, Types.VARCHAR);
			psBridgeInstallation.setNull(4, Types.VARCHAR);
		}

		// function
		if (intBridgeInstallation.isSetFunction()) {
			String[] function = Util.codeList2string(intBridgeInstallation.getFunction());
			psBridgeInstallation.setString(5, function[0]);
			psBridgeInstallation.setString(6, function[1]);
		} else {
			psBridgeInstallation.setNull(5, Types.VARCHAR);
			psBridgeInstallation.setNull(6, Types.VARCHAR);
		}

		// usage
		if (intBridgeInstallation.isSetUsage()) {
			String[] usage = Util.codeList2string(intBridgeInstallation.getUsage());
			psBridgeInstallation.setString(7, usage[0]);
			psBridgeInstallation.setString(8, usage[1]);
		} else {
			psBridgeInstallation.setNull(7, Types.VARCHAR);
			psBridgeInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		switch (parent) {
		case BRIDGE:
		case BRIDGE_PART:
			psBridgeInstallation.setLong(9, parentId);
			psBridgeInstallation.setNull(10, Types.NULL);
			break;
		case BRIDGE_ROOM:
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setLong(10, parentId);
			break;
		default:
			psBridgeInstallation.setNull(9, Types.NULL);
			psBridgeInstallation.setNull(10, Types.NULL);
		}	

		// Geometry
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
				if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.insert(abstractGeometry, intBridgeInstallationId);
				else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
					geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
				else {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							intBridgeInstallation.getCityGMLClass(), 
							intBridgeInstallation.getId()));
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
							intBridgeInstallationId, 
							TableEnum.BRIDGE_INSTALLATION, 
							"LOD4_BREP_ID"));
				}
			}
		}

		if (geometryId != 0)
			psBridgeInstallation.setLong(13, geometryId);
		else
			psBridgeInstallation.setNull(13, Types.NULL);

		if (geometryObject != null)
			psBridgeInstallation.setObject(16, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBridgeInstallation.setNull(16, nullGeometryType, nullGeometryTypeName);

		// implicit geometry
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
					pointGeom = otherGeometryImporter.getPoint(geometry.getReferencePoint());

				// transformation matrix
				if (geometry.isSetTransformationMatrix()) {
					Matrix matrix = geometry.getTransformationMatrix().getMatrix();
					if (affineTransformation)
						matrix = dbImporterManager.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

					matrixString = Util.collection2string(matrix.toRowPackedList(), " ");
				}

				// reference to IMPLICIT_GEOMETRY
				implicitId = implicitGeometryImporter.insert(geometry, intBridgeInstallationId);
			}
		}

		if (implicitId != 0)
			psBridgeInstallation.setLong(19, implicitId);
		else
			psBridgeInstallation.setNull(19, Types.NULL);

		if (pointGeom != null)
			psBridgeInstallation.setObject(22, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBridgeInstallation.setNull(22, nullGeometryType, nullGeometryTypeName);

		if (matrixString != null)
			psBridgeInstallation.setString(25, matrixString);
		else
			psBridgeInstallation.setNull(25, Types.VARCHAR);

		psBridgeInstallation.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE_INSTALLATION);

		// BoundarySurfaces
		if (intBridgeInstallation.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : intBridgeInstallation.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, intBridgeInstallation.getCityGMLClass(), intBridgeInstallationId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								intBridgeInstallation.getCityGMLClass(), 
								intBridgeInstallation.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								boundarySurface.getCityGMLClass(), 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					boundarySurfaceProperty.unsetBoundarySurface();
				} else {
					// xlink
					String href = boundarySurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.ABSTRACT_BRIDGE_BOUNDARY_SURFACE + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(intBridgeInstallation, intBridgeInstallationId);

		return intBridgeInstallationId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBridgeInstallation.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBridgeInstallation.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BRIDGE_INSTALLATION;
	}

}
