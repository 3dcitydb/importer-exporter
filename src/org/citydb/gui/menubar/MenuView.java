/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.citydb.config.Config;
import org.citydb.config.gui.Gui;
import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.mapviewer.MapWindow;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class MenuView extends JMenu {
	private final Config config;
	private final ImpExpGui topFrame;
	
	private JMenuItem map;
	private JCheckBoxMenuItem detachConsole;
	private JMenuItem defaults;
	
	public MenuView(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;		
		init();
	}
	
	private void init() {
		map = new JMenuItem();
		detachConsole = new JCheckBoxMenuItem();
		detachConsole.setSelected(config.getGui().getConsoleWindow().isDetached());
		defaults = new JMenuItem();
		
		map.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final MapWindow map = MapWindow.getInstance(config);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						map.setVisible(true);
					}
				});
			}
		});
		
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
		
		add(map);
		addSeparator();
		add(detachConsole);
		addSeparator();
		add(defaults);
	}
	
	public void doTranslation() {
		map.setText(Language.I18N.getString("menu.view.map.label"));
		detachConsole.setText(Language.I18N.getString("menu.view.detach.label"));
		defaults.setText(Language.I18N.getString("menu.view.defaults.label"));
		
		GuiUtil.setMnemonic(map, "menu.view.map.label", "menu.view.map.label.mnemonic");
		GuiUtil.setMnemonic(detachConsole, "menu.view.detach.label", "menu.view.detach.label.mnemonic");
		GuiUtil.setMnemonic(defaults, "menu.view.defaults.label", "menu.view.defaults.label.mnemonic");
	}
	
}
