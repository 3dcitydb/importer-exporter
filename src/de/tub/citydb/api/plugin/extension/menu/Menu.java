package de.tub.citydb.api.plugin.extension.menu;

import javax.swing.Icon;
import javax.swing.JMenu;

public abstract class Menu {
	public abstract String getLocalizedTitle();
	public abstract JMenu getMenuComponent();
	public abstract Icon getIcon();
	public abstract int getMnemonicIndex();
}
