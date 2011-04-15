package de.tub.citydb.modules.preferences.gui.view;

import java.awt.Component;

import javax.swing.Icon;

import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.plugin.PluginService;

public class PreferencesView implements View {
	private PreferencesPanel component;
	
	public PreferencesView(PluginService pluginService, Config config, ImpExpGui mainView) {
		component = new PreferencesPanel(pluginService, config, mainView);
	}
	
	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("main.tabbedPane.preferences");
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
	
	public void doTranslation() {
		component.doTranslation();
	}
	
	public boolean requestChange() {
		return component.requestChange();
	}

}
