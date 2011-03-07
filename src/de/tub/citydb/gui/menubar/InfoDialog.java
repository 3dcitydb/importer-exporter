package de.tub.citydb.gui.menubar;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import de.tub.citydb.gui.GuiUtil;

public class InfoDialog extends JDialog {

	public InfoDialog(JFrame frame) {
		super(frame, "Info", true);
		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton button = new JButton("Ok");		
		
		setLayout(new GridBagLayout()); {
			JPanel main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				Border border = BorderFactory.createEtchedBorder();
				Border margin = BorderFactory.createEmptyBorder(2, 2, 2, 2);
				
				JLabel titel = new JLabel(this.getClass().getPackage().getImplementationTitle());
				Font font = titel.getFont();
				titel.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
				main.add(titel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

				JLabel version = new JLabel("Version " + this.getClass().getPackage().getImplementationVersion());
				main.add(version, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,2,5,5,5));
				
				JLabel authorsHeader = new JLabel("Authors");
				main.add(authorsHeader, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
				JTextArea authors = new JTextArea();
				authors.setBorder(new CompoundBorder(border, margin));				
				authors.setEditable(false);
				authors.setBackground(new Color(255, 255, 255));
				authors.setFont(authorsHeader.getFont());
				
				authors.setText("Claus Nagel <nagel@igg.tu-berlin.de>\n" +
						"Alexandra Stadler <stadler@igg.tu-berlin.de>\n" +
						"Gerhard Koenig <gerhard.koenig@tu-berlin.de>\n" +
						"Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>");				
				main.add(authors, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));
				
				JLabel copyHeader = new JLabel("Copyright");
				main.add(copyHeader, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
				JTextArea copy = new JTextArea();
				copy.setBorder(new CompoundBorder(border, margin));
				copy.setEditable(false);
				copy.setBackground(new Color(255, 255, 255));
				copy.setFont(authorsHeader.getFont());
				copy.setText("(c) 2007 - 2008\n" +
						"Institute for Geodesy and Geoinformation Science (IGG)\n" +
						"Technische Universität Berlin, Germany\n" +
						"http://www.igg.tu-berlin.de/\n\n" +
						"This program is free software under the GNU Lesser General\n" +
						"Public License Version 3.0. For a copy of the GNU LGPL see\n<http://www.gnu.org/licenses/>.");				
				main.add(copy, GuiUtil.setConstraints(0,5,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));
				
				JLabel libHeader = new JLabel("External Libraries");
				main.add(libHeader, GuiUtil.setConstraints(0,6,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
				JTextArea lib = new JTextArea();
				lib.setAutoscrolls(true);
				lib.setRows(4);
				lib.setEditable(false);
				lib.setBorder(margin);
				lib.setBackground(libHeader.getBackground());
				lib.setFont(authorsHeader.getFont());
				
				File libDir = new File("lib");
				File[] libList = libDir.listFiles();
				StringBuilder libText = new StringBuilder();
				for (int i = 0; i < libList.length; i++) {
					if (!libList[i].isFile())
						continue;
					
					libText.append(libList[i].getName());				
					if (i != libList.length - 1) 
						libText.append("\n");
				}				
				lib.setText(libText.toString());
				
				JScrollPane scroll = new JScrollPane(lib);
				scroll.setBorder(border);
				main.add(scroll, GuiUtil.setConstraints(0,7,0.0,0.0,GridBagConstraints.HORIZONTAL,2,5,5,5));
			}
			
			button.setMargin(new Insets(button.getMargin().top, 25, button.getMargin().bottom, 25));
			add(button, GuiUtil.setConstraints(0,8,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
		}

		setResizable(false);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dispose();
					}
				});
			}
		});
	}
}
