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
package org.citydb.gui.console;

import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.citydb.config.Config;
import org.citydb.config.gui.window.WindowSize;
import org.citydb.config.language.Language;
import org.citydb.gui.ImpExpGui;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ConsoleWindow extends JFrame {
	private final Component content;
	private final Config config;
	private final ImpExpGui topFrame;

	public ConsoleWindow(Component content, Config config, ImpExpGui topFrame) {
		this.content = content;
		this.config = config;
		this.topFrame = topFrame;

		init();
		doTranslation();
		loadSettings();
	}

	private void init() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ConsoleWindow.class.getResource("/resources/img/common/logo_small.png")));
		setLayout(new GridBagLayout());
	}

	public void activate() {
		if (getWidth() == 0 && getHeight() == 0) {
			// if the console window has not been opened before
			int width = content.getWidth();
			if (width == 0)
				width = topFrame.getWidth();

			setLocation(topFrame.getX() + topFrame.getWidth(), topFrame.getY());
			setSize(width, topFrame.getHeight());
		}

		add(content, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));			
		doTranslation();
		setVisible(true);
	}

	public void loadSettings() {
		WindowSize size = config.getGui().getConsoleWindow().getSize();
		if (size.getX() != null && size.getY() != null && size.getWidth() != null & size.getHeight() != null) {
			setLocation(size.getX(), size.getY());
			setSize(size.getWidth(), size.getHeight());
		}
	}

	public void setSettings() {
		if (config.getGui().getConsoleWindow().isDetached()) {
			WindowSize size = config.getGui().getConsoleWindow().getSize();
			size.setX(getX());
			size.setY(getY());
			size.setWidth(getWidth());
			size.setHeight(getHeight());
		} else
			config.getGui().getConsoleWindow().setSize(new WindowSize());
	}

	public void doTranslation() {
		setTitle(Language.I18N.getString("main.window.title") + " - " + Language.I18N.getString("main.console.label"));
	}

}
