package org.citydb.citygml.deleter.controller;

import org.citydb.citygml.deleter.DeleteException;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class GlobalAppearanceCleaner {
	private final ConnectionManager connectionManager;
	private final AbstractDatabaseAdapter databaseAdapter;
	private volatile boolean isDeleting = false;

	public GlobalAppearanceCleaner(ConnectionManager connectionManager, AbstractDatabaseAdapter databaseAdapter) {
		this.connectionManager = connectionManager;
		this.databaseAdapter = databaseAdapter;
	}

	public int doCleanup() throws DeleteException {
		try {
			isDeleting = true;
			Connection connection = connectionManager.getConnection();
			String schema = databaseAdapter.getConnectionDetails().getSchema();
			return databaseAdapter.getUtil().cleanupGlobalAppearances(schema, connection);
		} catch (SQLException e) {
			throw new DeleteException("Failed to delete global appearances.", e);
		} finally {
			isDeleting = false;
		}
	}

	public void interrupt() {
		if (isDeleting) {
			databaseAdapter.getUtil().interruptDatabaseOperation();
		}
	}
}
