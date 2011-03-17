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
