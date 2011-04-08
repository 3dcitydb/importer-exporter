package de.tub.citydb.api.plugin.listener;

public interface DatabaseConnectionListener {
	public void connectionStateChange(boolean wasConnected, boolean isConnected);
}
