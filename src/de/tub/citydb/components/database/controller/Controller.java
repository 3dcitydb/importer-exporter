package de.tub.citydb.components.database.controller;

import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.components.database.DatabasePlugin;
import de.tub.citydb.components.database.gui.view.components.DatabasePanel;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.plugin.api.controller.DatabaseController;
import de.tub.citydb.plugin.api.data.database.DatabaseConnectionDetails;

public class Controller implements DatabaseController {
	private final DatabasePlugin plugin;
	private final DBConnectionPool dbPool;
	
	public Controller(DatabasePlugin plugin) {
		this.plugin = plugin;
		dbPool = DBConnectionPool.getInstance();
	}
	
	@Override
	public boolean connect() {
		// we make use of a gui component here because we want to automatically
		// print error messages in the gui
		return ((DatabasePanel)plugin.getView().getViewComponent()).connect();
	}

	@Override
	public boolean disconnect() {
		// we make use of a gui component here because we want to automatically
		// print error messages in the gui
		return ((DatabasePanel)plugin.getView().getViewComponent()).disconnect();
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
