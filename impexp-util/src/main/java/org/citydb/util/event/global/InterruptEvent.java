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
package org.citydb.util.event.global;

import org.citydb.config.project.global.LogLevel;
import org.citydb.util.event.Event;

public class InterruptEvent extends Event {
	private final String logMessage;
	private final LogLevel logLevelType;
	private final Throwable cause;
	
	public InterruptEvent(String logMessage, LogLevel logLevelType, Throwable cause, Object channel, String label) {
		super(EventType.INTERRUPT, channel, label);
		this.logMessage = logMessage;
		this.logLevelType = logLevelType;
		this.cause = cause;
	}

	public InterruptEvent(String logMessage, LogLevel logLevelType, Throwable cause, Object channel) {
		this(logMessage, logLevelType, cause, channel, null);
	}

	public InterruptEvent(String logMessage, LogLevel logLevelType, Object channel) {
		this(logMessage, logLevelType, null, channel, null);
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
