package org.citydb.citygml.deleter.controller;

import org.citydb.citygml.deleter.DeleteException;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;

import java.sql.SQLException;

public class GlobalAppearanceCleaner {
	private final AbstractDatabaseAdapter databaseAdapter;
	private volatile boolean isDeleting = false;

	public GlobalAppearanceCleaner() {
		this.databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
	}

	public int doCleanup() throws DeleteException {
		try {
			isDeleting = true;
			String schema = databaseAdapter.getConnectionDetails().getSchema();
			return databaseAdapter.getUtil().cleanupGlobalAppearances(schema);
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
