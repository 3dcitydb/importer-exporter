package de.tub.citydb.db.xlink.importer;

import java.sql.SQLException;

public interface DBXlinkImporter {
	public void executeBatch() throws SQLException;
	public void close() throws SQLException;
	public DBXlinkImporterEnum getDBXlinkImporterType();
}
