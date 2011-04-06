package de.tub.citydb.plugin.api.extensions.view;

import java.awt.Component;

import javax.swing.Icon;

public interface View {
	public String getTitle();
	public Component getViewComponent();
	public String getToolTip();
	public Icon getIcon();
}
