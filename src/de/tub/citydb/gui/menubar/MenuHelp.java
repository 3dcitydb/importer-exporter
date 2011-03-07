package de.tub.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import de.tub.citydb.gui.ImpExpGui;

public class MenuHelp extends JMenu {
	private JMenuItem info;
	private JMenuItem readMe;
	
	public MenuHelp(String name) {
		super(name);
		init();
	}
	
	private void init() {
		info = new JMenuItem("");
		readMe = new JMenuItem("");
		
		info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printHelp();
			}
		});
		
		readMe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printReadMe();
			}
		});
		
		add(info);
		add(readMe);
	}
	
	public void doTranslation() {
		info.setText(ImpExpGui.labels.getString("menu.file.info.label"));		
		readMe.setText(ImpExpGui.labels.getString("menu.file.readMe.label"));
	}
	
	private void printHelp() {		
		final InfoDialog infoDialog = new InfoDialog((ImpExpGui)getTopLevelAncestor());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				infoDialog.pack();
				infoDialog.setLocationRelativeTo(getTopLevelAncestor());
				infoDialog.setVisible(true);
			}
		});
	}
	
	private void printReadMe() {		
		final ReadMeDialog readMeDialog = new ReadMeDialog((ImpExpGui)getTopLevelAncestor());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				readMeDialog.pack();
				readMeDialog.setLocationRelativeTo(getTopLevelAncestor());
				readMeDialog.setVisible(true);
			}
		});
	}
}
