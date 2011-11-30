package de.tub.citydb.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseConnectionDetails;
import de.tub.citydb.api.database.DatabaseMetaData;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.util.database.DBUtil;

public class DatabaseControllerImpl implements DatabaseController {
	private final Config config;
	private final DatabaseConnectionPool dbPool;

	private ConnectionViewHandler viewHandler;

	public DatabaseControllerImpl(Config config) {
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
	}	
	
	public void setConnectionViewHandler(ConnectionViewHandler viewHandler) {
		this.viewHandler = viewHandler;
	}

	@Override
	public synchronized void connect(boolean showErrorDialog) throws DatabaseConfigurationException, SQLException {
		if (!dbPool.isConnected()) {
			viewHandler.commitConnectionDetails();
			viewHandler.printConnectionState(ConnectionStateEnum.INIT_CONNECT);

			try {
				dbPool.connect(config);
			} catch (DatabaseConfigurationException e) {
				viewHandler.printError(ConnectionStateEnum.CONNECT_ERROR, e, showErrorDialog);
				throw e;
			} catch (SQLException e) {
				viewHandler.printError(ConnectionStateEnum.CONNECT_ERROR, e, showErrorDialog);
				throw e;
			}

			viewHandler.printConnectionState(ConnectionStateEnum.FINISH_CONNECT);
		}
	}

	@Override
	public void disconnect(boolean showErrorDialog) throws SQLException {
		if (dbPool.isConnected()) {
			viewHandler.printConnectionState(ConnectionStateEnum.INIT_DISCONNECT);

			try {
				dbPool.disconnect();
			} catch (SQLException e) {
				viewHandler.printError(ConnectionStateEnum.DISCONNECT_ERROR, e, showErrorDialog);
				throw e;
			}

			viewHandler.printConnectionState(ConnectionStateEnum.FINISH_DISCONNECT);
		}
	}

	@Override
	public void forceDisconnect() {
		dbPool.forceDisconnect();
	}

	public boolean isConnected() {
		return dbPool.isConnected();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dbPool.getConnection();
	}

	@Override
	public boolean isIndexEnabled(String tableName, String columnName) throws SQLException {
		return DBUtil.isIndexed(tableName, columnName);
	}

	@Override
	public boolean existsWorkspace(String workspaceName) {
		return dbPool.existsWorkspace(new Workspace(workspaceName));
	}

	@Override
	public boolean gotoWorkspace(Connection conn, String workspaceName, String timestamp) throws SQLException {
		return dbPool.gotoWorkspace(conn, new Workspace(workspaceName, timestamp));
	}

	@Override
	public boolean gotoWorkspace(Connection conn, String workspaceName) throws SQLException {
		return gotoWorkspace(conn, workspaceName, null);
	}

	@Override
	public DatabaseConnectionDetails getActiveConnectionDetails() {
		return dbPool.getActiveConnection();
	}

	@Override
	public DatabaseMetaData getActiveConnectionMetaData() {
		return dbPool.getActiveConnectionMetaData();
	}

	@Override
	public List<DatabaseConnectionDetails> getConnectionDetails() {
		ArrayList<DatabaseConnectionDetails> tmp = new ArrayList<DatabaseConnectionDetails>();
		for (DBConnection conn : config.getProject().getDatabase().getConnections())
			tmp.add(conn);
		
		return tmp;
	}

	@Override
	public List<DatabaseSrs> getDatabaseSrs() {
		return config.getProject().getDatabase().getReferenceSystems();
	}

}
