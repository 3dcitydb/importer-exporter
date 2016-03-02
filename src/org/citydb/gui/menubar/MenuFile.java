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

import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class MenuFile extends JMenu {
	private JMenuItem exit;
	
	public MenuFile() {
		init();
	}
	
	private void init() {
		exit = new JMenuItem();
		
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ImpExpGui)getTopLevelAncestor()).dispose();
			}
		});
		
		add(exit);
	}
	
	public void doTranslation() {
		exit.setText(Language.I18N.getString("menu.file.exit.label"));
		GuiUtil.setMnemonic(exit, "menu.file.exit.label", "menu.file.exit.label.mnemonic");
	}
	
}
