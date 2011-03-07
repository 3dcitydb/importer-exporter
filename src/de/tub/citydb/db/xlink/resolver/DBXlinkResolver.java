package de.tub.citydb.db.xlink.resolver;

import java.sql.SQLException;

public interface DBXlinkResolver {
	public void executeBatch() throws SQLException;
	public void close() throws SQLException;
	public DBXlinkResolverEnum getDBXlinkResolverType();
}
