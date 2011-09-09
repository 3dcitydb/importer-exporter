package de.tub.citydb.api.gui;

import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

public interface StandardEditingPopupMenuDecorator {
	public void decorate(JTextComponent... components);
	public JPopupMenu decorate(JTextComponent component);
}
