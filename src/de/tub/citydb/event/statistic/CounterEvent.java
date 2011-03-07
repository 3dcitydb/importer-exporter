package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class CounterEvent extends Event {
	private long counter = 0;
	private CounterType type;
	
	public CounterEvent(CounterType type, int counter) {
		super(EventType.Counter);
		this.type = type;
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}

	public CounterType getType() {
		return type;
	}
	
}
