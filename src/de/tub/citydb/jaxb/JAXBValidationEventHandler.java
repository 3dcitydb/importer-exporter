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
