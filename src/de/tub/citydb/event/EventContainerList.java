package de.tub.citydb.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventContainerList {
	private ConcurrentLinkedQueue<EventListenerContainer> listeners;

	public EventContainerList() {
		listeners = new ConcurrentLinkedQueue<EventListenerContainer>();
	}

	public int addListener(EventListener listener, boolean autoRemove) {
		EventListenerContainer container = new EventListenerContainer(listener, autoRemove);
		listeners.add(container);

		return listeners.size();
	}

	public int addListener(EventListener listener) {
		return addListener(listener, false);
	}

	public EventListenerContainer removeListener(EventListener listener) {
		for (Iterator<EventListenerContainer> iter = listeners.iterator(); iter.hasNext(); ) {
			EventListenerContainer container = iter.next();

			if (container.getListener().equals(listener)) {
				listeners.remove(container);
				return container;
			}
		}

		return null;
	}

	protected Event propagate(Event e) throws Exception {
		ArrayList<EventListener> removeList = new ArrayList<EventListener>();

		for (EventListenerContainer container : listeners) {
			container.getListener().handleEvent(e);

			if (container.isAutoRemove())
				removeList.add(container.getListener());

			if (e.isCancelled())
				break;
		}

		for (EventListener listener : removeList)
			removeListener(listener);

		return e;
	}

	public int size() {
		return listeners.size();
	}

	public void clear() {
		listeners.clear();
	}

	public Iterator<EventListenerContainer> iterator() {
		return listeners.iterator();
	}
}
