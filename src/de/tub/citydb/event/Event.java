package de.tub.citydb.event;

public abstract class Event {
	private EventType eventType;
	private boolean cancelled;

	public Event(EventType eventType) {
		this.eventType = eventType;
		cancelled = false;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public EventType getEventType() {
		return eventType;
	}
}
