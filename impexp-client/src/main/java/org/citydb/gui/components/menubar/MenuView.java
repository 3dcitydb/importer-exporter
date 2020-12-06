/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.gui.components.menubar;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.gui.GuiConfig;
import org.citydb.config.gui.style.Theme;
import org.citydb.config.i18n.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.mapviewer.MapWindow;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.util.List;

public class MenuView extends JMenu {
	private final ImpExpGui mainView;
	private final Config config;
	
	private JMenuItem map;
	private JCheckBoxMenuItem detachConsole;
	private JCheckBoxMenuItem lightTheme;
	private JCheckBoxMenuItem darkTheme;
	private JMenuItem defaults;
	
	public MenuView(ImpExpGui mainView, Config config) {
		this.mainView = mainView;		
		this.config = config;
		init();
	}
	
	private void init() {
		map = new JMenuItem(new FlatSVGIcon("org/citydb/gui/icons/map.svg"));
		detachConsole = new JCheckBoxMenuItem();
		detachConsole.setSelected(config.getGuiConfig().getConsoleWindow().isDetached());
		lightTheme = new JCheckBoxMenuItem();
		darkTheme = new JCheckBoxMenuItem();
		defaults = new JMenuItem();

		ButtonGroup lafGroup = new ButtonGroup();
		lafGroup.add(lightTheme);
		lafGroup.add(darkTheme);

		if (config.getGuiConfig().getAppearance().getTheme() == Theme.DARK) {
			darkTheme.setSelected(true);
		} else {
			lightTheme.setSelected(true);
		}

		map.addActionListener(e -> {
			SwingUtilities.invokeLater(() -> MapWindow.getInstance(mainView).setVisible(true));
		});

		detachConsole.addActionListener(e -> mainView.enableConsoleWindow(!config.getGuiConfig().getConsoleWindow().isDetached(), true));
		lightTheme.addActionListener(e -> mainView.setLookAndFeel(Theme.LIGHT));
		darkTheme.addActionListener(e -> mainView.setLookAndFeel(Theme.DARK));

		defaults.addActionListener(e -> {
			List<String> recentlyUsedProjects = config.getGuiConfig().getRecentlyUsedConfigFiles();
			Theme theme = config.getGuiConfig().getAppearance().getTheme();

			// replace existing GUI configuration with defaults
			config.setGuiConfig(new GuiConfig());

			config.getGuiConfig().setRecentlyUsedConfigFiles(recentlyUsedProjects);
			config.getGuiConfig().getAppearance().setTheme(theme);
			mainView.restoreDefaults();
		});
		
		add(map);
		addSeparator();
		add(detachConsole);
		addSeparator();
		add(lightTheme);
		add(darkTheme);
		addSeparator();
		add(defaults);
	}
	
	public void doTranslation() {
		map.setText(Language.I18N.getString("menu.view.map.label"));
		detachConsole.setText(Language.I18N.getString("menu.view.detach.label"));
		lightTheme.setText(Language.I18N.getString("map.view.laf.light.label"));
		darkTheme.setText(Language.I18N.getString("map.view.laf.dark.label"));
		defaults.setText(Language.I18N.getString("menu.view.defaults.label"));
		
		GuiUtil.setMnemonic(map, "menu.view.map.label", "menu.view.map.label.mnemonic");
		GuiUtil.setMnemonic(detachConsole, "menu.view.detach.label", "menu.view.detach.label.mnemonic");
		GuiUtil.setMnemonic(lightTheme, "map.view.laf.light.label", "map.view.laf.light.label.mnemonic");
		GuiUtil.setMnemonic(darkTheme, "map.view.laf.dark.label", "map.view.laf.dark.label.mnemonic");
		GuiUtil.setMnemonic(defaults, "menu.view.defaults.label", "menu.view.defaults.label.mnemonic");
	}

	public void update() {
		detachConsole.setSelected(config.getGuiConfig().getConsoleWindow().isDetached());
	}
	
}
