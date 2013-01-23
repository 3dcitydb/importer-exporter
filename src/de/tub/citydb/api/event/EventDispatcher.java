/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.api.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.api.concurrent.SingleWorkerPool;

public class EventDispatcher {
	private SingleWorkerPool<Event> eventDispatcherThread;
	private ConcurrentHashMap<Enum<?>, EventHandlerContainerQueue> containerQueueMap;
	private ReentrantLock mainLock;

	public EventDispatcher(int eventQueueSize) {
		containerQueueMap = new ConcurrentHashMap<Enum<?>, EventHandlerContainerQueue>();
		eventDispatcherThread = new SingleWorkerPool<Event>(
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
}
