package de.tub.citydb.db.exporter;

import java.sql.SQLException;


public interface DBExporter {
	public void close() throws SQLException;
	public DBExporterEnum getDBExporterType();
}
