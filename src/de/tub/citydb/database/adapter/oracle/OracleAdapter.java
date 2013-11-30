package de.tub.citydb.database.adapter.oracle;

import java.util.Properties;

import oracle.jdbc.OracleConnection;
import de.tub.citydb.api.database.DatabaseType;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;

public class OracleAdapter extends AbstractDatabaseAdapter {
	
	public OracleAdapter() {
		geometryAdapter = new GeometryConverterAdapter();
		utilAdapter = new UtilAdapter(this);
		workspaceAdapter = new WorkspaceManagerAdapter(this);
		sqlAdapter = new SQLAdapter(this);
	}

	@Override
	public DatabaseType getDatabaseType() {
		return DatabaseType.ORACLE;
	}

	@Override
	public boolean hasVersioningSupport() {
		return true;
	}

	@Override
	public int getDefaultPort() {
		return 1521;
	}

	@Override
	public String getConnectionFactoryClassName() {
		return "oracle.jdbc.pool.OracleDataSource";
	}

	@Override
	public String getJDBCUrl(String server, int port, String database) {
		return "jdbc:oracle:thin:@//" + server + ":" + port + "/" + database;
	}

	@Override
	public Properties getConnectionProperties() {
		Properties properties = new Properties();

		// let statement data buffers be cached on a per thread basis
		properties.put(OracleConnection.CONNECTION_PROPERTY_USE_THREADLOCAL_BUFFER_CACHE, "true");

		return properties;
	}
	
	@Override
	public int getMaxBatchSize() {
		return 65535;
	}

}
