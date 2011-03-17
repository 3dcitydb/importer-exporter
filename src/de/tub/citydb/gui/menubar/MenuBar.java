/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import javax.swing.JMenuBar;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private final Config config;
	private final JAXBContext ctx;
	private final ImpExpGui topFrame;
	
	private MenuFile file;
	private MenuProject project;
	private MenuWindow window;
	private MenuHelp help;
	
	public MenuBar(Config config, JAXBContext ctx, ImpExpGui topFrame) {
		this.config = config;
		this.ctx = ctx;
		this.topFrame = topFrame;
		init();
	}
	
	private void init() {
		file = new MenuFile();
		project = new MenuProject(config, ctx, topFrame);
		window = new MenuWindow(config, topFrame);
		help = new MenuHelp(config, topFrame);
		
		add(file);
		add(project);
		add(window);
		add(help);
	}
	
	public void doTranslation() {
		file.setText(Internal.I18N.getString("menu.file.label"));
		project.setText(Internal.I18N.getString("menu.project.label"));
		window.setText(Internal.I18N.getString("menu.window.label"));
		help.setText(Internal.I18N.getString("menu.help.label"));
		
		GuiUtil.setMnemonic(file, "menu.file.label", "menu.file.label.mnemonic");
		GuiUtil.setMnemonic(project, "menu.project.label", "menu.project.label.mnemonic");
		GuiUtil.setMnemonic(window, "menu.window.label", "menu.window.label.mnemonic");
		GuiUtil.setMnemonic(help, "menu.help.label", "menu.help.label.mnemonic");
		
		file.doTranslation();
		project.doTranslation();
		window.doTranslation();
		help.doTranslation();
	}
	
}
