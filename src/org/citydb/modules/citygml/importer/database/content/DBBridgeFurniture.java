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
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBBridgeFurniture implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBridgeFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBOtherGeometry geometryImporter;

	private boolean affineTransformation;
	private int batchCounter;

	public DBBridgeFurniture(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into BRIDGE_FURNITURE (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, BRIDGE_ROOM_ID, ")
		.append("LOD4_BREP_ID, LOD4_OTHER_GEOM, ")
		.append("LOD4_IMPLICIT_REP_ID, LOD4_IMPLICIT_REF_POINT, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psBridgeFurniture = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		geometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(BridgeFurniture bridgeFurniture, long roomId) throws SQLException {
		long bridgeFurnitureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (bridgeFurnitureId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(bridgeFurniture, bridgeFurnitureId);

		// BridgeFurniture
		// ID
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
			String[] function = Util.codeList2string(bridgeFurniture.getFunction());
			psBridgeFurniture.setString(4, function[0]);
			psBridgeFurniture.setString(5, function[1]);
		} else {
			psBridgeFurniture.setNull(4, Types.VARCHAR);
			psBridgeFurniture.setNull(5, Types.VARCHAR);
		}

		// brid:usage
		if (bridgeFurniture.isSetUsage()) {
			String[] usage = Util.codeList2string(bridgeFurniture.getUsage());
			psBridgeFurniture.setString(6, usage[0]);
			psBridgeFurniture.setString(7, usage[1]);
		} else {
			psBridgeFurniture.setNull(6, Types.VARCHAR);
			psBridgeFurniture.setNull(7, Types.VARCHAR);
		}

		// BRIDGE_ROOM_ID
		psBridgeFurniture.setLong(8, roomId);

		// Geometry		
		long geometryId = 0;
		GeometryObject geometryObject = null;
		
		if (bridgeFurniture.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = bridgeFurniture.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.insert(abstractGeometry, bridgeFurnitureId);
				else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
					geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
				else {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							bridgeFurniture.getCityGMLClass(), 
							bridgeFurniture.getId()));
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
							bridgeFurnitureId, 
							TableEnum.BRIDGE_FURNITURE, 
							"LOD4_BREP_ID"));
				}
			}
		}

		if (geometryId != 0)
			psBridgeFurniture.setLong(9, geometryId);
		else
			psBridgeFurniture.setNull(9, Types.NULL);

		if (geometryObject != null)
			psBridgeFurniture.setObject(10, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psBridgeFurniture.setNull(10, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// implicit geometry
		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (bridgeFurniture.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = bridgeFurniture.getLod4ImplicitRepresentation();

			if (implicit.isSetObject()) {
				ImplicitGeometry geometry = implicit.getObject();

				// reference Point
				if (geometry.isSetReferencePoint())
					pointGeom = geometryImporter.getPoint(geometry.getReferencePoint());

				// transformation matrix
				if (geometry.isSetTransformationMatrix()) {
					Matrix matrix = geometry.getTransformationMatrix().getMatrix();
					if (affineTransformation)
						matrix = dbImporterManager.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);

					matrixString = Util.collection2string(matrix.toRowPackedList(), " ");
				}

				// reference to IMPLICIT_GEOMETRY
				implicitId = implicitGeometryImporter.insert(geometry, bridgeFurnitureId);
			}
		}

		if (implicitId != 0)
			psBridgeFurniture.setLong(11, implicitId);
		else
			psBridgeFurniture.setNull(11, Types.NULL);

		if (pointGeom != null)
			psBridgeFurniture.setObject(12, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psBridgeFurniture.setNull(12, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		if (matrixString != null)
			psBridgeFurniture.setString(13, matrixString);
		else
			psBridgeFurniture.setNull(13, Types.VARCHAR);

		psBridgeFurniture.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.BRIDGE_FURNITURE);

		// insert local appearance
		cityObjectImporter.insertAppearance(bridgeFurniture, bridgeFurnitureId);

		return bridgeFurnitureId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBridgeFurniture.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBridgeFurniture.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BRIDGE_FURNITURE;
	}

}
