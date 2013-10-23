package de.tub.citydb.api.database;

import java.sql.SQLException;

import de.tub.citydb.api.geometry.BoundingBox;

public interface DatabaseUtil {
	public BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs) throws SQLException;
	public boolean isIndexEnabled(String tableName, String columnName) throws SQLException;
}
