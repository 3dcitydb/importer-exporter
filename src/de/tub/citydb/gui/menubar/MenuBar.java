/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.menubar;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.api.plugin.extension.menu.MenuExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.plugin.PluginService;
import de.tub.citydb.util.gui.GuiUtil;
import de.tub.citydb.util.gui.OSXAdapter;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private final PluginService pluginService;
	private final Config config;
	private final JAXBContext ctx;
	private final ImpExpGui topFrame;

	private MenuFile file;
	private MenuProject project;
	private MenuView view;
	private MenuHelp help;

	private JMenu extensions;

	public MenuBar(PluginService pluginService, Config config, JAXBContext ctx, ImpExpGui topFrame) {
		this.pluginService = pluginService;
		this.config = config;
		this.ctx = ctx;
		this.topFrame = topFrame;
		init();
	}

	private void init() {
		file = new MenuFile();
		project = new MenuProject(pluginService, config, ctx, topFrame);
		view = new MenuView(config, topFrame);
		help = new MenuHelp(config, topFrame);

		// as long as the file menu only allows to close the application
		// we do not need it on Mac OS X
		if (!OSXAdapter.IS_MAC_OS_X)
			add(file);
		
		add(project);

		for (MenuExtension extension : pluginService.getExternalMenuExtensions()) {
			if (extensions == null)
				extensions = new JMenu();

			JMenu menu = extension.getMenu().getMenuComponent();
			menu.setText(extension.getMenu().getLocalizedTitle());
			menu.setIcon(extension.getMenu().getIcon());
			GuiUtil.setMnemonic(menu, menu.getText(), extension.getMenu().getMnemonicIndex());
			extensions.add(menu);
		}

		if (extensions != null)
			add(extensions);

		add(view);
		add(help);
	}

	public void doTranslation() {
		file.setText(Internal.I18N.getString("menu.file.label"));
		project.setText(Internal.I18N.getString("menu.project.label"));
		view.setText(Internal.I18N.getString("menu.view.label"));
		help.setText(Internal.I18N.getString("menu.help.label"));

		GuiUtil.setMnemonic(file, "menu.file.label", "menu.file.label.mnemonic");
		GuiUtil.setMnemonic(project, "menu.project.label", "menu.project.label.mnemonic");
		GuiUtil.setMnemonic(view, "menu.view.label", "menu.view.label.mnemonic");
		GuiUtil.setMnemonic(help, "menu.help.label", "menu.help.label.mnemonic");

		if (extensions != null) {
			extensions.setText(Internal.I18N.getString("menu.extensions.label"));
			GuiUtil.setMnemonic(extensions, "menu.extensions.label", "menu.extensions.label.mnemonic");

			int index = 0;
			for (MenuExtension extension : pluginService.getExternalMenuExtensions())
				((JMenu)extensions.getMenuComponent(index++)).setText(extension.getMenu().getLocalizedTitle());
		}

		file.doTranslation();
		project.doTranslation();
		view.doTranslation();
		help.doTranslation();
	}

	public void printInfo() {
		help.printInfo();
	}

}
