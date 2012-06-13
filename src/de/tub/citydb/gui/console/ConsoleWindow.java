/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.gui.console;

import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.window.WindowSize;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.util.gui.GuiUtil;

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
		setTitle(Internal.I18N.getString("main.window.title") + " - " + Internal.I18N.getString("main.console.label"));
	}

}
