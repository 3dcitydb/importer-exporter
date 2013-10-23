package de.tub.citydb.database.adapter;

import java.sql.SQLException;

public interface TextureImageExportAdapter {
	public boolean exportInFile(long id, String objectName, String fileName) throws SQLException;	
	public void close() throws SQLException;
}
