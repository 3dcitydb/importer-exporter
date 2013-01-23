/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.config;

import de.tub.citydb.config.gui.Gui;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.Project;

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
