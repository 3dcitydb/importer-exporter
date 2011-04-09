package de.tub.citydb.api.database;

public interface DatabaseConnectionListener {
	public void connectionStateChange(boolean wasConnected, boolean isConnected);
}
