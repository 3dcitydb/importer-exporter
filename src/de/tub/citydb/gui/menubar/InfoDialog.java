package de.tub.citydb.gui.menubar;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class InfoDialog extends JDialog {

	public InfoDialog(JFrame frame) {
		super(frame, Internal.I18N.getString("menu.file.info.label"), true);
		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton button = new JButton(Internal.I18N.getString("common.button.ok"));	

		setLayout(new GridBagLayout()); {
			JPanel header = new JPanel();
			header.setBackground(new Color(255, 255, 255));
			add(header, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
			header.setLayout(new GridBagLayout());
			{
				String name = this.getClass().getPackage().getImplementationTitle();
				String version = this.getClass().getPackage().getImplementationVersion();

				JLabel titel = new JLabel();
				titel.setText("<html><body><b>" + name + "</b><br>" +
						"Version " + version + "</body></html>");
				GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.HORIZONTAL,10,5,0,20);
				c.anchor = GridBagConstraints.NORTHEAST;
				header.add(titel, c);

				JLabel img = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/resources/img/logo.png"))));
				header.add(img, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.NONE,5,0,5,0));
			}

			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setMinimumSize(sep.getPreferredSize());
			add(sep, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,0,0,0));

			JPanel main = new JPanel();
			add(main, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				Border border = BorderFactory.createEtchedBorder();
				Border margin = BorderFactory.createEmptyBorder(2, 2, 2, 2);

				JLabel authorsHeader = new JLabel(Internal.I18N.getString("menu.file.info.authors"));
				main.add(authorsHeader, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
				JTextArea authors = new JTextArea();
				authors.setBorder(new CompoundBorder(border, margin));				
				authors.setEditable(false);
				authors.setBackground(new Color(255, 255, 255));
				authors.setFont(authorsHeader.getFont());

				authors.setText("Claus Nagel <claus.nagel@tu-berlin.de>\n" +
						"Javier Herreruela <javier.herreruela@tu-berlin.de>\n" +
						"Alexandra Lorenz <lorenz@tu-berlin.de>\n" +
						"Gerhard Koenig <gerhard.koenig@tu-berlin.de>\n" +
				"Thomas H. Kolbe <thomas.kolbe@tu-berlin.de>");				
				main.add(authors, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));

				JLabel copyHeader = new JLabel("Copyright");
				main.add(copyHeader, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
				JTextArea copy = new JTextArea();
				copy.setBorder(new CompoundBorder(border, margin));
				copy.setEditable(false);
				copy.setBackground(new Color(255, 255, 255));
				copy.setFont(authorsHeader.getFont());
				copy.setText("(c) 2007 - 2011\n" +
						"Institute for Geodesy and Geoinformation Science (IGG)\n" +
						"Technische Universität Berlin, Germany\n" +
						"http://www.igg.tu-berlin.de/\n\n" +
						"This program is free software under the GNU Lesser General\n" +
				"Public License Version 3.0. For a copy of the GNU LGPL see\n<http://www.gnu.org/licenses/>.");				
				main.add(copy, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));				
			}				

			button.setMargin(new Insets(button.getMargin().top, 25, button.getMargin().bottom, 25));
			add(button, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
		}

		setResizable(true);
		pack();

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
