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
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventHandlerContainerQueue {
	private ConcurrentLinkedQueue<EventHandlerContainer> containerQueue;

	public EventHandlerContainerQueue() {
		containerQueue = new ConcurrentLinkedQueue<EventHandlerContainer>();
	}

	public void addEventHandler(EventHandler handler, boolean autoRemove) {
		EventHandlerContainer container = new EventHandlerContainer(handler, autoRemove);
		containerQueue.add(container);
	}

	public void addEventHandler(EventHandler handler) {
		addEventHandler(handler, false);
	}

	public boolean removeEventHandler(EventHandler handler) {
		if (handler != null) {
			for (Iterator<EventHandlerContainer> iter = containerQueue.iterator(); iter.hasNext(); ) {
				EventHandlerContainer container = iter.next();

				if (handler == container.getEventHandler()) {
					containerQueue.remove(container);
					return true;
				}
			}
		}

		return false;
	}

	protected Event propagate(Event event) {
		Iterator<EventHandlerContainer> iter = containerQueue.iterator();
		
		while (iter.hasNext()) {
			EventHandlerContainer container = iter.next();
			EventHandler handler = container.getEventHandler();
			
			// since we deal with weak references, check whether
			// handler is null and remove its container in this case
			if (handler == null) {
				iter.remove();
				continue;
			}
			
			try {
				handler.handleEvent(event);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			
			if (container.isAutoRemove())
				iter.remove();
			
			if (event.isCancelled())
				break;
		}

		return event;
	}

	public void clear() {
		containerQueue.clear();
	}

	public Iterator<EventHandlerContainer> iterator() {
		return containerQueue.iterator();
	}
	
	public List<EventHandler> getHandlers() {
		List<EventHandler> handlers = new ArrayList<EventHandler>();		
		Iterator<EventHandlerContainer> iter = containerQueue.iterator();
		
		while (iter.hasNext()) {
			EventHandlerContainer container = iter.next();
			EventHandler handler = container.getEventHandler();
			
			// since we deal with weak references, check whether
			// handler is null and remove its container in this case
			if (handler == null) {
				iter.remove();
				continue;
			}
			
			handlers.add(handler);
		}

		return handlers;
	}
}
