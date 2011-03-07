package de.tub.citydb.event.statistic;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class StatusDialogProgressBar extends Event {
	private int currentValue;
	private int maxValue;
	private boolean setIntermediate = false;
	
	public StatusDialogProgressBar(int currentValue, int maxValue) {
		super(EventType.StatusDialogProgressBar);
		this.currentValue = currentValue;
		this.maxValue = maxValue;
	}
	
	public StatusDialogProgressBar(boolean setIntermediate) {
		super(EventType.StatusDialogProgressBar);
		this.setIntermediate = setIntermediate;
	}
	
	public int getCurrentValue() {
		return currentValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public boolean isSetIntermediate() {
		return setIntermediate;
	}
		
}
