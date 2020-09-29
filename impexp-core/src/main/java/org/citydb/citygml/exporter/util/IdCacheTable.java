package org.citydb.citygml.exporter.util;

import org.citydb.database.adapter.AbstractSQLAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class IdCacheTable {
	private final Connection connection;
	private final AbstractSQLAdapter sqlAdapter;
	private final String tableName;
	private boolean isCreated = false;

	private IdCacheTable (Connection connection, AbstractSQLAdapter sqlAdapter) {
		this.connection = connection;
		this.sqlAdapter = sqlAdapter;
		this.tableName = "TMP_ID_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
	}

	public static IdCacheTable newInstance(Connection connection, AbstractSQLAdapter sqlAdapter) throws SQLException {
		IdCacheTable instance = new IdCacheTable(connection, sqlAdapter);
		instance.create();
		return instance;
	}

	public void truncate() throws SQLException {
		if (!isCreated)
			return;

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate("delete from " + tableName);
			connection.commit();
		}
	}

	public String getTableName() {
		return tableName;
	}

	public Connection getConnection() {
		return connection;
	}

	public void drop() throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate("drop table " + tableName);
			connection.commit();
		}
		isCreated = false;
	}

	private void create() throws SQLException {
		if (!isCreated) {
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate(sqlAdapter.getCreateUnloggedTable(tableName, "(" + "ID " + sqlAdapter.getInteger() + ")"));
				connection.commit();
			}
			isCreated = true;
		}
	}

}
