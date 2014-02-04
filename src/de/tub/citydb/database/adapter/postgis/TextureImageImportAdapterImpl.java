package de.tub.citydb.database.adapter.postgis;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.database.adapter.TextureImageImportAdapter;
import de.tub.citydb.log.Logger;

public class TextureImageImportAdapterImpl implements TextureImageImportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psUpdate;

	protected TextureImageImportAdapterImpl(Connection connection) throws SQLException {
		this.connection = connection;
		psUpdate = connection.prepareStatement("insert into TEX_IMAGE (ID, TEX_IMAGE_URI, TEX_IMAGE, MIME_TYPE, MIME_TYPE_CODESPACE) values (?, ?, ?, ?, ?)");
	}

	@Override
	public boolean insert(long id, InputStream in, String fileName, String mimeType, String codeSpace) throws SQLException {
		try {
			psUpdate.setBinaryStream(1, in, in.available());
			psUpdate.setLong(2, id);
			psUpdate.execute();		

			return true;
		} catch (IOException e) {
			LOG.error("Failed to read texture file '" + fileName + "': " + e.getMessage());
			return false;
		} catch (SQLException e) {
			LOG.error("SQL error while importing texture file '" + fileName + "': " + e.getMessage());
			return false;
		}
	}

	@Override
	public void close() throws SQLException {
		psUpdate.close();
	}

}
