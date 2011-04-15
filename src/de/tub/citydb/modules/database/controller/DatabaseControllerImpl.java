package de.tub.citydb.modules.database.controller;

import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseConnectionDetails;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.modules.database.DatabasePlugin;
import de.tub.citydb.modules.database.gui.view.DatabasePanel;

public class DatabaseControllerImpl implements DatabaseController {
	private final DatabasePlugin plugin;
	private final Config config;
	private final DBConnectionPool dbPool;
	
	public DatabaseControllerImpl(Config config, DatabasePlugin plugin) {
		this.plugin = plugin;
		this.config = config;
		
		dbPool = DBConnectionPool.getInstance();
	}	

	@Override
	public void connect() throws DatabaseConfigurationException, SQLException {
		((DatabasePanel)plugin.getView().getViewComponent()).setSettings();
		dbPool.connect(config.getProject().getDatabase().getActiveConnection());
	}

	@Override
	public void disconnect() throws SQLException {
		dbPool.disconnect();
	}

	public boolean isConnected() {
		return dbPool.isConnected();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dbPool.getConnection();
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
		return dbPool.getActiveConnection().toPluginObject();
	}
	
}
