package de.tub.citydb.database.adapter.postgis;

import java.io.ByteArrayInputStream;
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

	protected BlobExportAdapterImpl(Connection connection, BlobType blobType) {
		this.connection = connection;
		this.blobType = blobType;		
	}

	@Override
	public byte[] getInByteArray(long id, String objectName, String fileName) throws SQLException {
		ResultSet rs = null;

		try {
			if (psExport == null)
				psExport = connection.prepareStatement(blobType == BlobType.TEXTURE_IMAGE ?
						"select TEX_IMAGE from SURFACE_DATA where ID=?" : "select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=?");

			// try and read texture image attribute from SURFACE_DATA table
			psExport.setLong(1, id);
			rs = psExport.executeQuery();
			if (!rs.next()) {
				LOG.error("Error while exporting a " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + " does not exist in database.");
				return null;
			}

			byte[] buf = rs.getBytes(1);
			if (rs.wasNull() || buf.length == 0) {
				LOG.error("Failed to read " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file: " + objectName + ".");
				return null;
			}

			return buf;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}
		}
	}

	@Override
	public boolean getInFile(long id, String objectName, String fileName) throws SQLException {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(fileName);

			byte[] buf = getInByteArray(id, objectName, fileName);
			if (buf != null) {
				out.write(buf);
				return true;
			} else
				return false;
			
		} catch (IOException e) {
			LOG.error("Failed to write " + (blobType == BlobType.TEXTURE_IMAGE ? "texture" : "library object") + " file " + fileName + ": " + e.getMessage());
			return false;
		} finally {
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
	public ByteArrayInputStream getInStream(ResultSet rs, String columnName, String objectName) throws SQLException {
		return new ByteArrayInputStream(rs.getBytes(columnName));
	}

	@Override
	public void close() throws SQLException {
		if (psExport != null)
			psExport.close();
	}

}
