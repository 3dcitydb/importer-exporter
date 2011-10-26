package de.tub.citydb.plugin;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.database.DatabaseConnectionPool;
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
		if (event.getEventType() == GlobalEvents.DATABASE_CONNECTION_STATE && event.getSource() != DatabaseConnectionPool.getInstance())
			throw new IllegalArgumentException("Events of type " + GlobalEvents.DATABASE_CONNECTION_STATE + " may not be triggered by plugins.");

		else if (event.getEventType() == GlobalEvents.SWITCH_LOCALE && !(event.getSource() instanceof ImpExpGui))
			throw new IllegalArgumentException("Events of type " + GlobalEvents.SWITCH_LOCALE + " may not be triggered by plugins.");
	}
	
}
