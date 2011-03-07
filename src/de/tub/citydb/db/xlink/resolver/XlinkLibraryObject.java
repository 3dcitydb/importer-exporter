package de.tub.citydb.db.xlink.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkLibraryObject;
import de.tub.citydb.log.Logger;

public class XlinkLibraryObject implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection externalFileConn;
	private final Config config;

	private PreparedStatement psPrepare;
	PreparedStatement psSelect;
	private String localPath;

	public XlinkLibraryObject(Connection textureImageConn, Config config) throws SQLException {
		this.externalFileConn = textureImageConn;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();

		psPrepare = externalFileConn.prepareStatement("update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=empty_blob() where ID=?");
		psSelect = externalFileConn.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=? for update");
	}

	public boolean insert(DBXlinkLibraryObject xlink) throws SQLException {
		String objectFileName = xlink.getFileURI();
		boolean isRemote = true;
		URL objectURL = null;
		
		try {
			// first step: prepare BLOB
			psPrepare.setLong(1, xlink.getId());
			psPrepare.executeUpdate();

			// second step: get prepared BLOB to fill it with contents
			psSelect.setLong(1, xlink.getId());
			OracleResultSet rs = (OracleResultSet)psSelect.executeQuery();
			if (!rs.next()) {
				LOG.error("Database error while importing library object: " + objectFileName);

				rs.close();
				externalFileConn.rollback();
				return false;
			}

			BLOB blob = rs.getBLOB(1);
			rs.close();

			// third step: try and upload library object data
			try {
				objectURL = new URL(objectFileName);
				objectFileName = objectURL.toString();

			} catch (MalformedURLException malURL) {
				isRemote = false;
				File objectFile = new File(objectFileName);
				objectFileName = localPath + File.separator + objectFile.getPath();
			}

			LOG.debug("Importing library object: " + objectFileName);

			InputStream in = null;

			if (isRemote) {
				in = objectURL.openStream();
			} else {
				in = new FileInputStream(objectFileName);
			}

			if (in == null) {
				LOG.error("Database error while importing library object: " + objectFileName);

				externalFileConn.rollback();
				return false;
			}

			OutputStream out = blob.setBinaryStream(1L);

			int size = blob.getBufferSize();
			byte[] buffer = new byte[size];
			int length = -1;

			while ((length = in.read(buffer)) != -1)
				out.write(buffer, 0, length);
		
			in.close();
			out.close();
			externalFileConn.commit();
			return true;
			
		} catch (FileNotFoundException fnfEx) {
			LOG.error("Failed to find library object file '" + objectFileName + "'.");

			externalFileConn.rollback();
			return false;
		} catch (IOException ioEx) {
			LOG.error("Failed to read library object file '" + objectFileName + "': " + ioEx.getMessage());

			externalFileConn.rollback();
			return false;
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while importing library object '" + objectFileName + "': " + sqlEx.getMessage());

			externalFileConn.rollback();
			return false;
		} 
	}

	@Override
	public void executeBatch() throws SQLException {
		// we do not have any action here, since we are heavily committing and roll-backing
		// within the insert-method. that's also the reason why we need a separated connection instance.
	}

	@Override
	public void close() throws SQLException {
		psPrepare.close();
		psSelect.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.LIBRARY_OBJECT;
	}

}
