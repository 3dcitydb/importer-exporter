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
package org.citydb.modules.citygml.exporter.database.content;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.util.Util;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;

public class DBImplicitGeometry implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Connection connection;

	private PreparedStatement psImplicitGeometry;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBOtherGeometry geometryExporter;
	private MessageDigest md5;

	public DBImplicitGeometry(Connection connection, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		String getLength = dbExporterManager.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("blob.get_length");

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new SQLException(e);
		}

		StringBuilder query = new StringBuilder("select ID, MIME_TYPE, REFERENCE_TO_LIBRARY, ")
		.append(getLength).append("(LIBRARY_OBJECT) as DB_LIBRARY_OBJECT_LENGTH, ")
		.append("RELATIVE_BREP_ID, RELATIVE_OTHER_GEOM from IMPLICIT_GEOMETRY where ID=?");

		psImplicitGeometry = connection.prepareStatement(query.toString());
		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		geometryExporter = (DBOtherGeometry)dbExporterManager.getDBExporter(DBExporterEnum.OTHER_GEOMETRY);
	}

	public ImplicitGeometry read(long id, GeometryObject referencePoint, String transformationMatrix) throws SQLException {
		ResultSet rs = null;

		try {		
			psImplicitGeometry.setLong(1, id);
			rs = psImplicitGeometry.executeQuery();

			// ImplicitGeometry
			ImplicitGeometry implicit = new ImplicitGeometry();
			boolean isValid = false;

			if (rs.next()) {
				// library object
				String blobURI = rs.getString(3);
				long dbBlobSize = rs.getLong(4);
				if (blobURI != null) {
					// export library object from database
					isValid = true;
					if (dbBlobSize > 0) {
						File file = new File(blobURI);
						implicit.setLibraryObject(file.getName());

						dbExporterManager.propagateXlink(new DBXlinkLibraryObject(
								id,
								file.getName()));
					} else
						implicit.setLibraryObject(blobURI);

					implicit.setMimeType(new Code(rs.getString(2)));
				}

				// geometry
				long surfaceGeometryId = rs.getLong(5);
				Object otherGeomObj = rs.getObject(6);

				if (surfaceGeometryId != 0) {
					isValid = true;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.readImplicitGeometry(surfaceGeometryId);
					if (geometry != null) {
						GeometryProperty<AbstractGeometry> geometryProperty = new GeometryProperty<AbstractGeometry>();
						if (geometry.getAbstractGeometry() != null)
							geometryProperty.setGeometry(geometry.getAbstractGeometry());
						else
							geometryProperty.setHref(geometry.getTarget());

						implicit.setRelativeGeometry(geometryProperty);
					} else
						isValid = false;
					
				} else if (otherGeomObj != null) {
					isValid = true;

					long implicitId = rs.getLong(1);
					String uuid = toHexString(md5.digest(String.valueOf(implicitId).getBytes()));

					if (dbExporterManager.lookupAndPutGmlId(uuid, implicitId, CityGMLClass.IMPLICIT_GEOMETRY)) {
						implicit.setRelativeGeometry(new GeometryProperty<AbstractGeometry>("#UUID_" + uuid));
					} else {
						GeometryObject otherGeom = dbExporterManager.getDatabaseAdapter().getGeometryConverter().getGeometry(otherGeomObj);
						AbstractGeometry geometry = geometryExporter.getPointOrCurveGeometry(otherGeom, true);
						if (geometry != null) {
							geometry.setId("UUID_" + uuid);
							implicit.setRelativeGeometry(new GeometryProperty<AbstractGeometry>(geometry));
						} else
							isValid = false;
					}
				}
			}

			if (!isValid)
				return null;

			// referencePoint
			if (referencePoint != null) {
				PointProperty pointProperty = geometryExporter.getPointProperty(referencePoint, false);
				if (pointProperty != null)
					implicit.setReferencePoint(pointProperty);
			}

			// transformationMatrix
			if (transformationMatrix != null) {
				List<Double> m = Util.string2double(transformationMatrix, "\\s+");
				if (m != null && m.size() >= 16) {
					Matrix matrix = new Matrix(4, 4);
					matrix.setMatrix(m.subList(0, 16));
					implicit.setTransformationMatrix(new TransformationMatrix4x4(matrix));
				}
			}

			if (isValid) {
				dbExporterManager.updateFeatureCounter(CityGMLClass.IMPLICIT_GEOMETRY);
				return implicit;
			}

			return null;
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	private String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
			hexString.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));

		return hexString.toString();
	}

	@Override
	public void close() throws SQLException {
		psImplicitGeometry.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.IMPLICIT_GEOMETRY;
	}

}
