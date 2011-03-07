package de.tub.citydb.db.exporter;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometryPropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.GeometryProperty;
import org.citygml4j.model.gml.PointProperty;

import de.tub.citydb.db.xlink.DBXlinkLibraryObject;
import de.tub.citydb.util.Util;

public class DBImplicitGeometry implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Connection connection;

	private PreparedStatement psImplicitGeometry;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBSdoGeometry sdoGeometry;

	public DBImplicitGeometry(Connection connection, CityGMLFactory cityGMLFactory, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		psImplicitGeometry = connection.prepareStatement("select ID, MIME_TYPE, REFERENCE_TO_LIBRARY, dbms_lob.getLength(LIBRARY_OBJECT) as DB_LIBRARY_OBJECT_LENGTH, RELATIVE_GEOMETRY_ID from IMPLICIT_GEOMETRY where ID=?");
		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SDO_GEOMETRY);
	}

	public ImplicitGeometry read(long id, JGeometry referencePoint, String transformationMatrix, CoreModule core) throws SQLException {
		ResultSet rs = null;

		try {		
			psImplicitGeometry.setLong(1, id);
			rs = psImplicitGeometry.executeQuery();

			// ImplicitGeometry
			ImplicitGeometry implicit = cityGMLFactory.createImplicitGeometry(core);
			boolean isValid = false;

			if (rs.next()) {
				// library object
				long dbBlobSize = rs.getLong("DB_LIBRARY_OBJECT_LENGTH");
				String blobURI = rs.getString("REFERENCE_TO_LIBRARY");
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

					String mimeType = rs.getString("MIME_TYPE");
					if (mimeType != null)
						implicit.setMimeType(mimeType);
				}

				long surfaceGeometryId = rs.getLong("RELATIVE_GEOMETRY_ID");
				if (!rs.wasNull() && surfaceGeometryId != 0) {
					isValid = true;

					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(surfaceGeometryId);
					if (geometry != null) {
						GeometryProperty geometryProperty = new GeometryPropertyImpl();

						if (geometry.getAbstractGeometry() != null)
							geometryProperty.setGeometry(geometry.getAbstractGeometry());
						else
							geometryProperty.setHref(geometry.getTarget());

						implicit.setRelativeGeometry(geometryProperty);
					}
				}
			}

			if (!isValid)
				return null;

			// referencePoint
			if (referencePoint != null) {
				PointProperty pointProperty = sdoGeometry.getPoint(referencePoint, false);

				if (pointProperty != null)
					implicit.setReferencePoint(pointProperty);
			}

			// transformationMatrix
			if (transformationMatrix != null) {
				List<Double> m = Util.string2double(transformationMatrix, "\\s+");

				if (m != null && m.size() >= 16) {
					Matrix matrix = new Matrix(4, 4);
					matrix.setMatrix(m.subList(0, 16));

					implicit.setTransformationMatrix(cityGMLFactory.createTransformationMatrix4x4(matrix, core));
				}
			}

			if (isValid) {
				dbExporterManager.updateFeatureCounter(CityGMLClass.IMPLICITGEOMETRY);
				return implicit;
			}

			return null;
		} finally {
			if (rs != null)
				rs.close();
		}
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
