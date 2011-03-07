package de.tub.citydb.event;

public class EventListenerContainer {

	private EventListener listener = null;
	private boolean autoRemove = false;

	public EventListenerContainer(EventListener listener) {
		this.listener = listener;
	}

	public EventListenerContainer(EventListener listener, boolean autoRemove) {
		this(listener);
		this.autoRemove = autoRemove;
	}

	public boolean isAutoRemove() {
		return autoRemove;
	}

	public void setAutoRemove(boolean autoRemove) {
		this.autoRemove = autoRemove;
	}

	public EventListener getListener() {
		return listener;
	}
}
