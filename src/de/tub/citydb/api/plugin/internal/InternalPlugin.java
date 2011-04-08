package de.tub.citydb.api.plugin.internal;

import de.tub.citydb.api.plugin.api.Plugin;

public interface InternalPlugin extends Plugin {
	public void loadSettings();
	public void setSettings();
}
