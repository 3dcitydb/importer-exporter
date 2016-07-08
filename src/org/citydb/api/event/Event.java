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

import java.lang.ref.WeakReference;

public abstract class Event {
	public static Object GLOBAL_CHANNEL = new Object();
	
	private final Enum<?> eventType;
	private final WeakReference<Object> source;
	private final WeakReference<Object> channel;
	private boolean cancelled;

	public Event(Enum<?> eventType, Object channel, Object source) {
		if (eventType == null)
			throw new IllegalArgumentException("The type of an event may not be null.");
			
		if (source == null)
			throw new IllegalArgumentException("The source of an event may not be null.");
		
		this.eventType = eventType;
		this.source = new WeakReference<Object>(source);
		this.channel = new WeakReference<Object>(channel);
		cancelled = false;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Enum<?> getEventType() {
		return eventType;
	}

	public Object getSource() {
		return source.get();
	}

	public Object getChannel() {
		return channel.get();
	}
	
}
