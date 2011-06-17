package de.tub.citydb.api.event.global;

public interface DatabaseConnectionStateEvent {
	public boolean isConnected();
	public boolean wasConnected();
}
