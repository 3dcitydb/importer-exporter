/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.api.concurrent.SingleWorkerPool;

public class EventDispatcher {
	private SingleWorkerPool<Event> eventDispatcherThread;
	private ConcurrentHashMap<Enum<?>, EventListenerContainerQueue> containerQueueMap;
	private ReentrantLock mainLock;

	public EventDispatcher(int eventQueueSize) {
		containerQueueMap = new ConcurrentHashMap<Enum<?>, EventListenerContainerQueue>();
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

	public void addListener(Enum<?> type, EventListener listener, boolean autoRemove) {
		containerQueueMap.putIfAbsent(type, new EventListenerContainerQueue());

		EventListenerContainerQueue containerQueue = containerQueueMap.get(type);
		containerQueue.addListener(listener, autoRemove);
	}


	public void addListener(Enum<?> type, EventListener listener) {
		addListener(type, listener, false);
	}

	public boolean removeListener(Enum<?> type, EventListener listener) {
		if (!containerQueueMap.containsKey(type))
			return false;

		EventListenerContainerQueue containerQueue = containerQueueMap.get(type);
		return containerQueue.removeListener(listener);
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
			if (containerQueueMap.containsKey(e.getEventType())) {
				EventListenerContainerQueue containerQueue = containerQueueMap.get(e.getEventType());
				containerQueue.propagate(e);
			}

			return e;
		} finally {
			lock.unlock();
		}
	}

	public Enum<?>[] getRegisteredEventTypes() {
		Enum<?>[] types = containerQueueMap.keySet().toArray(new Enum<?>[] {});
		return types;
	}

	public void reset() {
		for (Iterator<EventListenerContainerQueue> iter = containerQueueMap.values().iterator(); iter.hasNext(); ) {
			EventListenerContainerQueue containerQueue = iter.next();
			containerQueue.clear();
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
