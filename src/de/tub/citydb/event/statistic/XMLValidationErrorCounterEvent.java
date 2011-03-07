package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class XMLValidationErrorCounterEvent extends Event {
	private long counter = 0;

	public XMLValidationErrorCounterEvent(int counter) {
		super(EventType.ValidationErrorCounter);
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}
}
