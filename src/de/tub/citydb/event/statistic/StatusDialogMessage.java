package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class StatusDialogMessage extends Event {
	private String message;
	
	public StatusDialogMessage(String message) {
		super(EventType.StatusDialogMessage);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
