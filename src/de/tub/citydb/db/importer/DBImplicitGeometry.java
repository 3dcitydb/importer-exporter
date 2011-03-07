package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.core.ImplicitGeometry;
import de.tub.citygml4j.model.gml.GeometryProperty;

public class DBImplicitGeometry implements DBImporter {
	private final static ReentrantLock mainLock = new ReentrantLock();

	private final Connection batchConn;
	private final Connection commitConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psImplicitGeometry;
	private PreparedStatement psUpdateImplicitGeometry;
	private PreparedStatement psSelectLibraryObject;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private ResultSet rs;

	private int batchCounter;
	
	public DBImplicitGeometry(Connection batchConn, Connection commitConn,DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.commitConn = commitConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psImplicitGeometry = commitConn.prepareStatement("insert into IMPLICIT_GEOMETRY (ID) values (?)");
		psUpdateImplicitGeometry = batchConn.prepareStatement("update IMPLICIT_GEOMETRY set MIME_TYPE=?, REFERENCE_TO_LIBRARY=?, RELATIVE_GEOMETRY_ID=? where ID=?");
		psSelectLibraryObject = batchConn.prepareStatement("select ID from IMPLICIT_GEOMETRY where REFERENCE_TO_LIBRARY=?");
		
		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
	}

	public long insert(ImplicitGeometry implicitGeometry, long parentId) throws SQLException {
		// writing implicit geometries differs from other importers. we want to avoid duplicate
		// entries for library objects. thus we have to make sure on this prior to inserting entries.
		String libraryURI = null;
		long implicitGeometryId = 0;

		// we need to synchronize this check.
		final ReentrantLock lock = mainLock;
		lock.lock();

		try {
			// check on library object reference
			if (implicitGeometry.getLibraryObject() != null) {
				libraryURI = implicitGeometry.getLibraryObject().trim();

				// check if we have the same library object in database
				psSelectLibraryObject.setString(1, libraryURI);
				rs = psSelectLibraryObject.executeQuery();

				if (rs.next()) {
					long id = rs.getLong(1);
					rs.close();

					return id;
				}

				rs.close();

				// well, no entry found. so let us create a new one...
				implicitGeometryId = dbImporterManager.getDBId(DBSequencerEnum.IMPLICIT_GEOMETRY_SEQ);
				psImplicitGeometry.setLong(1, implicitGeometryId);
				psImplicitGeometry.execute();
			}

		} finally {
			lock.unlock();
		}

		// ok, the rest can be handled concurrently and as batch update...
		GeometryProperty geometryProperty = implicitGeometry.getRelativeGMLGeometry();
		psUpdateImplicitGeometry.setLong(4, implicitGeometryId);

		// mimeType
        if (implicitGeometry.getMimeType() != null)
        	psUpdateImplicitGeometry.setString(1, implicitGeometry.getMimeType());
        else
        	psUpdateImplicitGeometry.setNull(1, Types.VARCHAR);

        // libraryObject
        if (libraryURI != null) {
        	psUpdateImplicitGeometry.setString(2, libraryURI);

			dbImporterManager.propagateXlink(new DBXlinkExternalFile(
					implicitGeometryId,
					libraryURI,
					DBXlinkExternalFileEnum.LIBRARY_OBJECT
			));
        } else
        	psUpdateImplicitGeometry.setNull(2, Types.VARCHAR);

        // relativeGeometry
        if (geometryProperty != null) {
            long surfaceGeometryId = 0;

            if (geometryProperty.getGeometry() != null) {
            	surfaceGeometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), parentId);
            } else {
                // xlink
                String href = geometryProperty.getHref();

                if (href != null && href.length() != 0) {
                    DBXlinkBasic xlink = new DBXlinkBasic(
                            implicitGeometryId,
                            DBTableEnum.IMPLICIT_GEOMETRY,
                            href,
                            DBTableEnum.SURFACE_GEOMETRY
                    );

                    xlink.setAttrName("RELATIVE_GEOMETRY_ID");
                    dbImporterManager.propagateXlink(xlink);
                }
            }

            if (surfaceGeometryId != 0)
            	psUpdateImplicitGeometry.setLong(3, surfaceGeometryId);
            else
            	psUpdateImplicitGeometry.setNull(3, 0);
        } else
        	psUpdateImplicitGeometry.setNull(3, 0);

        dbImporterManager.updateFeatureCounter(CityGMLClass.IMPLICITGEOMETRY);
        psUpdateImplicitGeometry.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.IMPLICIT_GEOMETRY);
        
		return implicitGeometryId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psUpdateImplicitGeometry.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.IMPLICIT_GEOMETRY;
	}
}
