package de.tub.citydb.event;

public interface EventListener {
	public void handleEvent(Event e) throws Exception;
}
