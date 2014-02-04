package de.tub.citydb.database.adapter;

import java.io.InputStream;
import java.sql.SQLException;

public interface TextureImageImportAdapter {
	public boolean insert(long id, InputStream in, String fileName, String mimeType, String codeSpace) throws SQLException;	
	public void close() throws SQLException;
	
}
