/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.gui.menubar;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.xml.bind.JAXBContext;

import org.citydb.api.plugin.extension.menu.MenuExtension;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.plugin.PluginService;
import org.citydb.util.gui.GuiUtil;
import org.citydb.util.gui.OSXAdapter;

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
		file.setText(Language.I18N.getString("menu.file.label"));
		project.setText(Language.I18N.getString("menu.project.label"));
		view.setText(Language.I18N.getString("menu.view.label"));
		help.setText(Language.I18N.getString("menu.help.label"));

		GuiUtil.setMnemonic(file, "menu.file.label", "menu.file.label.mnemonic");
		GuiUtil.setMnemonic(project, "menu.project.label", "menu.project.label.mnemonic");
		GuiUtil.setMnemonic(view, "menu.view.label", "menu.view.label.mnemonic");
		GuiUtil.setMnemonic(help, "menu.help.label", "menu.help.label.mnemonic");

		if (extensions != null) {
			extensions.setText(Language.I18N.getString("menu.extensions.label"));
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
