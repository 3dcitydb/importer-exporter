/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.menubar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.html.HTMLDocument;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class InfoDialog extends JDialog {
	private final Config config;

	public InfoDialog(Config config, JFrame frame) {
		super(frame, Internal.I18N.getString("menu.help.info.label"), true);
		this.config = config;
		
		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton button = new JButton(Internal.I18N.getString("common.button.ok"));	

		setLayout(new GridBagLayout());
		{			
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

				JLabel img = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/resources/img/common/logo.png"))));
				header.add(img, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.NONE,5,0,5,10));
			}

			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setMinimumSize(sep.getPreferredSize());
			add(sep, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,0,0,0));

			JTabbedPane tabs = new JTabbedPane();
			add(tabs, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));

			tabs.add(getGeneralTab(), Internal.I18N.getString("menu.help.info.tab.general"));			
			tabs.add(getPartnerTab(), Internal.I18N.getString("menu.help.info.tab.partner"));			

			button.setMargin(new Insets(button.getMargin().top, 25, button.getMargin().bottom, 25));
			add(button, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
		}

		setMinimumSize(new Dimension(400, 450));		
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

	private JPanel getGeneralTab() {
		JPanel general = new JPanel();		
		general.setLayout(new GridBagLayout());
		{
			Border border = BorderFactory.createEtchedBorder();
			Border margin = BorderFactory.createEmptyBorder(2, 2, 2, 2);

			JLabel authorsHeader = new JLabel(Internal.I18N.getString("menu.help.info.authors"));
			general.add(authorsHeader, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
			JTextArea authors = new JTextArea();
			authors.setBorder(new CompoundBorder(border, margin));				
			authors.setEditable(false);
			authors.setBackground(new Color(255, 255, 255));
			authors.setFont(authorsHeader.getFont());

			authors.setText("Claus Nagel <claus.nagel@tu-berlin.de>\n" +
					"Javier Herreruela <javier.herreruela@tu-berlin.de>\n" +
					"Alexandra Lorenz <lorenz@tu-berlin.de>\n" +
					"Gerhard König <gerhard.koenig@tu-berlin.de>\n" +
			"Thomas H. Kolbe <thomas.kolbe@tum.de>");				
			general.add(authors, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));

			JLabel copyHeader = new JLabel("Copyright");
			general.add(copyHeader, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
			JTextArea copy = new JTextArea();
			copy.setBorder(new CompoundBorder(border, margin));
			copy.setEditable(false);
			copy.setBackground(new Color(255, 255, 255));
			copy.setFont(authorsHeader.getFont());
			copy.setText("(c) 2007 - 2013\n" +
					"Institute for Geodesy and Geoinformation Science (IGG)\n" +
					"Technische Universität Berlin, Germany\n" +
					"http://www.igg.tu-berlin.de/\n\n" +
					"This program is free software under the GNU Lesser General\n" +
			"Public License Version 3.0. For a copy of the GNU LGPL see\n<http://www.gnu.org/licenses/>.");				
			general.add(copy, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));				
	
			PopupMenuDecorator.getInstance().decorate(authors, copy);
		}

		return general;
	}

	private JPanel getPartnerTab() {
		JPanel partner = new JPanel();
		partner.setLayout(new GridBagLayout());
		{
			String text = Internal.I18N.getString("menu.help.info.partner");
			Object[] args = new Object[]{ this.getClass().getPackage().getImplementationTitle() };
			String result = MessageFormat.format(text, args);
			
			JLabel supportText = new JLabel(result);
			partner.add(supportText, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));

			JPanel logos = new JPanel();
			partner.add(logos, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			logos.setBackground(Color.WHITE);
			logos.setBorder(BorderFactory.createEtchedBorder());
			logos.setLayout(new GridBagLayout());
			{
				Font font = UIManager.getFont("Label.font");
		        String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";

		        JLabel blc_logo = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/resources/img/partner/blc_logo.png"))));
				logos.add(blc_logo, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,5,0,5,0));
				JEditorPane blc_label = new JEditorPane("text/html", "<html><b>Business Location Center, Berlin</b><br/>http://www.businesslocationcenter.de/</html>");
		        ((HTMLDocument)blc_label.getDocument()).getStyleSheet().addRule(bodyRule);
				blc_label.setEditable(false);
		        logos.add(blc_label, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,15,5,5));

				JLabel vcs_logo = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/resources/img/partner/vcs_logo.png"))));
				logos.add(vcs_logo, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.NONE,5,0,5,0));
				JEditorPane vcs_label = new JEditorPane("text/html", "<html><b>virtualcitySYSTEMS GmbH, Berlin</b><br/>http://www.virtualcitysystems.de/</html>");
		        ((HTMLDocument)vcs_label.getDocument()).getStyleSheet().addRule(bodyRule);
		        vcs_label.setEditable(false);				
				logos.add(vcs_label, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,5,15,5,5));

				String wtf_label_text = config.getProject().getGlobal().getLanguage() == LanguageType.EN ?
						"Berlin Senate of Business, <br/>Technology and Women" : "Senatsverwaltung für Wirtschaft, <br/>Technologie und Frauen, Berlin";
				
				JLabel wtf_logo = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/resources/img/partner/wtf_logo.png"))));
				logos.add(wtf_logo, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.NONE,5,0,5,0));
				JEditorPane wtf_label = new JEditorPane("text/html", "<html><b>" + wtf_label_text + "</b><br/>http://www.berlin.de/sen/wtf/</html>");
		        ((HTMLDocument)wtf_label.getDocument()).getStyleSheet().addRule(bodyRule);
		        wtf_label.setEditable(false);
				logos.add(wtf_label, GuiUtil.setConstraints(1,2,1,0,GridBagConstraints.HORIZONTAL,5,15,5,5));

				logos.add(new JLabel(), GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,5,5,0,5));
	
				PopupMenuDecorator.getInstance().decorate(blc_label, vcs_label, wtf_label);
			}		
		}

		return partner;
	}

}
