package de.tub.citydb.components.database.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.components.database.DatabasePlugin;
import de.tub.citydb.components.database.gui.view.components.DatabasePanel;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.plugin.api.controller.DatabaseController;
import de.tub.citydb.plugin.api.data.database.DatabaseConnectionDetails;
import de.tub.citydb.plugin.api.exception.DatabaseConfigurationException;
import de.tub.citydb.plugin.api.listener.DatabaseConnectionListener;

public class DatabaseControllerImpl implements DatabaseController, PropertyChangeListener {
	private final DatabasePlugin plugin;
	private final Config config;
	private final DBConnectionPool dbPool;
	private List<DatabaseConnectionListener> listeners;
	
	public DatabaseControllerImpl(Config config, DatabasePlugin plugin) {
		this.plugin = plugin;
		this.config = config;
		
		dbPool = DBConnectionPool.getInstance();
		dbPool.addPropertyChangeListener(DBConnectionPool.PROPERTY_DB_IS_CONNECTED, this);
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

	@Override
	public synchronized void addDatabaseConnectionListener(DatabaseConnectionListener listener) {
		if (listeners == null)
			listeners = new ArrayList<DatabaseConnectionListener>();
		
		listeners.add(listener);
	}

	@Override
	public synchronized void removeDatabaseConnectionListener(DatabaseConnectionListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (listeners != null && evt.getPropertyName().equals(DBConnectionPool.PROPERTY_DB_IS_CONNECTED)) {
			boolean isConnected = (Boolean)evt.getNewValue();
					
			for (DatabaseConnectionListener listener : listeners) {
				if (isConnected)
					listener.databaseConnected();
				else
					listener.databaseDisconnected();
			}
				
		}
	}

}
