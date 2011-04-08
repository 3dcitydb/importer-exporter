package de.tub.citydb.api.plugin.extension.menu;

import javax.swing.Icon;
import javax.swing.JMenu;

public interface Menu {
	public String getTitle();
	public JMenu getMenuComponent();
	public Icon getIcon();
	public int getMnemonicIndex();
}
