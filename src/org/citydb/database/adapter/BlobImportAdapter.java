package org.citydb.database.adapter;

import java.io.InputStream;
import java.sql.SQLException;

public interface BlobImportAdapter {
	public boolean insert(long id, InputStream in, String fileName) throws SQLException;	
	public void close() throws SQLException;	
}
