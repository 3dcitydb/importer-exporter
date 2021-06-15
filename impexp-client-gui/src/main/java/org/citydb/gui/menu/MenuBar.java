/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.menu;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.util.GuiUtil;
import org.citydb.core.log.Logger;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.plugin.extension.menu.Menu;
import org.citydb.core.plugin.extension.menu.MenuExtension;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class MenuBar extends JMenuBar {
	private final PluginManager pluginManager;

	private final MenuFile file;
	private final MenuView view;
	private final MenuHelp help;

	private JMenu extensions;

	public MenuBar(ImpExpGui mainView, Config config) {
		pluginManager = PluginManager.getInstance();
		
		file = new MenuFile(mainView, config);
		view = new MenuView(mainView, config);
		help = new MenuHelp(mainView, config);
		
		add(file);

		for (MenuExtension extension : pluginManager.getExternalPlugins(MenuExtension.class)) {
			Menu menu = extension.getMenu();
			if (menu == null || menu.getMenuComponent() == null) {
				Logger.getInstance().error("Failed to get menu entry from plugin " + extension.getClass().getName() + ".");
				continue;
			}

			if (extensions == null)
				extensions = new JMenu();

			JMenu component = menu.getMenuComponent();
			component.setText(menu.getLocalizedTitle());
			component.setIcon(menu.getIcon());
			GuiUtil.setMnemonic(component, component.getText(), menu.getMnemonicIndex());
			extensions.add(component);
		}

		if (extensions != null)
			add(extensions);

		add(view);
		add(help);

		view.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				view.update();
			}

			@Override
			public void menuDeselected(MenuEvent e) { }

			@Override
			public void menuCanceled(MenuEvent e) { }
		});
	}

	public void doTranslation() {
		file.setText(Language.I18N.getString("menu.file.label"));
		view.setText(Language.I18N.getString("menu.view.label"));
		help.setText(Language.I18N.getString("menu.help.label"));

		GuiUtil.setMnemonic(file, "menu.file.label", "menu.file.label.mnemonic");
		GuiUtil.setMnemonic(view, "menu.view.label", "menu.view.label.mnemonic");
		GuiUtil.setMnemonic(help, "menu.help.label", "menu.help.label.mnemonic");

		if (extensions != null) {
			extensions.setText(Language.I18N.getString("menu.extensions.label"));
			GuiUtil.setMnemonic(extensions, "menu.extensions.label", "menu.extensions.label.mnemonic");

			int index = 0;
			for (MenuExtension extension : pluginManager.getExternalPlugins(MenuExtension.class))
				((JMenu)extensions.getMenuComponent(index++)).setText(extension.getMenu().getLocalizedTitle());
		}

		file.doTranslation();
		view.doTranslation();
		help.doTranslation();
	}

	public void printInfo() {
		help.printInfo();
	}

}
