package org.citydb.database.adapter.oracle;

import org.citydb.api.database.DatabaseType;
import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class OracleAdapter extends AbstractDatabaseAdapter {
	
	public OracleAdapter() {
		geometryAdapter = new GeometryConverterAdapter();
		utilAdapter = new UtilAdapter(this);
		workspaceAdapter = new WorkspaceManagerAdapter(this);
		sqlAdapter = new SQLAdapter();
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
		return "oracle.jdbc.OracleDriver";
	}

	@Override
	public String getJDBCUrl(String server, int port, String database) {
		return "jdbc:oracle:thin:@//" + server + ":" + port + "/" + database;
	}

	@Override
	public int getMaxBatchSize() {
		return 65535;
	}

}
