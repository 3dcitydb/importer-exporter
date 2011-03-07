package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class TopLevelFeatureCounterEvent extends Event {
	private long counter = 0;

	public TopLevelFeatureCounterEvent(int counter) {
		super(EventType.TopLevelFeatureCounter);
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}
}
