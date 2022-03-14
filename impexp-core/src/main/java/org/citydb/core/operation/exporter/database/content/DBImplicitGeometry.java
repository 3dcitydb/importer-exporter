/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.operation.common.xlink.DBXlinkLibraryObject;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.util.Util;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DBImplicitGeometry implements DBExporter {
	private final CityGMLExportManager exporter;
	private final PreparedStatement ps;
	private final DBSurfaceGeometry geometryExporter;
	private final GMLConverter gmlConverter;
	private final AttributeValueSplitter valueSplitter;
	private final boolean affineTransformation;
	private final boolean hasGmlIdColumn;

	public DBImplicitGeometry(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		this.exporter = exporter;

		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
		affineTransformation = exporter.getExportConfig().getAffineTransformation().isEnabled();
		hasGmlIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 3, 0) >= 0;
		String getLength = exporter.getDatabaseAdapter().getSQLAdapter().resolveDatabaseOperationName("blob.get_length");
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		Table table = new Table(TableEnum.IMPLICIT_GEOMETRY.getName(), schema);
		Select select = new Select().addProjection(table.getColumns("mime_type", "reference_to_library", "relative_brep_id", "relative_other_geom"))
				.addProjection(new Function(getLength, "db_library_object_length", table.getColumn("library_object")))
				.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));

		if (hasGmlIdColumn) {
			select.addProjection(table.getColumn("gmlid"));
		} else {
			Table surfaceGeometry = new Table(TableEnum.SURFACE_GEOMETRY.getName(), schema);
			select.addProjection(surfaceGeometry.getColumn("gmlid"))
					.addJoin(JoinFactory.left(surfaceGeometry, "id", ComparisonName.EQUAL_TO, table.getColumn("relative_brep_id")));
		}
		ps = connection.prepareStatement(select.toString());
	}

	protected ImplicitGeometry doExport(long id, GeometryObject referencePoint, String transformationMatrix) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {		
			ImplicitGeometry implicitGeometry = new ImplicitGeometry();
			boolean isValid = false;

			if (rs.next()) {
				// library object
				String libraryObject = rs.getString(2);
				if (!rs.wasNull()) {
					isValid = true;
					long dbBlobSize = rs.getLong(5);
					if (dbBlobSize > 0) {
						String extension = Util.getFileExtension(libraryObject);
						String fileName = CoreConstants.UNIQUE_LIBRARY_OBJECT_FILENAME_PREFIX + id +
								(!extension.isEmpty() ? "." + extension : "");
						implicitGeometry.setLibraryObject(CoreConstants.LIBRARY_OBJECTS_DIR + '/' + fileName);
						exporter.propagateXlink(new DBXlinkLibraryObject(id, fileName));
					} else {
						implicitGeometry.setLibraryObject(libraryObject);
					}

					String mimeType = rs.getString(1);
					if (!rs.wasNull()) {
						implicitGeometry.setMimeType(new Code(mimeType));
					}
				}

				// geometry
				long geometryId = rs.getLong(3);
				if (!rs.wasNull()) {
					isValid = true;
					String gmlId = rs.getString(6);
					if (exporter.lookupGeometryId(gmlId)) {
						implicitGeometry.setRelativeGeometry(new GeometryProperty<>("#" + gmlId));
					} else {
						geometryExporter.addImplicitGeometryBatch(geometryId, implicitGeometry);
					}
				} else {
					Object otherGeometry = rs.getObject(4);
					if (!rs.wasNull()) {
						isValid = true;
						String gmlId = hasGmlIdColumn ?
								rs.getString(6) :
								"ID_" + UUID.nameUUIDFromBytes(String.valueOf(id).getBytes());

						if (exporter.lookupAndPutObjectId(gmlId, id, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID)) {
							implicitGeometry.setRelativeGeometry(new GeometryProperty<>("#" + gmlId));
						} else {
							GeometryObject geometryObject = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(otherGeometry);
							AbstractGeometry geometry = gmlConverter.getPointOrCurveGeometry(geometryObject, true);
							if (geometry != null) {
								geometry.setId(gmlId);
								implicitGeometry.setRelativeGeometry(new GeometryProperty<>(geometry));
							} else {
								isValid = false;
							}
						}
					}
				}
			}

			if (!isValid) {
				return null;
			}

			// referencePoint
			if (referencePoint != null) {
				implicitGeometry.setReferencePoint(gmlConverter.getPointProperty(referencePoint, false));
			}

			// transformationMatrix
			if (transformationMatrix != null) {
				List<Double> values = valueSplitter.splitDoubleList(transformationMatrix);
				if (values.size() >= 16) {
					Matrix matrix = new Matrix(4, 4);
					matrix.setMatrix(values.subList(0, 16));

					if (affineTransformation) {
						matrix = exporter.getAffineTransformer().transformImplicitGeometryTransformationMatrix(matrix);
					}

					implicitGeometry.setTransformationMatrix(new TransformationMatrix4x4(matrix));
				}
			}

			return implicitGeometry;
		}
	}

	@Override
	public void close() throws SQLException {
		ps.close();
	}
}
