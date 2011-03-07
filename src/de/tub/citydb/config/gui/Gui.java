package de.tub.citydb.config.gui;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.gui.window.ConsoleWindow;
import de.tub.citydb.config.gui.window.MainWindow;

@XmlRootElement
@XmlType(name="GuiType", propOrder={
		"main",
		"console",
		"showPreferencesConfirmDialog",
		"recentlyUsedProjects"
})
public class Gui {
	private MainWindow main; 
	private ConsoleWindow console;
	private boolean showPreferencesConfirmDialog = true;
	@XmlElementWrapper(name="recentlyUsedProjects")
	@XmlElement(name="fileName")
	private List<String> recentlyUsedProjects;
	
	@XmlTransient
	private final int maxLastUsedEntries = 5;

	public Gui() {
		main = new MainWindow();
		console = new ConsoleWindow();
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
