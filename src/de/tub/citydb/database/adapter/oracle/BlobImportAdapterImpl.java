package de.tub.citydb.database.adapter.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import de.tub.citydb.database.adapter.BlobImportAdapter;
import de.tub.citydb.log.Logger;

public class BlobImportAdapterImpl implements BlobImportAdapter {
	protected final Logger LOG = Logger.getInstance();
	protected final Connection connection;

	private PreparedStatement psPrepare;
	private PreparedStatement psSelect;

	protected BlobImportAdapterImpl(Connection connection) throws SQLException {
		this.connection = connection;
		psPrepare = connection.prepareStatement("update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=empty_blob() where ID=?");
		psSelect = connection.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=? for update");
	}

	@Override
	public boolean insert(long id, InputStream in, String fileName) throws SQLException {
		OracleResultSet rs = null;

		try {
			// first step: prepare BLOB
			psPrepare.setLong(1, id);
			psPrepare.executeUpdate();

			// second step: get prepared BLOB to fill it with contents
			psSelect.setLong(1, id);
			rs = (OracleResultSet)psSelect.executeQuery();
			if (!rs.next()) {
				LOG.error("Database error while importing library object: " + fileName);
				connection.rollback();
				return false;
			}

			BLOB blob = rs.getBLOB(1);

			// third step: try and upload library object data
			OutputStream out = blob.setBinaryStream(1L);

			int size = blob.getBufferSize();
			byte[] buffer = new byte[size];
			int length = -1;

			while ((length = in.read(buffer)) != -1)
				out.write(buffer, 0, length);

			in.close();
			out.close();
			blob.free();
			connection.commit();
			return true;
		} catch (IOException e) {
			LOG.error("Failed to read library object file '" + fileName + "': " + e.getMessage());
			connection.rollback();
			return false;
		} catch (SQLException e) {
			LOG.error("SQL error while importing object file '" + fileName + "': " + e.getMessage());
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
		psPrepare.close();
		psSelect.close();
	}

}
