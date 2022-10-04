/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.util.event;

import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class Event {
	public static final Object GLOBAL_CHANNEL = new Object();
	
	private final Enum<?> eventType;
	private WeakReference<Object> channel;
	private boolean cancelled;

	public Event(Enum<?> eventType, Object channel) {
		this.eventType = Objects.requireNonNull(eventType, "The type of an event may not be null.");
		cancelled = false;
		setChannel(channel);
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

	public Object getChannel() {
		return channel.get();
	}

	public void setChannel(Object channel) {
		this.channel = new WeakReference<>(channel != null ? channel : GLOBAL_CHANNEL);
	}
}
