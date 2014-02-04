package de.tub.citydb.database.adapter.postgis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.tub.citydb.database.adapter.BlobExportAdapter;
import de.tub.citydb.log.Logger;

public class BlobExportAdapterImpl implements BlobExportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psExport;

	protected BlobExportAdapterImpl(Connection connection) {
		this.connection = connection;
	}

	@Override
	public boolean getInFile(long id, String objectName, String fileName) throws SQLException {
		ResultSet rs = null;
		FileOutputStream out = null;

		try {
			if (psExport == null)
				psExport = connection.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=?");

			// try and read texture image attribute from SURFACE_DATA table
			psExport.setLong(1, id);
			rs = psExport.executeQuery();
			if (!rs.next()) {
				LOG.error("Error while exporting a library object file: " + objectName + " does not exist in database.");
				return false;
			}

			byte[] buf = rs.getBytes(1);
			if (rs.wasNull() || buf.length == 0) {
				LOG.error("Failed to read library object file: " + objectName + ".");
				return false;
			}
			
			out = new FileOutputStream(fileName);
			out.write(buf);

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
		if (psExport != null)
			psExport.close();
	}

}
