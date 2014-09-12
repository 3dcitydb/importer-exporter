/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
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
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;

public class DBBridgeConstrElement implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBridgeConstruction;
	private DBCityObject cityObjectImporter;
	private DBBridgeThematicSurface thematicSurfaceImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;

	private boolean affineTransformation;
	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBBridgeConstrElement(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into BRIDGE_CONSTR_ELEMENT (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, BRIDGE_ID, ")
		.append("LOD1_TERRAIN_INTERSECTION, LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION, ")
		.append("LOD1_BREP_ID, LOD2_BREP_ID, LOD3_BREP_ID, LOD4_BREP_ID, LOD1_OTHER_GEOM, LOD2_OTHER_GEOM, LOD3_OTHER_GEOM, LOD4_OTHER_GEOM, ")
		.append("LOD1_IMPLICIT_REP_ID, LOD2_IMPLICIT_REP_ID, LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD1_IMPLICIT_REF_POINT, LOD2_IMPLICIT_REF_POINT, LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD1_IMPLICIT_TRANSFORMATION, LOD2_IMPLICIT_TRANSFORMATION, LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psBridgeConstruction = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		thematicSurfaceImporter = (DBBridgeThematicSurface)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE_THEMATIC_SURFACE);
	}

	public long insert(BridgeConstructionElement bridgeConstruction, CityGMLClass parent, long parentId) throws SQLException {
		long bridgeConstructionId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (bridgeConstructionId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(bridgeConstruction, bridgeConstructionId);

		// BridgeConstructionElement
		// ID
		psBridgeConstruction.setLong(1, bridgeConstructionId);

		// brid:class
		if (bridgeConstruction.isSetClazz() && bridgeConstruction.getClazz().isSetValue()) {
			psBridgeConstruction.setString(2, bridgeConstruction.getClazz().getValue());
			psBridgeConstruction.setString(3, bridgeConstruction.getClazz().getCodeSpace());
		} else {
			psBridgeConstruction.setNull(2, Types.VARCHAR);
			psBridgeConstruction.setNull(3, Types.VARCHAR);
		}

		// brid:function
		if (bridgeConstruction.isSetFunction()) {
			String[] function = Util.codeList2string(bridgeConstruction.getFunction());
			psBridgeConstruction.setString(4, function[0]);
			psBridgeConstruction.setString(5, function[1]);
		} else {
			psBridgeConstruction.setNull(4, Types.VARCHAR);
			psBridgeConstruction.setNull(5, Types.VARCHAR);
		}

		// brid:usage
		if (bridgeConstruction.isSetUsage()) {
			String[] usage = Util.codeList2string(bridgeConstruction.getUsage());
			psBridgeConstruction.setString(6, usage[0]);
			psBridgeConstruction.setString(7, usage[1]);
		} else {
			psBridgeConstruction.setNull(6, Types.VARCHAR);
			psBridgeConstruction.setNull(7, Types.VARCHAR);
		}

		// parentId
		psBridgeConstruction.setLong(8, parentId);

		// Geometry
		// lodXTerrainIntersectionCurve
		for (int i = 0; i < 4; i++) {
			MultiCurveProperty multiCurveProperty = null;
			GeometryObject multiLine = null;

			switch (i) {
			case 0:
				multiCurveProperty = bridgeConstruction.getLod1TerrainIntersection();
				break;
			case 1:
				multiCurveProperty = bridgeConstruction.getLod2TerrainIntersection();
				break;
			case 2:
				multiCurveProperty = bridgeConstruction.getLod3TerrainIntersection();
				break;
			case 3:
				multiCurveProperty = bridgeConstruction.getLod4TerrainIntersection();
				break;
			}

			if (multiCurveProperty != null) {
				multiLine = otherGeometryImporter.getMultiCurve(multiCurveProperty);
				multiCurveProperty.unsetMultiCurve();
			}

			if (multiLine != null) {
				Object multiLineObj = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiLine, batchConn);
				psBridgeConstruction.setObject(9 + i, multiLineObj);
			} else
				psBridgeConstruction.setNull(9 + i, nullGeometryType, nullGeometryTypeName);
		}

		// lodXgeometry
		for (int i = 0; i < 4; i++) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = null;
			long geometryId = 0;
			GeometryObject geometryObject = null;

			switch (i) {
			case 0:
				geometryProperty = bridgeConstruction.getLod1Geometry();
				break;
			case 1:
				geometryProperty = bridgeConstruction.getLod2Geometry();
				break;
			case 2:
				geometryProperty = bridgeConstruction.getLod3Geometry();
				break;
			case 3:
				geometryProperty = bridgeConstruction.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.isSetGeometry()) {
					AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
					if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
						geometryId = surfaceGeometryImporter.insert(abstractGeometry, bridgeConstructionId);
					else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
						geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
					else {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeConstruction.getCityGMLClass(), 
								bridgeConstruction.getId()));
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
								bridgeConstructionId, 
								TableEnum.BRIDGE_CONSTR_ELEMENT, 
								"LOD" + (i + 1) + "_BREP_ID"));
					}
				}
			}

			if (geometryId != 0)
				psBridgeConstruction.setLong(13 + i, geometryId);
			else
				psBridgeConstruction.setNull(13 + i, Types.NULL);

			if (geometryObject != null)
				psBridgeConstruction.setObject(17 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
			else
				psBridgeConstruction.setNull(17 + i, nullGeometryType, nullGeometryTypeName);
		}

		// implicit geometry
		for (int i = 0; i < 4; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = bridgeConstruction.getLod1ImplicitRepresentation();
				break;
			case 1:
				implicit = bridgeConstruction.getLod2ImplicitRepresentation();
				break;
			case 2:
				implicit = bridgeConstruction.getLod3ImplicitRepresentation();
				break;
			case 3:
				implicit = bridgeConstruction.getLod4ImplicitRepresentation();
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
					implicitId = implicitGeometryImporter.insert(geometry, bridgeConstructionId);
				}
			}

			if (implicitId != 0)
				psBridgeConstruction.setLong(21 + i, implicitId);
			else
				psBridgeConstruction.setNull(21 + i, Types.NULL);

			if (pointGeom != null)
				psBridgeConstruction.setObject(25 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psBridgeConstruction.setNull(25 + i, nullGeometryType, nullGeometryTypeName);

			if (matrixString != null)
				psBridgeConstruction.setString(29 + i, matrixString);
			else
				psBridgeConstruction.setNull(29 + i, Types.VARCHAR);
		}

		psBridgeConstruction.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE_CONSTR_ELEMENT);

		// BoundarySurfaces
		if (bridgeConstruction.isSetBoundedBySurface()) {
			for (BoundarySurfaceProperty boundarySurfaceProperty : bridgeConstruction.getBoundedBySurface()) {
				AbstractBoundarySurface boundarySurface = boundarySurfaceProperty.getBoundarySurface();

				if (boundarySurface != null) {
					String gmlId = boundarySurface.getId();
					long id = thematicSurfaceImporter.insert(boundarySurface, bridgeConstruction.getCityGMLClass(), bridgeConstructionId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								bridgeConstruction.getCityGMLClass(), 
								bridgeConstruction.getId()));
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
		cityObjectImporter.insertAppearance(bridgeConstruction, bridgeConstructionId);

		return bridgeConstructionId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBridgeConstruction.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBridgeConstruction.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BRIDGE_CONSTR_ELEMENT;
	}

}
