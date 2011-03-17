package de.tub.citydb.gui.menubar;

import javax.swing.JMenuBar;
import javax.xml.bind.JAXBContext;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private final Config config;
	private final JAXBContext ctx;
	private final ImpExpGui topFrame;
	
	private MenuFile file;
	private MenuProject project;
	private MenuWindow window;
	private MenuHelp help;
	
	public MenuBar(Config config, JAXBContext ctx, ImpExpGui topFrame) {
		this.config = config;
		this.ctx = ctx;
		this.topFrame = topFrame;
		init();
	}
	
	private void init() {
		file = new MenuFile();
		project = new MenuProject(config, ctx, topFrame);
		window = new MenuWindow(config, topFrame);
		help = new MenuHelp(config, topFrame);
		
		add(file);
		add(project);
		add(window);
		add(help);
	}
	
	public void doTranslation() {
		file.setText(Internal.I18N.getString("menu.file.label"));
		project.setText(Internal.I18N.getString("menu.project.label"));
		window.setText(Internal.I18N.getString("menu.window.label"));
		help.setText(Internal.I18N.getString("menu.help.label"));
		
		GuiUtil.setMnemonic(file, "menu.file.label", "menu.file.label.mnemonic");
		GuiUtil.setMnemonic(project, "menu.project.label", "menu.project.label.mnemonic");
		GuiUtil.setMnemonic(window, "menu.window.label", "menu.window.label.mnemonic");
		GuiUtil.setMnemonic(help, "menu.help.label", "menu.help.label.mnemonic");
		
		file.doTranslation();
		project.doTranslation();
		window.doTranslation();
		help.doTranslation();
	}
	
}
