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
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

public class DBTunnelFurniture implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTunnelFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBOtherGeometry geometryImporter;

	private boolean affineTransformation;
	private int batchCounter;

	public DBTunnelFurniture(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into TUNNEL_FURNITURE (ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, TUNNEL_HOLLOW_SPACE_ID, ")
		.append("LOD4_BREP_ID, LOD4_OTHER_GEOM, ")
		.append("LOD4_IMPLICIT_REP_ID, LOD4_IMPLICIT_REF_POINT, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psTunnelFurniture = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		geometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(TunnelFurniture tunnelFurniture, long roomId) throws SQLException {
		long tunnelFurnitureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (tunnelFurnitureId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(tunnelFurniture, tunnelFurnitureId);

		// TunnelFurniture
		// ID
		psTunnelFurniture.setLong(1, tunnelFurnitureId);

		// tun:class
		if (tunnelFurniture.isSetClazz() && tunnelFurniture.getClazz().isSetValue()) {
			psTunnelFurniture.setString(2, tunnelFurniture.getClazz().getValue());
			psTunnelFurniture.setString(3, tunnelFurniture.getClazz().getCodeSpace());
		} else {
			psTunnelFurniture.setNull(2, Types.VARCHAR);
			psTunnelFurniture.setNull(3, Types.VARCHAR);
		}

		// tun:function
		if (tunnelFurniture.isSetFunction()) {
			String[] function = Util.codeList2string(tunnelFurniture.getFunction());
			psTunnelFurniture.setString(4, function[0]);
			psTunnelFurniture.setString(5, function[1]);
		} else {
			psTunnelFurniture.setNull(4, Types.VARCHAR);
			psTunnelFurniture.setNull(5, Types.VARCHAR);
		}

		// tun:usage
		if (tunnelFurniture.isSetUsage()) {
			String[] usage = Util.codeList2string(tunnelFurniture.getUsage());
			psTunnelFurniture.setString(6, usage[0]);
			psTunnelFurniture.setString(7, usage[1]);
		} else {
			psTunnelFurniture.setNull(6, Types.VARCHAR);
			psTunnelFurniture.setNull(7, Types.VARCHAR);
		}

		// TUNNEL_HOLLOW_SPACE_ID
		psTunnelFurniture.setLong(8, roomId);

		// Geometry		
		long geometryId = 0;
		GeometryObject geometryObject = null;
		
		if (tunnelFurniture.isSetLod4Geometry()) {
			GeometryProperty<? extends AbstractGeometry> geometryProperty = tunnelFurniture.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
				AbstractGeometry abstractGeometry = geometryProperty.getGeometry();
				if (surfaceGeometryImporter.isSurfaceGeometry(abstractGeometry))
					geometryId = surfaceGeometryImporter.insert(abstractGeometry, tunnelFurnitureId);
				else if (otherGeometryImporter.isPointOrLineGeometry(abstractGeometry))
					geometryObject = otherGeometryImporter.getPointOrCurveGeometry(abstractGeometry);
				else {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							tunnelFurniture.getCityGMLClass(), 
							tunnelFurniture.getId()));
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
							tunnelFurnitureId, 
							TableEnum.TUNNEL_FURNITURE, 
							"LOD4_BREP_ID"));
				}
			}
		}

		if (geometryId != 0)
			psTunnelFurniture.setLong(9, geometryId);
		else
			psTunnelFurniture.setNull(9, Types.NULL);

		if (geometryObject != null)
			psTunnelFurniture.setObject(10, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(geometryObject, batchConn));
		else
			psTunnelFurniture.setNull(10, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		// implicit geometry
		GeometryObject pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (tunnelFurniture.isSetLod4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = tunnelFurniture.getLod4ImplicitRepresentation();

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
				implicitId = implicitGeometryImporter.insert(geometry, tunnelFurnitureId);
			}
		}

		if (implicitId != 0)
			psTunnelFurniture.setLong(11, implicitId);
		else
			psTunnelFurniture.setNull(11, Types.NULL);

		if (pointGeom != null)
			psTunnelFurniture.setObject(12, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
		else
			psTunnelFurniture.setNull(12, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

		if (matrixString != null)
			psTunnelFurniture.setString(13, matrixString);
		else
			psTunnelFurniture.setNull(13, Types.VARCHAR);

		psTunnelFurniture.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TUNNEL_FURNITURE);

		// insert local appearance
		cityObjectImporter.insertAppearance(tunnelFurniture, tunnelFurnitureId);

		return tunnelFurnitureId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTunnelFurniture.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTunnelFurniture.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TUNNEL_FURNITURE;
	}

}
