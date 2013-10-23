package de.tub.citydb.database.adapter.postgis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.tub.citydb.database.adapter.BlobExportAdapter;
import de.tub.citydb.database.adapter.TextureImageExportAdapter;
import de.tub.citydb.database.adapter.postgis.SQLAdapter.BlobType;
import de.tub.citydb.log.Logger;

public class BlobExportAdapterImpl implements TextureImageExportAdapter, BlobExportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psExport;
	private BlobType blobType;

	protected BlobExportAdapterImpl(Connection connection, BlobType blobType) throws SQLException {
		this.connection = connection;
		this.blobType = blobType;
		
		psExport = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
				"select TEX_IMAGE from SURFACE_DATA where ID=?" : "select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=?");
	}

	@Override
	public boolean exportInFile(long id, String objectName, String fileName) throws SQLException {
		ResultSet rs = null;
		FileOutputStream out = null;

		try {
			// try and read texture image attribute from SURFACE_DATA table
			psExport.setLong(1, id);
			rs = psExport.executeQuery();
			if (!rs.next()) {
				LOG.error("Error while exporting a " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + " does not exist in database.");
				return false;
			}

			byte[] buf = rs.getBytes(1);
			if (buf == null || buf.length == 0) {
				LOG.error("Failed to read " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + ".");
				return false;
			}

			out = new FileOutputStream(fileName);
			out.write(buf);
			out.close();
			return true;
		} catch (IOException e) {
			LOG.error("Failed to write " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file " + fileName + ": " + e.getMessage());
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
		psExport.close();
	}

}
