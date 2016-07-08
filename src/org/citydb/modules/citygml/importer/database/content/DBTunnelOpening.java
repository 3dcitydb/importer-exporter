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
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.AbstractOpening;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;

public class DBTunnelOpening implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psOpening;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBOtherGeometry otherGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBTunnelOpenToThemSrf openingToThemSurfaceImporter;

	private boolean affineTransformation;
	private int batchCounter;

	public DBTunnelOpening(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into TUNNEL_OPENING (ID, OBJECTCLASS_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, ")
		.append("LOD3_IMPLICIT_REP_ID, LOD4_IMPLICIT_REP_ID, ")
		.append("LOD3_IMPLICIT_REF_POINT, LOD4_IMPLICIT_REF_POINT, ")
		.append("LOD3_IMPLICIT_TRANSFORMATION, LOD4_IMPLICIT_TRANSFORMATION) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psOpening = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		openingToThemSurfaceImporter = (DBTunnelOpenToThemSrf)dbImporterManager.getDBImporter(DBImporterEnum.TUNNEL_OPEN_TO_THEM_SRF);
	}

	public long insert(AbstractOpening opening, long parentId) throws SQLException {
		long openingId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
		if (openingId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(opening, openingId);

		// Opening
		// ID
		psOpening.setLong(1, openingId);

		// OBJECTCLASS_ID
		psOpening.setInt(2, Util.cityObject2classId(opening.getCityGMLClass()));

		// Geometry
		for (int i = 0; i < 2; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiSurfaceId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = opening.getLod3MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = opening.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), openingId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								openingId, 
								TableEnum.TUNNEL_OPENING, 
								"LOD" + (i + 3) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiSurfaceId != 0)
				psOpening.setLong(3 + i, multiSurfaceId);
			else
				psOpening.setNull(3 + i, Types.NULL);
		}

		// implicit geometry
		for (int i = 0; i < 2; i++) {
			ImplicitRepresentationProperty implicit = null;
			GeometryObject pointGeom = null;
			String matrixString = null;
			long implicitId = 0;

			switch (i) {
			case 0:
				implicit = opening.getLod3ImplicitRepresentation();
				break;
			case 1:
				implicit = opening.getLod4ImplicitRepresentation();
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
					implicitId = implicitGeometryImporter.insert(geometry, openingId);
				}
			}

			if (implicitId != 0)
				psOpening.setLong(5 + i, implicitId);
			else
				psOpening.setNull(5 + i, Types.NULL);

			if (pointGeom != null)
				psOpening.setObject(7 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(pointGeom, batchConn));
			else
				psOpening.setNull(7 + i, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
						dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());

			if (matrixString != null)
				psOpening.setString(9 + i, matrixString);
			else
				psOpening.setNull(9 + i, Types.VARCHAR);
		}

		psOpening.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TUNNEL_OPENING);

		openingToThemSurfaceImporter.insert(openingId, parentId);

		// insert local appearance
		cityObjectImporter.insertAppearance(opening, openingId);

		return openingId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psOpening.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psOpening.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TUNNEL_OPENING;
	}

}
