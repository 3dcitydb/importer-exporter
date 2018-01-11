package org.citydb.ade.importer;

import java.sql.SQLException;

import org.citydb.citygml.importer.CityGMLImportException;

public interface ADEImporter {
	public void executeBatch() throws CityGMLImportException, SQLException;
	public void close() throws CityGMLImportException, SQLException;
}
