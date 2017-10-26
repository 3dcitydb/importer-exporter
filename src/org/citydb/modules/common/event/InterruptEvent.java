/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.modules.common.event;

import org.citydb.api.event.Event;
import org.citydb.api.log.LogLevel;

public class InterruptEvent extends Event {
	private final InterruptReason interruptType;
	private final String logMessage;
	private final LogLevel logLevelType;
	private final Throwable cause;
	
	public InterruptEvent(InterruptReason interruptType, String logMessage, LogLevel logLevelType, Object channel, Object source) {
		this(interruptType, logMessage, logLevelType, null, channel, source);
	}
	
	public InterruptEvent(InterruptReason interruptType, String logMessage, LogLevel logLevelType, Throwable cause, Object channel, Object source) {
		super(EventType.INTERRUPT, channel, source);
		this.interruptType = interruptType;
		this.logMessage = logMessage;
		this.logLevelType = logLevelType;
		this.cause = cause;
	}

	public InterruptReason getInterruptReason() {
		return interruptType;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public LogLevel getLogLevelType() {
		return logLevelType;
	}

	public Throwable getCause() {
		return cause;
	}
	
}
