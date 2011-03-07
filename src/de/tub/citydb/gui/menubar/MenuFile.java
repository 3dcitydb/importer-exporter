package de.tub.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.tub.citydb.gui.ImpExpGui;

public class MenuFile extends JMenu {
	private JMenuItem exit;
	
	public MenuFile(String name) {
		super(name);
		init();
	}
	
	private void init() {
		exit = new JMenuItem("");
		
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ImpExpGui)getTopLevelAncestor()).dispose();
			}
		});
		
		add(exit);
	}
	
	public void doTranslation() {
		exit.setText(ImpExpGui.labels.getString("menu.file.exit.label"));
	}
	
}
