/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.util.concurrent.ConcurrentLinkedQueue;

import de.tub.citydb.api.controller.LogController;
import de.tub.citydb.api.registry.ObjectRegistry;

public class EventHandlerContainerQueue {
	private final LogController LOG;
	private ConcurrentLinkedQueue<EventHandlerContainer> containerQueue;

	public EventHandlerContainerQueue() {
		LOG = ObjectRegistry.getInstance().getLogController();
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
				LOG.error("The following error occurred while processing an event:");
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
