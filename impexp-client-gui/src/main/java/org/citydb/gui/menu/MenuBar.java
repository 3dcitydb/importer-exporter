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
import org.citydb.core.plugin.Plugin;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.plugin.PluginStateEvent;
import org.citydb.core.plugin.extension.menu.Menu;
import org.citydb.core.plugin.extension.menu.MenuExtension;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.ArrayList;
import java.util.List;

public class MenuBar extends JMenuBar implements EventHandler {
	private final List<MenuExtension> menuExtensions;

	private final MenuFile file;
	private final MenuView view;
	private final MenuHelp help;
	private JMenu extensions;

	public MenuBar(ImpExpGui mainView, Config config) {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.PLUGIN_STATE, this);
		
		file = new MenuFile(mainView, config);
		view = new MenuView(mainView, config);
		help = new MenuHelp(mainView, config);

		menuExtensions = new ArrayList<>();
		for (MenuExtension extension : PluginManager.getInstance().getExternalPlugins(MenuExtension.class)) {
			Menu menu = extension.getMenu();
			if (menu == null || menu.getMenuComponent() == null) {
				Logger.getInstance().error("Failed to get menu entry from plugin " + extension.getClass().getName() + ".");
				continue;
			}

			menuExtensions.add(extension);
		}

		buildMenu();

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

	private void buildMenu() {
		if (getMenuCount() > 0) {
			removeAll();
		}

		add(file);

		extensions = null;
		for (MenuExtension menuExtension : menuExtensions) {
			if (((Plugin) menuExtension).isEnabled()) {
				if (extensions == null) {
					extensions = new JMenu();
					extensions.setText(Language.I18N.getString("menu.plugins.label"));
					GuiUtil.setMnemonic(extensions, "menu.plugins.label", "menu.plugins.label.mnemonic");
				}

				extensions.add(menuExtension.getMenu().getMenuComponent());
			}
		}

		if (extensions != null) {
			add(extensions);
			translateExtensions();
		}

		add(view);
		add(help);
	}

	public void doTranslation() {
		file.setText(Language.I18N.getString("menu.file.label"));
		view.setText(Language.I18N.getString("menu.view.label"));
		help.setText(Language.I18N.getString("menu.help.label"));

		GuiUtil.setMnemonic(file, "menu.file.label", "menu.file.label.mnemonic");
		GuiUtil.setMnemonic(view, "menu.view.label", "menu.view.label.mnemonic");
		GuiUtil.setMnemonic(help, "menu.help.label", "menu.help.label.mnemonic");

		file.doTranslation();
		view.doTranslation();
		help.doTranslation();

		if (extensions != null) {
			translateExtensions();
		}
	}

	private void translateExtensions() {
		extensions.setText(Language.I18N.getString("menu.plugins.label"));
		GuiUtil.setMnemonic(extensions, "menu.plugins.label", "menu.plugins.label.mnemonic");

		for (int i = 0; i < extensions.getMenuComponentCount(); i++) {
			JMenu menu = (JMenu) extensions.getMenuComponent(i);
			for (MenuExtension menuExtension : menuExtensions) {
				if (menu == menuExtension.getMenu().getMenuComponent()) {
					menu.setText(menuExtension.getMenu().getLocalizedTitle());
					GuiUtil.setMnemonic(menu, menu.getText(), menuExtension.getMenu().getMnemonicIndex());
					break;
				}
			}
		}
	}

	public void printInfo() {
		help.printInfo();
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (((PluginStateEvent) event).getPlugins().stream().anyMatch(p -> p instanceof MenuExtension)) {
			SwingUtilities.invokeLater(this::buildMenu);
		}
	}
}
