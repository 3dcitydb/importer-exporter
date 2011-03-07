package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class TextureImageCounterEvent extends Event {
	private long counter = 0;

	public TextureImageCounterEvent(int counter) {
		super(EventType.TextureImageCounter);
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}
}
