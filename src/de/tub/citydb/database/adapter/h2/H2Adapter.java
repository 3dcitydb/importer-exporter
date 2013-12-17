package de.tub.citydb.database.adapter.h2;

import de.tub.citydb.api.database.DatabaseType;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;

public class H2Adapter extends AbstractDatabaseAdapter {
	// NOTE: this adapter is currently only used for cache tables
	
	public H2Adapter() {
		sqlAdapter = new SQLAdapter();
	}
	
	@Override
	public DatabaseType getDatabaseType() {
		return null;
	}

	@Override
	public boolean hasVersioningSupport() {
		return false;
	}

	@Override
	public int getDefaultPort() {
		return -1;
	}

	@Override
	public String getConnectionFactoryClassName() {
		return "org.h2.Driver";
	}

	@Override
	public String getJDBCUrl(String server, int port, String database) {
		return "jdbc:h2:" + server;
	}

	@Override
	public int getMaxBatchSize() {
		return 65535;
	}

}
