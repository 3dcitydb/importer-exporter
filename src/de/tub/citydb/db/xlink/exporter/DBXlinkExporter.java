package de.tub.citydb.db.xlink.exporter;

import java.sql.SQLException;

public interface DBXlinkExporter {
	public void close() throws SQLException;
	public DBXlinkExporterEnum getDBXlinkExporterType();
}
