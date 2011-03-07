package de.tub.citydb.event.concurrent;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;
import de.tub.citydb.log.LogLevelType;

public class InterruptEvent extends Event {
	private InterruptEnum interruptType;
	private String logMessage;
	private LogLevelType logLevelType;
	
	public InterruptEvent(InterruptEnum interruptType) {
		super(EventType.Interrupt);
		this.interruptType = interruptType;
	}
	
	public InterruptEvent(InterruptEnum interruptType, String logMessage, LogLevelType logLevelType) {
		this(interruptType);
		this.logMessage = logMessage;
		this.logLevelType = logLevelType;
	}

	public InterruptEnum getInterruptType() {
		return interruptType;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public LogLevelType getLogLevelType() {
		return logLevelType;
	}
	
}
