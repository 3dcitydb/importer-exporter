package de.tub.citydb.api.plugin.api.extension.view;

import java.awt.Component;

import javax.swing.Icon;

public interface View {
	public String getTitle();
	public Component getViewComponent();
	public String getToolTip();
	public Icon getIcon();
}
