/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.citydb.config.i18n.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.util.GuiUtil;

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
