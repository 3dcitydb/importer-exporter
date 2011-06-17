package de.tub.citydb.event;

import java.util.Locale;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.event.global.SwitchLocaleEvent;

public class SwitchLocaleEventImpl extends Event implements SwitchLocaleEvent {
	private final Locale locale;
	
	public SwitchLocaleEventImpl(Locale locale, Object source) {
		super(GlobalEvents.SWITCH_LOCALE, source);
		this.locale = locale;
	}
	
	@Override
	public Locale getLocale() {
		return locale;
	}

}
