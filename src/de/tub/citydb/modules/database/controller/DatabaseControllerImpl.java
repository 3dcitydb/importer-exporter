package de.tub.citydb.modules.database.controller;

import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseConnectionDetails;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.modules.database.DatabasePlugin;
import de.tub.citydb.modules.database.gui.view.DatabasePanel;

public class DatabaseControllerImpl implements DatabaseController {
	private final DatabasePlugin plugin;
	private final DBConnectionPool dbPool;
	
	public DatabaseControllerImpl(DatabasePlugin plugin) {
		this.plugin = plugin;		
		dbPool = DBConnectionPool.getInstance();
	}	

	@Override
	public void connect(boolean showErrorDialog) throws DatabaseConfigurationException, SQLException {
		((DatabasePanel)plugin.getView().getViewComponent()).connect(showErrorDialog);
	}

	@Override
	public void disconnect(boolean showErrorDialog) throws SQLException {
		((DatabasePanel)plugin.getView().getViewComponent()).disconnect(showErrorDialog);
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
