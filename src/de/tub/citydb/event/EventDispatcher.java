package de.tub.citydb.event;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.concurrent.SingleWorkerPool;

public class EventDispatcher {
	private SingleWorkerPool<Event> eventDispatcherThread;
	private ConcurrentHashMap<EventType, EventContainerList> listeners;
	private ReentrantLock mainLock;

	public EventDispatcher() {
		listeners = new ConcurrentHashMap<EventType, EventContainerList>();
		eventDispatcherThread = new SingleWorkerPool<Event>(
				new EventWorkerFactory(this),
				100,
				false);

		eventDispatcherThread.prestartCoreWorkers();
		mainLock = new ReentrantLock();
	}

	public int addListener(EventType type, EventListener listener, boolean autoRemove) {
		listeners.putIfAbsent(type, new EventContainerList());

		EventContainerList list = listeners.get(type);
		return list.addListener(listener, autoRemove);
	}


	public int addListener(EventType type, EventListener listener) {
		return addListener(type, listener, false);
	}

	public EventListener removeListener(EventType type, EventListener listener) {
		if (!listeners.containsKey(type))
			return null;

		EventContainerList list = listeners.get(type);
		EventListenerContainer container = list.removeListener(listener);

		if (container != null)
			return container.getListener();

		return null;
	}

	public void triggerEvent(Event e) {
		eventDispatcherThread.addWork(e);
	}

	public Event triggerSyncEvent(Event e) throws Exception {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return propagate(e);
		} finally {
			lock.unlock();
		}
	}

	protected Event propagate(Event e) throws Exception {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (listeners.containsKey(e.getEventType())) {
				EventContainerList list = listeners.get(e.getEventType());
				list.propagate(e);
			}

			return e;
		} finally {
			lock.unlock();
		}
	}

	public EventType[] getRegisteredEventTypes() {
		EventType[] types = listeners.keySet().toArray(new EventType[] {});

		return types;
	}

	public void reset() {
		for (Iterator<EventContainerList> iter = listeners.values().iterator(); iter.hasNext(); ) {
			EventContainerList list = iter.next();
			list.clear();
		}
	}

	public void shutdown() {
		eventDispatcherThread.shutdown();
	}
	
	public void shutdownAndWait() throws InterruptedException {
		eventDispatcherThread.shutdownAndWait();
	}
	
	public void join() throws InterruptedException {
		eventDispatcherThread.join();
	}
}
