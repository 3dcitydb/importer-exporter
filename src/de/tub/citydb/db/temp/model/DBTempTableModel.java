package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;


public interface DBTempTableModel {
	public void createGTT(Connection conn, String tableName) throws SQLException;
	public void createGTTIndexes(Connection conn, String tableName) throws SQLException;
	public void createHeap(Connection conn, String tableName) throws SQLException;
	public void createHeapIndexes(Connection conn, String tableName) throws SQLException;
	public DBTempTableModelEnum getType();
}
