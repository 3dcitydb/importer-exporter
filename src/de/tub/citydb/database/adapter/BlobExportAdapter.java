package de.tub.citydb.database.adapter;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface BlobExportAdapter {
	public byte[] getInByteArray(long id, String objectName, String fileName) throws SQLException;
	public boolean getInFile(long id, String objectName, String fileName) throws SQLException;
	public InputStream getInStream(ResultSet rs, String columnName, String objectName) throws SQLException;
	public void close() throws SQLException;
}
