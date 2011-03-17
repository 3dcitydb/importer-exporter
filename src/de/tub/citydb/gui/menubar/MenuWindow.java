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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.Gui;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MenuWindow extends JMenu {
	private final Config config;
	private final ImpExpGui topFrame;
	
	private JCheckBoxMenuItem detachConsole;
	private JMenuItem defaults;
	
	public MenuWindow(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;		
		init();
	}
	
	private void init() {
		detachConsole = new JCheckBoxMenuItem();
		detachConsole.setSelected(config.getGui().getConsoleWindow().isDetached());
		defaults = new JMenuItem();
		
		detachConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean status = !config.getGui().getConsoleWindow().isDetached();
				config.getGui().getConsoleWindow().setDetached(status);		
				topFrame.enableConsoleWindow(status, true);
				detachConsole.setSelected(status);
			}
		});
		
		defaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// do not loose recently used projects
				List<String> recentlyUsedProjects = config.getGui().getRecentlyUsedProjectFiles();
				config.setGui(new Gui());
				
				config.getGui().setRecentlyUsedProjectFiles(recentlyUsedProjects);
				detachConsole.setSelected(config.getGui().getConsoleWindow().isDetached());
				topFrame.restoreDefaults();
			}
		});
		
		add(detachConsole);
		addSeparator();
		add(defaults);
	}
	
	public void doTranslation() {
		detachConsole.setText(Internal.I18N.getString("menu.window.detach.label"));
		defaults.setText(Internal.I18N.getString("menu.window.defaults.label"));
		
		GuiUtil.setMnemonic(detachConsole, "menu.window.detach.label", "menu.window.detach.label.mnemonic");
		GuiUtil.setMnemonic(defaults, "menu.window.defaults.label", "menu.window.defaults.label.mnemonic");
	}
	
}
