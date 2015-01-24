package org.citydb.modules.common.event;

import org.citydb.api.event.Event;

public class PropertyChangeEvent extends Event {
	private final String propertyName;
	private final Object oldValue;
	private final Object newValue;
	
	public PropertyChangeEvent(String propertyName, Object oldValue, Object newValue, Object source) {
		super(EventType.PROPERTY_CHANGE_EVENT, GLOBAL_CHANNEL, source);
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}
	
}
