package de.tub.citydb.plugin;

import de.tub.citydb.api.plugin.Plugin;

public interface InternalPlugin extends Plugin {
	public void loadSettings();
	public void setSettings();
}
