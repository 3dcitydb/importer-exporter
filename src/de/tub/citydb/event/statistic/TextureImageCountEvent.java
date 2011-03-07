package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class TextureImageCountEvent extends Event {
	private long counter = 0;

	public TextureImageCountEvent(int counter) {
		super(EventType.TextureImageCounter);
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}
}
