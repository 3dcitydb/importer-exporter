package de.tub.citydb.database.adapter;

import de.tub.citydb.api.database.BalloonTemplateFactory;
import de.tub.citydb.api.database.DatabaseAdapter;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.DatabaseMetaDataImpl;
import de.tub.citydb.modules.kml.database.BalloonTemplateFactoryImpl;

public abstract class AbstractDatabaseAdapter implements DatabaseAdapter {
	protected DatabaseConnectionPool connectionPool;	
	protected DatabaseMetaDataImpl metaData;
	protected DBConnection connectionDetails;
	protected AbstractGeometryConverterAdapter geometryAdapter;
	protected AbstractWorkspaceManagerAdapter workspaceAdapter;
	protected AbstractUtilAdapter utilAdapter;
	protected AbstractSQLAdapter sqlAdapter;
	
	public AbstractDatabaseAdapter() {
		connectionPool = DatabaseConnectionPool.getInstance();
	}
	
	public abstract int getDefaultPort();
	public abstract String getConnectionFactoryClassName();
	public abstract String getJDBCUrl(String server, int port, String database);
	public abstract int getMaxBatchSize();
		
	@Override
	public DBConnection getConnectionDetails() {
		return connectionDetails;
	}

	public void setConnectionDetails(DBConnection connectionDetails) {
		this.connectionDetails = connectionDetails;
	}
	
	@Override
	public DatabaseMetaDataImpl getConnectionMetaData() {
		return metaData;
	}
	
	public void setConnectionMetaData(DatabaseMetaDataImpl metaData) {
		this.metaData = metaData;
	}

	@Override
	public AbstractGeometryConverterAdapter getGeometryConverter() {
		return geometryAdapter;
	}
	
	@Override
	public AbstractWorkspaceManagerAdapter getWorkspaceManager() {
		return workspaceAdapter;
	}

	@Override
	public AbstractUtilAdapter getUtil() {
		return utilAdapter;
	}

	@Override
	public BalloonTemplateFactory getBalloonTemplateFactory() {
		return BalloonTemplateFactoryImpl.getInstance();
	}
	
	public AbstractSQLAdapter getSQLAdapter() {
		return sqlAdapter;
	}
	
}
