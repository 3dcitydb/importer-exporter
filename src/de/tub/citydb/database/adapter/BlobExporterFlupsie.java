package de.tub.citydb.database.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface BlobExporterFlupsie {
	public abstract boolean exportBlobInFile(ResultSet rs, int columnIndex, String objectName, String fileName) throws SQLException;
	public abstract boolean exportTextureImageInFile(ResultSet rs, int columnIndex, String objectName, String fileName) throws SQLException;
}
