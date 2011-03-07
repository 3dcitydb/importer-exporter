package de.tub.citydb.gui.panel.settings;

import javax.swing.JPanel;

import de.tub.citydb.config.Config;

public abstract class PrefPanelBase extends JPanel {
	private boolean modified;
	protected Config config;
	
	public PrefPanelBase(Config config) {
		this.config = config;
	}

	public boolean isModified() {
		return modified;
	}
	
	public abstract void setSettings();
	
	public abstract void loadSettings();
	
	public abstract void doTranslation();
	
	public void resetSettings() {
		Config tmp = config;
		config = new Config();
		loadSettings();
		config = tmp;
	}
	
	public void saveChanges() {
		setSettings();
		modified = false;
	}
	
	public void noSaveChanges() {
		loadSettings();
		modified = false;
	}

}
