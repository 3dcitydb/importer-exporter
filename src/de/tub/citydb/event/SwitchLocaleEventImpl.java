package de.tub.citydb.event;

import java.util.Locale;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.common.ApplicationEvent;
import de.tub.citydb.api.event.common.SwitchLocaleEvent;

public class SwitchLocaleEventImpl extends Event implements SwitchLocaleEvent {
	private final Locale locale;
	
	public SwitchLocaleEventImpl(Locale locale, Object source) {
		super(ApplicationEvent.SWITCH_LOCALE, source);
		this.locale = locale;
	}
	
	@Override
	public Locale getLocale() {
		return locale;
	}

}
