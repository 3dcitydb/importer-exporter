package de.tub.citydb.gui.panel.settings;

import javax.swing.JPanel;

import de.tub.citydb.config.Config;

@SuppressWarnings("serial")
public abstract class PrefPanelBase extends JPanel {
	protected Config config;
	
	public PrefPanelBase(Config config) {
		this.config = config;
	}

	public abstract boolean isModified();
	
	public abstract void setSettings();
	
	public abstract void loadSettings();
	
	public abstract void doTranslation();
	
	public void resetSettings() {
		Config tmp = config;
		config = new Config();
		loadSettings();
		config = tmp;
	}

}
