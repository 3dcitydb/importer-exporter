package de.tub.citydb.db.exporter;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.implementation.gml._3_1_1.GeometryPropertyImpl;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.core.CoreModule;
import de.tub.citygml4j.model.citygml.core.ImplicitGeometry;
import de.tub.citygml4j.model.citygml.core.TransformationMatrix4x4;
import de.tub.citygml4j.model.gml.GeometryProperty;
import de.tub.citygml4j.model.gml.PointProperty;

public class DBImplicitGeometry implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Connection connection;

	private PreparedStatement psImplicitGeometry;
	private ResultSet rs;

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

	public ImplicitGeometry read(long id, JGeometry referencePoint, String transformationMatrix, CoreModule factory) throws SQLException {
		psImplicitGeometry.setLong(1, id);
		rs = psImplicitGeometry.executeQuery();

		// ImplicitGeometry
		ImplicitGeometry implicit = cityGMLFactory.createImplicitGeometry(factory);
		boolean isValid = false;

		if (rs.next()) {
			// library object
			long dbBlobSize = rs.getLong("DB_LIBRARY_OBJECT_LENGTH");
			String blobURI = rs.getString("REFERENCE_TO_LIBRARY");
			if (blobURI != null) {
				// export texture image from database
				isValid = true;
				if (dbBlobSize > 0) {
					File file = new File(blobURI);
					implicit.setLibraryObject(file.getName());

					DBXlinkExternalFile xlink = new DBXlinkExternalFile(
							id,
							file.getName(),
							DBXlinkExternalFileEnum.LIBRARY_OBJECT);
					dbExporterManager.propagateXlink(xlink);
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
			String[] splitted = transformationMatrix.trim().split("\\s+");

			if (splitted != null && splitted.length >= 16) {
				List<Double> transformationList = new ArrayList<Double>();

				for (int i = 0; i < 16; i++) {
					if (splitted[i] == null)
						continue;

					Double matrixPart = null;
					try {
						matrixPart = Double.parseDouble(splitted[i]);
					} catch (NumberFormatException nfe) {
						//
					}

					if (matrixPart != null)
						transformationList.add(matrixPart);
				}

				TransformationMatrix4x4 matrix = cityGMLFactory.createTransformationMatrix4x4(transformationList, factory);
				implicit.setTransformationMatrix4x4(matrix);
			}
		}

		if (!isValid)
			return null;
		else {
			dbExporterManager.updateFeatureCounter(CityGMLClass.IMPLICITGEOMETRY);
			return implicit;
		}
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.IMPLICIT_GEOMETRY;
	}

}
