package de.tub.citydb.api.plugin.api;

import java.util.Locale;

public interface Plugin {
	public void init();
	public void shutdown();
	public void switchLocale(Locale locale);		
}
