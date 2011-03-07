package de.tub.citydb.db.importer;

import java.sql.SQLException;

public interface DBImporter {
	public void executeBatch() throws SQLException;
	public void close() throws SQLException;
	public DBImporterEnum getDBImporterType();
}
