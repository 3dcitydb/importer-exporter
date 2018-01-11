package org.citydb.database.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.citydb.config.project.database.DBConnection;
import org.citydb.log.Logger;

public abstract class AbstractSchemaManagerAdapter {
	private final Logger LOG = Logger.getInstance();
	protected final AbstractDatabaseAdapter databaseAdapter;

	protected AbstractSchemaManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}
	
	public abstract String getDefaultSchema();
	public abstract boolean equalsDefaultSchema(String schema);
	public abstract boolean existsSchema(Connection connection, String schema);
	public abstract List<String> fetchSchemasFromDatabase(Connection connection) throws SQLException;

	public boolean existsSchema(String schema) {
		return existsSchema(schema, false);
	}

	public boolean existsSchema(String schema, boolean logResult) {
		try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
			boolean exists = existsSchema(conn, schema);
			if (logResult) {
				if (!exists)
					LOG.error("Database schema '" + schema + "' is not available.");
				else 
					LOG.info("Switching to database schema '" + schema + "'.");
			}

			return exists;
		} catch (SQLException e) {
			return false;
		}
	}

	public List<String> fetchSchemasFromDatabase(DBConnection dbConnection) throws SQLException {
		Properties properties = new Properties();
		properties.setProperty("user", dbConnection.getUser());
		properties.setProperty("password", dbConnection.getInternalPassword());

		try (Connection conn = DriverManager.getConnection(databaseAdapter.getJDBCUrl(
				dbConnection.getServer(), dbConnection.getPort(), dbConnection.getSid()), properties)) {
			return fetchSchemasFromDatabase(conn);
		}
	}

}
