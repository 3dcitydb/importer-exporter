package de.tub.citydb.plugins.matching_merging.gui.preferences;

import de.tub.citydb.api.plugin.extension.preferences.PreferencesEntry;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesEvent;

public class DefaultPreferencesEntry extends PreferencesEntry {
	public AbstractPreferencesComponent component;
	
	public DefaultPreferencesEntry(AbstractPreferencesComponent component) {
		this.component = component;
	}
	
	@Override
	public boolean isModified() {
		return component.isModified();
	}

	@Override
	public boolean handleEvent(PreferencesEvent event) {
		switch (event) {
		case APPLY_SETTINGS:
			component.setSettings();
			break;
		case RESTORE_SETTINGS:
			component.loadSettings();
			break;
		case SET_DEFAULT_SETTINGS:
			component.resetSettings();
			break;
		}
		
		return true;
	}

	@Override
	public String getLocalizedTitle() {
		return component.getTitle();
	}

	@Override
	public final AbstractPreferencesComponent getViewComponent() {
		return component;
	}
	
	@Override
	public final void addChildEntry(PreferencesEntry child) {
		if (!(child instanceof DefaultPreferencesEntry))
			throw new IllegalArgumentException("Only DefaultPreferencesEntry instances are allowed as child entries.");
		
		super.addChildEntry(child);
	}

	public void switchLocale() {
		component.switchLocale();
	}

}
