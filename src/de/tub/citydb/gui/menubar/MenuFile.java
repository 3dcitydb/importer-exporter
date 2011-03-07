package de.tub.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class MenuFile extends JMenu {
	private JMenuItem exit;
	
	public MenuFile() {
		init();
	}
	
	private void init() {
		exit = new JMenuItem();
		
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ImpExpGui)getTopLevelAncestor()).dispose();
			}
		});
		
		add(exit);
	}
	
	public void doTranslation() {
		exit.setText(Internal.I18N.getString("menu.file.exit.label"));
		GuiUtil.setMnemonic(exit, "menu.file.exit.label", "menu.file.exit.label.mnemonic");
	}
	
}
