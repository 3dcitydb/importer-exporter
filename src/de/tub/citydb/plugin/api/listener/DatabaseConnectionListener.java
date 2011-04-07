package de.tub.citydb.plugin.api.listener;

public interface DatabaseConnectionListener {
	public void connectionStateChange(boolean wasConnected, boolean isConnected);
}
