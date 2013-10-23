package de.tub.citydb.database.adapter.postgis;

import java.util.Properties;

import de.tub.citydb.api.database.DatabaseType;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;

public class PostGISAdapter extends AbstractDatabaseAdapter {

	public PostGISAdapter() {
		geometryAdapter = new GeometryConverterAdapter();
		utilAdapter = new UtilsAdapter(this);
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
	public Properties getConnectionProperties() {
		return null;
	}
	
	@Override
	public int getMaxBatchSize() {
		return 10000;
	}

	@Override
	public boolean requiresPseudoTableInSelect() {
		return false;
	}

	@Override
	public String getPseudoTableName() {
		return "";
	}
	
}
