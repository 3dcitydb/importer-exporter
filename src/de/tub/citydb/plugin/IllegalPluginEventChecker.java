package de.tub.citydb.plugin;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.common.ApplicationEvent;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.gui.ImpExpGui;

public class IllegalPluginEventChecker implements EventHandler {
	private static IllegalPluginEventChecker instance;
	
	private IllegalPluginEventChecker() {
		// just to thwart instantiation
	}
	
	public static synchronized IllegalPluginEventChecker getInstance() {
		if (instance == null)
			instance = new IllegalPluginEventChecker();
		
		return instance;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getEventType() == ApplicationEvent.DATABASE_CONNECTION_STATE && event.getSource() != DBConnectionPool.getInstance())
			throw new IllegalArgumentException("Events of type " + ApplicationEvent.DATABASE_CONNECTION_STATE + " may not be triggered by plugins.");

		else if (event.getEventType() == ApplicationEvent.SWITCH_LOCALE && !(event.getSource() instanceof ImpExpGui))
			throw new IllegalArgumentException("Events of type " + ApplicationEvent.SWITCH_LOCALE + " may not be triggered by plugins.");
	}
	
}
