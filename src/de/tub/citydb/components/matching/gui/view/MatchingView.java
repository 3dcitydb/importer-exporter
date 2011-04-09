package de.tub.citydb.components.matching.gui.view;

import java.awt.Component;

import javax.swing.Icon;

import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.components.matching.gui.view.components.MatchingPanel;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;

public class MatchingView implements View {
	private final MatchingPanel component;
	
	public MatchingView(Config config, ImpExpGui mainView) {
		component = new MatchingPanel(config, mainView);
	}
	
	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("main.tabbedPane.matchingTool");
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
	
	public void doTranslation() {
		component.doTranslation();
	}

}
