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
		if (project != null)
			this.project = project;
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

	public void setInternal(Internal internal) {
		this.internal = internal;
	}

}
