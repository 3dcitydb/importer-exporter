package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class StatusDialogTitle extends Event {
	private String title;
	
	public StatusDialogTitle(String title) {
		super(EventType.StatusDialogTitle);
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
}
