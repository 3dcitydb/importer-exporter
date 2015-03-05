/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.api.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.SingleWorkerPool;

public class EventDispatcher {
	private SingleWorkerPool<Event> eventDispatcherThread;
	private ConcurrentHashMap<Enum<?>, EventHandlerContainerQueue> containerQueueMap;
	private ReentrantLock mainLock;

	public EventDispatcher(int eventQueueSize) {
		containerQueueMap = new ConcurrentHashMap<Enum<?>, EventHandlerContainerQueue>();
		eventDispatcherThread = new SingleWorkerPool<Event>(
				"event_dispatcher",
				new EventWorkerFactory(this),
				eventQueueSize,
				true);

		eventDispatcherThread.prestartCoreWorkers();
		mainLock = new ReentrantLock();
	}

	public EventDispatcher() {
		this(100);
	}

	public void addEventHandler(Enum<?> type, EventHandler handler, boolean autoRemove) {
		containerQueueMap.putIfAbsent(type, new EventHandlerContainerQueue());

		EventHandlerContainerQueue containerQueue = containerQueueMap.get(type);
		containerQueue.addEventHandler(handler, autoRemove);
	}


	public void addEventHandler(Enum<?> type, EventHandler handler) {
		addEventHandler(type, handler, false);
	}

	public boolean removeEventHandler(Enum<?> type, EventHandler handler) {
		if (!containerQueueMap.containsKey(type))
			return false;

		EventHandlerContainerQueue containerQueue = containerQueueMap.get(type);
		return containerQueue.removeEventHandler(handler);
	}

	public void removeEventHandler(EventHandler handler) {
		for (EventHandlerContainerQueue containerQueue : containerQueueMap.values())
			containerQueue.removeEventHandler(handler);	
	}

	public void triggerEvent(Event event) {
		eventDispatcherThread.addWork(event);
	}

	public Event triggerSyncEvent(Event event) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return propagate(event);
		} finally {
			lock.unlock();
		}
	}

	protected Event propagate(Event event) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (containerQueueMap.containsKey(event.getEventType())) {
				EventHandlerContainerQueue containerQueue = containerQueueMap.get(event.getEventType());
				containerQueue.propagate(event);
			}

			return event;
		} finally {
			lock.unlock();
		}
	}

	public List<EventHandler> getRegisteredHandlers(Enum<?> type) {
		return containerQueueMap.containsKey(type) ?
				containerQueueMap.get(type).getHandlers() : new ArrayList<EventHandler>();
	}

	public void reset() {
		for (Iterator<EventHandlerContainerQueue> iter = containerQueueMap.values().iterator(); iter.hasNext(); ) {
			EventHandlerContainerQueue containerQueue = iter.next();
			containerQueue.clear();
		}
	}

	public void flushEvents() throws InterruptedException {
		eventDispatcherThread.join();
	}
	
	public void shutdown() {
		eventDispatcherThread.shutdown();
		containerQueueMap.clear();
	}
	
	public void shutdownNow() {
		eventDispatcherThread.shutdownNow();
		containerQueueMap.clear();
	}
	
	public void shutdownAndWait() throws InterruptedException {
		eventDispatcherThread.shutdownAndWait();
		containerQueueMap.clear();
	}
	
}
