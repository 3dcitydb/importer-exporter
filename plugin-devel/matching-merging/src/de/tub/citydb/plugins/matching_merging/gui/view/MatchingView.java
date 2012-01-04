package de.tub.citydb.plugins.matching_merging.gui.view;

import java.awt.Component;

import javax.swing.Icon;

import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.plugins.matching_merging.PluginImpl;
import de.tub.citydb.plugins.matching_merging.util.Util;

public class MatchingView extends View {
	private final MatchingPanel component;
	
	public MatchingView(PluginImpl plugin) {
		component = new MatchingPanel(plugin);
	}
	
	@Override
	public String getLocalizedTitle() {
		return Util.I18N.getString("main.tabbedPane.matchingTool");
	}

	@Override
	public Component getViewComponent() {
		return component;
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return null;
	}
	
	public void loadSettings() {
		component.loadSettings();
	}
	
	public void setSettings() {
		component.setSettings();
	}
	
	public void switchLocale() {
		component.switchLocale();
	}

}
