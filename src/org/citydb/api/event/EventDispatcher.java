/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
