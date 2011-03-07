package de.tub.citydb.gui;

import javax.swing.JPanel;

import de.tub.citydb.config.Config;

public abstract class PrefPanelBase extends JPanel {
	
	private boolean modified;
	protected Config config;
	
	public PrefPanelBase(Config inpConfig) {
		super();
		config = inpConfig;
	}
	
	public void setModified() {
		modified = true;
	}
	
	public void setModified(boolean m) {
		modified = m;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public abstract void setSettings();
	
	public abstract void loadSettings();
	
	public abstract void doTranslation();
	
	public void resetSettings() {
		Config defaultConfig = new Config();
		Config actualConfig = this.config;
		this.config = defaultConfig;
		loadSettings();
		this.config = actualConfig;
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
