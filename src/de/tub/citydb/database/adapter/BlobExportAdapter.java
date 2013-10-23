package de.tub.citydb.database.adapter;

import java.sql.SQLException;

public interface BlobExportAdapter {
	public boolean exportInFile(long id, String objectName, String fileName) throws SQLException;	
	public void close() throws SQLException;
}
