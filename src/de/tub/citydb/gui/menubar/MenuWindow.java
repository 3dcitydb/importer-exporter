package de.tub.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.Gui;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MenuWindow extends JMenu {
	private final Config config;
	private final ImpExpGui topFrame;
	
	private JCheckBoxMenuItem detachConsole;
	private JMenuItem defaults;
	
	public MenuWindow(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;		
		init();
	}
	
	private void init() {
		detachConsole = new JCheckBoxMenuItem();
		detachConsole.setSelected(config.getGui().getConsoleWindow().isDetached());
		defaults = new JMenuItem();
		
		detachConsole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean status = !config.getGui().getConsoleWindow().isDetached();
				config.getGui().getConsoleWindow().setDetached(status);		
				topFrame.enableConsoleWindow(status, true);
				detachConsole.setSelected(status);
			}
		});
		
		defaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// do not loose recently used projects
				List<String> recentlyUsedProjects = config.getGui().getRecentlyUsedProjectFiles();
				config.setGui(new Gui());
				
				config.getGui().setRecentlyUsedProjectFiles(recentlyUsedProjects);
				detachConsole.setSelected(config.getGui().getConsoleWindow().isDetached());
				topFrame.restoreDefaults();
			}
		});
		
		add(detachConsole);
		addSeparator();
		add(defaults);
	}
	
	public void doTranslation() {
		detachConsole.setText(Internal.I18N.getString("menu.window.detach.label"));
		defaults.setText(Internal.I18N.getString("menu.window.defaults.label"));
		
		GuiUtil.setMnemonic(detachConsole, "menu.window.detach.label", "menu.window.detach.label.mnemonic");
		GuiUtil.setMnemonic(defaults, "menu.window.defaults.label", "menu.window.defaults.label.mnemonic");
	}
	
}
