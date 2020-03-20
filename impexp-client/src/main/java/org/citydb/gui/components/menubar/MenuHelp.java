/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.ImpExp;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

@SuppressWarnings("serial")
public class MenuHelp extends JMenu {
	private final Config config;
	private final ImpExpGui mainView;
	private JMenuItem doc;
	private JMenuItem info;
	private JMenuItem readMe;
	
	public MenuHelp(ImpExpGui mainView, Config config) {
		this.config = config;
		this.mainView = mainView;
		init();
	}
	
	private void init() {
		doc = new JMenuItem();
		info = new JMenuItem();
		readMe = new JMenuItem();

		doc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openOnlineDoc();
			}
		});

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

		add(doc);
		add(info);
		add(readMe);
	}
	
	public void doTranslation() {
		doc.setText(Language.I18N.getString("menu.help.doc.label"));
		info.setText(Language.I18N.getString("menu.help.info.label"));		
		readMe.setText(Language.I18N.getString("menu.help.readMe.label"));

		GuiUtil.setMnemonic(doc, "menu.help.doc.label", "menu.help.doc.label.mnemonic");
		GuiUtil.setMnemonic(info, "menu.help.info.label", "menu.help.info.label.mnemonic");
		GuiUtil.setMnemonic(readMe, "menu.help.readMe.label", "menu.help.readMe.label.mnemonic");
	}

	public void openOnlineDoc() {
		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				if (ImpExp.class.getName().equals(manifest.getMainAttributes().getValue("Main-Class"))) {
					java.awt.Desktop.getDesktop().browse(URI.create(manifest.getMainAttributes().getValue("docUrl")));
					break;
				}
			}
		} catch (IOException e) {
			Logger.getInstance().error("Failed to open the 3DCityDB online documentation: " + e.getMessage());
		}
	}
	
	public void printInfo() {		
		final InfoDialog infoDialog = new InfoDialog(config, mainView);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				infoDialog.setLocationRelativeTo(getTopLevelAncestor());
				infoDialog.setVisible(true);
			}
		});
	}
	
	private void printReadMe() {		
		final ReadMeDialog readMeDialog = new ReadMeDialog(mainView);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				readMeDialog.setLocationRelativeTo(getTopLevelAncestor());
				readMeDialog.setVisible(true);
			}
		});
	}
}
