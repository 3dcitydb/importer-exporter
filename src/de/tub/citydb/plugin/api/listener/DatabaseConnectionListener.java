package de.tub.citydb.plugin.api.listener;

public interface DatabaseConnectionListener {
	public void databaseConnected();
	public void databaseDisconnected();
}
