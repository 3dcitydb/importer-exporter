package de.tub.citydb.api.event.global;

import java.util.HashMap;
import java.util.Map;

import de.tub.citydb.api.event.Event;

public final class GenericEvent extends Event {
	private final String id;
	private final HashMap<String, Object> properties;
	
	public GenericEvent(String id, Object source) {
		super(GlobalEvents.GENERIC_EVENT, source);
		this.id = id;
		properties = new HashMap<String, Object>();
	}
	
	public GenericEvent(String id, Map<String, Object> properties, Object source) {
		super(GlobalEvents.GENERIC_EVENT, source);
		this.id = id;
		this.properties = new HashMap<String, Object>(properties);
	}
	
	public String getId() {
		return id;
	}
	
	public boolean hasProperties() {
		return !properties.isEmpty();
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public Object getProperty(String key) {
		return hasProperties() ? properties.get(key) : null;
	}
}
