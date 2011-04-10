package de.tub.citydb.api.event.common;

public interface DatabaseConnectionStateEvent {
	public boolean isConnected();
	public boolean wasConnected();
}
