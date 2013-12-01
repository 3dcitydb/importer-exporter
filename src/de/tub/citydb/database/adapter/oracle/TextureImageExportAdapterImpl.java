package de.tub.citydb.database.adapter.oracle;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import de.tub.citydb.database.adapter.TextureImageExportAdapter;
import de.tub.citydb.log.Logger;

public class TextureImageExportAdapterImpl implements TextureImageExportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psExport;

	protected TextureImageExportAdapterImpl(Connection connection) {
		this.connection = connection;
	}

	@Override
	public byte[] getInByteArray(long id, String objectName, String fileName) throws SQLException {
		OracleResultSet rs = null;
		OrdImage imgProxy = null;

		try {
			if (psExport == null)
				psExport = connection.prepareStatement("select TEX_IMAGE from SURFACE_DATA where ID=?");

			// try and read texture image attribute from SURFACE_DATA table
			psExport.setLong(1, id);
			rs = (OracleResultSet)psExport.executeQuery();
			if (!rs.next()) {
				LOG.error("Error while exporting a texture file: " + objectName + " does not exist in database.");
				return null;
			}

			imgProxy = (OrdImage)rs.getORAData(1, OrdImage.getORADataFactory());
			if (imgProxy == null) {
				LOG.error("Failed to read texture file: " + objectName + ".");
				return null;
			}

			return imgProxy.getDataInByteArray();
		} catch (IOException e) {
			LOG.error("Failed to read texture file " + objectName + ": " + e.getMessage());
			return null;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}
			}

			if (imgProxy != null) {
				try {
					imgProxy.close();				
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
			out.write(getInByteArray(id, objectName, fileName));			
			return true;
		} catch (IOException e) {
			LOG.error("Failed to write texture file " + objectName + ": " + e.getMessage());
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
		OrdImage imgProxy = null;

		try {
			// try and read texture image attribute from result set
			imgProxy = (OrdImage)((OracleResultSet)rs).getORAData(columnName, OrdImage.getORADataFactory());
			if (imgProxy == null) {
				LOG.error("Failed to read texture file: " + objectName + ".");
				return null;
			}

			return new ByteArrayInputStream(imgProxy.getDataInByteArray());
		} catch (IOException e) {
			LOG.error("Failed to read texture file " + objectName + ": " + e.getMessage());
			return null;
		} finally {
			if (imgProxy != null) {
				try {
					imgProxy.close();				
				} catch (SQLException e) {
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
