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
package org.citydb.config;

import org.citydb.config.gui.Gui;
import org.citydb.config.internal.Internal;
import org.citydb.config.project.Project;

public class Config {
	private Project project;
	private Gui gui;
	private Internal internal;
	
	public Config() {
		project = new Project();
		gui = new Gui();
		internal = new Internal();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		if (project != null) {
			this.project = project;
			
			// add things to be done after changing the project settings
			// (e.g., after unmarshalling the config file) here 
			project.getDatabase().addDefaultReferenceSystems();
		}
	}

	public Gui getGui() {
		return gui;
	}

	public void setGui(Gui gui) {
		if (gui != null)
			this.gui = gui;
	}

	public Internal getInternal() {
		return internal;
	}

}
