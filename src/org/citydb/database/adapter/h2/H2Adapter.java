package org.citydb.database.adapter.h2;

import org.citydb.api.database.DatabaseType;
import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class H2Adapter extends AbstractDatabaseAdapter {
	// NOTE: this adapter is currently only used for cache tables
	
	public H2Adapter() {
		geometryAdapter = new GeometryConverterAdapter();
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
		return "jdbc:h2:" + server + ";MULTI_THREADED=TRUE;LOG=0;UNDO_LOG=0";
	}

	@Override
	public int getMaxBatchSize() {
		return 65535;
	}

}
