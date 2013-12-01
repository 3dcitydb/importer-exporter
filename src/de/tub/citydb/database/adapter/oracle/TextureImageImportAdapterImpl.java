package de.tub.citydb.database.adapter.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import de.tub.citydb.database.adapter.TextureImageImportAdapter;
import de.tub.citydb.log.Logger;

public class TextureImageImportAdapterImpl implements TextureImageImportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psPrepare;
	private PreparedStatement psSelect;
	private OraclePreparedStatement psInsert;

	protected TextureImageImportAdapterImpl(Connection connection) throws SQLException {
		this.connection = connection;
		psPrepare = connection.prepareStatement("update SURFACE_DATA set TEX_IMAGE=ordimage.init() where ID=?");
		psSelect = connection.prepareStatement("select TEX_IMAGE from SURFACE_DATA where ID=? for update");
		psInsert = (OraclePreparedStatement)connection.prepareStatement("update SURFACE_DATA set TEX_IMAGE=? where ID=?");
	}

	@Override
	public boolean insert(long id, InputStream in, String fileName) throws SQLException {
		OracleResultSet rs = null;
		
		try {
			// first step: prepare OrdImage
			psPrepare.setLong(1, id);
			psPrepare.executeUpdate();

			// second step: get prepared OrdImage to fill it with contents
			psSelect.setLong(1, id);
			rs = (OracleResultSet)psSelect.executeQuery();
			if (!rs.next()) {
				LOG.error("Database error while importing texture file '" + fileName + "'.");
				connection.rollback();
				return false;
			}

			OrdImage imgProxy = (OrdImage)rs.getORAData(1, OrdImage.getORADataFactory());

			// third step: try and upload image data
			boolean letDBdetermineProperties = true;
			imgProxy.loadDataFromInputStream(in);

			// determine image formats by file extension
			int index = fileName.lastIndexOf('.');
			if (index != -1) {
				String extension = fileName.substring(index + 1, fileName.length()).trim();

				if (extension.toUpperCase().equals("RGB")) {
					imgProxy.setMimeType("image/rgb");
					imgProxy.setFormat("RGB");
					imgProxy.setContentLength(1);
					letDBdetermineProperties = false;
				} else if (extension.toUpperCase().equals("RGBA")) {
					imgProxy.setMimeType("image/x-rgb");
					imgProxy.setFormat("RGBA");
					imgProxy.setContentLength(1);
					letDBdetermineProperties = false;
				}
			}
			
			if (letDBdetermineProperties)
				imgProxy.setProperties();
			
			psInsert.setORAData(1, imgProxy);
			psInsert.setLong(2, id);
			psInsert.execute();

			imgProxy.close();
			connection.commit();
			
			return true;
		} catch (IOException e) {
			LOG.error("Failed to read texture file '" + fileName + "': " + e.getMessage());
			connection.rollback();
			return false;
		} catch (SQLException e) {
			LOG.error("SQL error while importing texture file '" + fileName + "': " + e.getMessage());
			connection.rollback();
			return false;
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
	public void close() throws SQLException {
		psInsert.close();
		psPrepare.close();
		psSelect.close();
	}

}
