package de.tub.citydb.api.plugin;

import java.util.Locale;

public interface Plugin {
	public void init(Locale locale);
	public void shutdown();
	public void switchLocale(Locale locale);		
}
