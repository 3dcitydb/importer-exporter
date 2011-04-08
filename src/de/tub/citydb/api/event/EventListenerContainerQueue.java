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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventListenerContainerQueue {
	private ConcurrentLinkedQueue<EventListenerContainer> containerQueue;

	public EventListenerContainerQueue() {
		containerQueue = new ConcurrentLinkedQueue<EventListenerContainer>();
	}

	public void addListener(EventListener listener, boolean autoRemove) {
		EventListenerContainer container = new EventListenerContainer(listener, autoRemove);
		containerQueue.add(container);
	}

	public void addListener(EventListener listener) {
		addListener(listener, false);
	}

	public boolean removeListener(EventListener listener) {
		if (listener != null) {
			for (Iterator<EventListenerContainer> iter = containerQueue.iterator(); iter.hasNext(); ) {
				EventListenerContainer container = iter.next();

				if (listener.equals(container.getListener())) {
					containerQueue.remove(container);
					return true;
				}
			}
		}

		return false;
	}

	protected Event propagate(Event e) throws Exception {
		ArrayList<EventListenerContainer> removeList = new ArrayList<EventListenerContainer>();

		for (EventListenerContainer container : containerQueue) {
			EventListener listener = container.getListener();
			
			// since we deal with weak references, check whether
			// listener is null and remove its container in this case
			if (listener != null)
				listener.handleEvent(e);
			else
				removeList.add(container);

			if (container.isAutoRemove())
				removeList.add(container);

			if (e.isCancelled())
				break;
		}

		containerQueue.removeAll(removeList);
		return e;
	}

	public void clear() {
		containerQueue.clear();
	}

	public Iterator<EventListenerContainer> iterator() {
		return containerQueue.iterator();
	}
}
