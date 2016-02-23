/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
