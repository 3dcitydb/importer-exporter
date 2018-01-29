/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.exporter.database.content;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.citydb.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;

public class DBImplicitGeometry implements DBExporter {
	private final CityGMLExportManager exporter;

	private PreparedStatement ps;

	private DBSurfaceGeometry geometryExporter;
	private GMLConverter gmlConverter;
	private MessageDigest md5;
	private AttributeValueSplitter valueSplitter;

	public DBImplicitGeometry(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		this.exporter = exporter;

		String getLength = exporter.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("blob.get_length");
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CityGMLExportException(e);
		}
		
		Table table = new Table(TableEnum.IMPLICIT_GEOMETRY.getName(), schema);
		Select select = new Select().addProjection(table.getColumn("id"), table.getColumn("mime_type"), table.getColumn("reference_to_library"),
				new Function(getLength, "db_library_object_length", table.getColumn("library_object")),
				table.getColumn("relative_brep_id"), table.getColumn("relative_other_geom"))
				.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
		ps = connection.prepareStatement(select.toString());
		
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected ImplicitGeometry doExport(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);
		
		try (ResultSet rs = ps.executeQuery()) {		
			ImplicitGeometry implicit = new ImplicitGeometry();
			boolean isValid = false;

			if (rs.next()) {
				// library object
				String blobURI = rs.getString(3);
				if (!rs.wasNull()) {
					isValid = true;

					long dbBlobSize = rs.getLong(4);
					if (dbBlobSize > 0) {
						File file = new File(blobURI);
						implicit.setLibraryObject(file.getName());

						exporter.propagateXlink(new DBXlinkLibraryObject(
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

					SurfaceGeometry geometry = geometryExporter.doExportImplicitGeometry(surfaceGeometryId);
					if (geometry != null) {
						GeometryProperty<AbstractGeometry> geometryProperty = new GeometryProperty<AbstractGeometry>();
						if (geometry.isSetGeometry())
							geometryProperty.setGeometry(geometry.getGeometry());
						else
							geometryProperty.setHref(geometry.getReference());

						implicit.setRelativeGeometry(geometryProperty);
					} else
						isValid = false;

				} else if (otherGeomObj != null) {
					isValid = true;

					long implicitId = rs.getLong(1);
					String uuid = toHexString(md5.digest(String.valueOf(implicitId).getBytes()));

					if (exporter.lookupAndPutObjectUID(uuid, implicitId, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID)) {
						implicit.setRelativeGeometry(new GeometryProperty<AbstractGeometry>("#UUID_" + uuid));
					} else {
						GeometryObject otherGeom = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(otherGeomObj);
						AbstractGeometry geometry = gmlConverter.getPointOrCurveGeometry(otherGeom, true);
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
			if (referencePoint != null)
				implicit.setReferencePoint(gmlConverter.getPointProperty(referencePoint, false));

			// transformationMatrix
			if (transformationMatrix != null) {
				List<Double> m = valueSplitter.splitDoubleList(transformationMatrix);
				if (m.size() >= 16) {
					Matrix matrix = new Matrix(4, 4);
					matrix.setMatrix(m.subList(0, 16));
					implicit.setTransformationMatrix(new TransformationMatrix4x4(matrix));
				}
			}

			return implicit;
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
		ps.close();
	}

}
