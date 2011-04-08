package de.tub.citydb.api.plugin.api.listener;

public interface DatabaseConnectionListener {
	public void connectionStateChange(boolean wasConnected, boolean isConnected);
}
