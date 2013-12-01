package de.tub.citydb.database.adapter.oracle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import de.tub.citydb.database.adapter.BlobExportAdapter;
import de.tub.citydb.log.Logger;

public class BlobExportAdapterImpl implements BlobExportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psLibraryObject;

	protected BlobExportAdapterImpl(Connection connection) {
		this.connection = connection;
	}

	@Override
	public boolean getInFile(long id, String objectName, String fileName) throws SQLException {
		OracleResultSet rs = null;
		InputStream in = null;
		FileOutputStream out = null;

		try {
			if (psLibraryObject == null)
				psLibraryObject = connection.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=?");

			// try and read object reference attribute from IMPLICIT_OBJECT table
			psLibraryObject.setLong(1, id);
			rs = (OracleResultSet)psLibraryObject.executeQuery();
			if (!rs.next()) {
				LOG.error("Error while exporting a library object: " + objectName + " does not exist in database.");
				return false;
			}

			BLOB blob = rs.getBLOB(1);
			if (blob == null) {
				LOG.error("Error while exporting a library object: " + objectName + " does not exist in database.");
				return false;
			}

			int size = blob.getBufferSize();
			byte[] buffer = new byte[size];
			in = blob.getBinaryStream(1L);
			out = new FileOutputStream(fileName);

			int length = -1;
			while ((length = in.read(buffer)) != -1)
				out.write(buffer, 0, length);

			return true;
		} catch (IOException e) {
			LOG.error("Failed to write library object file " + objectName + ": " + e.getMessage());
			return false;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					//
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	@Override
	public void close() throws SQLException {
		if (psLibraryObject != null)
			psLibraryObject.close();
	}

}
