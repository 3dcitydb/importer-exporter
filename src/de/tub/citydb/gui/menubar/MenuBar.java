package de.tub.citydb.gui.menubar;

import javax.swing.JMenuBar;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;

public class MenuBar extends JMenuBar {
	private final Config config;
	private final JAXBContext ctx;
	
	private MenuFile file;
	private MenuProject project;
	private MenuHelp help;
	
	public MenuBar(Config config, JAXBContext ctx) {
		this.config = config;
		this.ctx = ctx;
		init();
	}
	
	private void init() {
		file = new MenuFile("");
		project = new MenuProject("", config, ctx);
		help = new MenuHelp("");
		
		add(file);
		add(project);
		add(help);
	}
	
	public void doTranslation() {
		file.setText(Internal.I18N.getString("menu.file.label"));
		project.setText(Internal.I18N.getString("menu.project.label"));
		help.setText(Internal.I18N.getString("menu.help.label"));
		
		file.doTranslation();
		project.doTranslation();
		help.doTranslation();
	}
}
