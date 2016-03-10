/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class MenuHelp extends JMenu {
	private final Config config;
	private final ImpExpGui topFrame;
	private JMenuItem info;
	private JMenuItem readMe;
	
	public MenuHelp(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;
		init();
	}
	
	private void init() {
		info = new JMenuItem();
		readMe = new JMenuItem();
		
		info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printInfo();
			}
		});
		
		readMe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printReadMe();
			}
		});
		
		add(info);
		add(readMe);
	}
	
	public void doTranslation() {
		info.setText(Language.I18N.getString("menu.help.info.label"));		
		readMe.setText(Language.I18N.getString("menu.help.readMe.label"));
		
		GuiUtil.setMnemonic(info, "menu.help.info.label", "menu.help.info.label.mnemonic");
		GuiUtil.setMnemonic(readMe, "menu.help.readMe.label", "menu.help.readMe.label.mnemonic");
	}
	
	public void printInfo() {		
		final InfoDialog infoDialog = new InfoDialog(config, topFrame);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				infoDialog.setLocationRelativeTo(getTopLevelAncestor());
				infoDialog.setVisible(true);
			}
		});
	}
	
	private void printReadMe() {		
		final ReadMeDialog readMeDialog = new ReadMeDialog(topFrame);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				readMeDialog.setLocationRelativeTo(getTopLevelAncestor());
				readMeDialog.setVisible(true);
			}
		});
	}
}
