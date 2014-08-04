package org.citydb.database.adapter.postgis;

import org.citydb.api.database.DatabaseType;
import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class PostGISAdapter extends AbstractDatabaseAdapter {

	public PostGISAdapter() {
		geometryAdapter = new GeometryConverterAdapter();
		utilAdapter = new UtilAdapter(this);
		workspaceAdapter = new WorkspaceManagerAdapter(this);
		sqlAdapter = new SQLAdapter();
	}
	
	@Override
	public DatabaseType getDatabaseType() {
		return DatabaseType.POSTGIS;
	}

	@Override
	public boolean hasVersioningSupport() {
		return false;
	}

	@Override
	public int getDefaultPort() {
		return 5432;
	}

	@Override
	public String getConnectionFactoryClassName() {
		return "org.postgresql.Driver";
	}

	@Override
	public String getJDBCUrl(String server, int port, String database) {
		return "jdbc:postgresql://" + server + ":" + port + "/" + database;
	}

	@Override
	public int getMaxBatchSize() {
		return 65535;
	}
	
}
