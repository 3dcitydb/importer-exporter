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
package de.tub.citydb.jaxb;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;

public class JAXBValidationEventHandler implements ValidationEventHandler {    
	private final Logger LOG = Logger.getInstance();
	private final EventDispatcher eventDispatcher;
	private final boolean continueAfterEvent;
	
	private long lineNumber = -1;
	private long columnNumber = -1;
	private boolean hasEvents = false;

	public JAXBValidationEventHandler(EventDispatcher eventDispatcher, boolean continueAfterEvent) {
		this.eventDispatcher = eventDispatcher;
		this.continueAfterEvent = continueAfterEvent;
	}
	
	public JAXBValidationEventHandler(EventDispatcher eventDispatcher) {
		this(eventDispatcher, true);
	}

	public boolean handleEvent(ValidationEvent ve) {   
		hasEvents = true;
		
		if (!ve.getMessage().startsWith("cvc"))
			return true;

		StringBuilder msg = new StringBuilder();
		LogLevelType type;
		
		switch (ve.getSeverity()) {
		case ValidationEvent.FATAL_ERROR:
		case ValidationEvent.ERROR:
			msg.append("Invalid content");
			type = LogLevelType.ERROR;
			break;
		case ValidationEvent.WARNING:
			msg.append("Warning");
			type = LogLevelType.WARN;
			break;
		default:
			return continueAfterEvent;
		}

		if (lineNumber > 0)
			msg.append(" at [" + lineNumber + ", " + columnNumber + "]");

		msg.append(": ");
		msg.append(ve.getMessage());
		LOG.log(type, msg.toString());

		eventDispatcher.triggerEvent(new CounterEvent(CounterType.XML_VALIDATION_ERROR, 1));
		return continueAfterEvent;
	}

	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setColumnNumber(long columnNumber) {
		this.columnNumber = columnNumber;
	}

	public void reset() {
		lineNumber = columnNumber = -1;
		hasEvents = false;
	}

	public boolean hasEvents() {
		return hasEvents;
	}

}
