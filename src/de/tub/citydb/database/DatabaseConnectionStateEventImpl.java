package de.tub.citydb.database;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.common.ApplicationEvent;
import de.tub.citydb.api.event.common.DatabaseConnectionStateEvent;

public final class DatabaseConnectionStateEventImpl extends Event implements DatabaseConnectionStateEvent {
	private final boolean wasConnected;
	private final boolean isConnected;
	
	protected DatabaseConnectionStateEventImpl(boolean wasConnected, boolean isConnected, Object source) {
		super(ApplicationEvent.DATABASE_CONNECTION_STATE, source);
		this.wasConnected = wasConnected;
		this.isConnected = isConnected;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public boolean wasConnected() {
		return wasConnected;
	}
	
}
