package de.tub.citydb.plugin.api.extension.menu;

import javax.swing.Icon;
import javax.swing.JMenuItem;

public interface Menu {
	public String getTitle();
	public JMenuItem getMenuItem();
	public Icon getIcon();
	public int getMnemonicIndex();
}
