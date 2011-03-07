package de.tub.citydb.db.temp;

import java.sql.SQLException;

public interface DBTempTable {
	public boolean isCreated();
	public void create() throws SQLException;
	public void createIndexed() throws SQLException;
	public void drop() throws SQLException;
	public DBTempTableEnum getType();
}
