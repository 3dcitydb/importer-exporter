package org.citydb.ade.exporter;

import java.sql.SQLException;

import org.citydb.citygml.exporter.CityGMLExportException;

public interface ADEExporter {
	public void close() throws CityGMLExportException, SQLException;
}
