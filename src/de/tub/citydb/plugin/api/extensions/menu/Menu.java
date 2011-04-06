package de.tub.citydb.plugin.api.extensions.menu;

import javax.swing.Icon;
import javax.swing.JMenu;

public interface Menu {
	public String getTitle();
	public JMenu getPluginMenu();
	public Icon getIcon();
	public int getMnemonicIndex();
}
