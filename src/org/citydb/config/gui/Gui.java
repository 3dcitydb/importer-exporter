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
package org.citydb.config.gui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.gui.window.ConsoleWindow;
import org.citydb.config.gui.window.MainWindow;
import org.citydb.config.gui.window.MapWindow;

@XmlRootElement
@XmlType(name="GuiType", propOrder={
		"main",
		"console",
		"map",
		"showPreferencesConfirmDialog",
		"recentlyUsedProjects"
})
public class Gui {
	private MainWindow main; 
	private ConsoleWindow console;
	private MapWindow map;
	private boolean showPreferencesConfirmDialog = true;
	@XmlElementWrapper(name="recentlyUsedProjects")
	@XmlElement(name="fileName")
	private List<String> recentlyUsedProjects;

	@XmlTransient
	private final int maxLastUsedEntries = 5;

	public Gui() {
		main = new MainWindow();
		console = new ConsoleWindow();
		map = new MapWindow();
		recentlyUsedProjects = new ArrayList<String>(maxLastUsedEntries + 1);
	}

	public MainWindow getMainWindow() {
		return main;
	}

	public void setMainWindow(MainWindow main) {
		if (main != null)
			this.main = main;
	}

	public ConsoleWindow getConsoleWindow() {
		return console;
	}

	public void setConsoleWindow(ConsoleWindow console) {
		if (console != null)
			this.console = console;
	}

	public MapWindow getMapWindow() {
		return map;
	}

	public void setMapWindow(MapWindow map) {
		if (map != null)
			this.map = map;
	}

	public boolean isShowPreferencesConfirmDialog() {
		return showPreferencesConfirmDialog;
	}

	public void setShowPreferencesConfirmDialog(boolean showPreferencesConfirmDialog) {
		this.showPreferencesConfirmDialog = showPreferencesConfirmDialog;
	}

	public List<String> getRecentlyUsedProjectFiles() {
		return recentlyUsedProjects;
	}

	public void setRecentlyUsedProjectFiles(List<String> recentlyUsedProjects) {
		if (recentlyUsedProjects != null)
			this.recentlyUsedProjects = recentlyUsedProjects;
	}

	public int getMaxLastUsedEntries() {
		return maxLastUsedEntries;
	}

}
