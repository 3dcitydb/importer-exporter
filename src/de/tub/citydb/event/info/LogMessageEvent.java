package de.tub.citydb.event.info;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class LogMessageEvent extends Event {
	private String message;
	private LogMessageEnum messageType;

	public LogMessageEvent(String message, LogMessageEnum messageType) {
		super(EventType.LogMessage);
		this.message = message;
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public LogMessageEnum getMessageType() {
		return messageType;
	}

}
