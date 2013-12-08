package de.tub.citydb.database.adapter.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.database.adapter.BlobImportAdapter;
import de.tub.citydb.log.Logger;

public class BlobImportAdapterImpl implements BlobImportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psUpdate;

	protected BlobImportAdapterImpl(Connection connection) throws SQLException {
		this.connection = connection;
		psUpdate = connection.prepareStatement("update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=? where ID=?");
	}

	@Override
	public boolean insert(long id, InputStream in, String fileName) throws SQLException {
		try {
			psUpdate.setBinaryStream(1, in, in.available());
			psUpdate.setLong(2, id);
			psUpdate.execute();		

			connection.commit();
			return true;
		} catch (IOException e) {
			LOG.error("Failed to read library object file '" + fileName + "': " + e.getMessage());
			connection.rollback();
			return false;
		} catch (SQLException e) {
			LOG.error("SQL error while importing library object file '" + fileName + "': " + e.getMessage());
			connection.rollback();
			return false;
		}
	}

	@Override
	public void close() throws SQLException {
		psUpdate.close();
	}

}
