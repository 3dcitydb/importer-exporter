package de.tub.citydb.api.plugin.extension.view;

import java.awt.Component;

import javax.swing.Icon;

public interface View {
	public String getLocalizedTitle();
	public Component getViewComponent();
	public String getToolTip();
	public Icon getIcon();
}
