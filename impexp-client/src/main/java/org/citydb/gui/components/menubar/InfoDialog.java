/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.gui.components.menubar;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LanguageType;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.text.MessageFormat;

@SuppressWarnings("serial")
public class InfoDialog extends JDialog {
	private final Config config;

	public InfoDialog(Config config, JFrame frame) {
		super(frame, Language.I18N.getString("menu.help.info.label"), true);
		this.config = config;

		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton button = new JButton(Language.I18N.getString("common.button.ok"));	

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

				JLabel img = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/org/citydb/gui/images/common/logo.png"))));
				header.add(img, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.NONE,5,0,5,10));
			}

			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setMinimumSize(sep.getPreferredSize());
			add(sep, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,0,0,0));

			JTabbedPane tabs = new JTabbedPane();
			add(tabs, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));

			tabs.add(getGeneralTab(), Language.I18N.getString("menu.help.info.tab.general"));
			tabs.add(getPartnerTab(), Language.I18N.getString("menu.help.info.tab.partner"));

			button.setMargin(new Insets(button.getMargin().top, 25, button.getMargin().bottom, 25));
			add(button, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
		}

		setMinimumSize(new Dimension(400, 400));		
		setResizable(true);
		pack();

		button.addActionListener(l -> dispose());
	}

	private JPanel getGeneralTab() {
		JPanel general = new JPanel();		
		general.setLayout(new GridBagLayout());
		{
			Border border = BorderFactory.createEtchedBorder();
			Border margin = BorderFactory.createEmptyBorder(2, 2, 2, 2);

			JLabel supportHeader = new JLabel(Language.I18N.getString("menu.help.info.support"));
			general.add(supportHeader, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
			JTextArea support = new JTextArea();
			support.setBorder(new CompoundBorder(border, margin));				
			support.setEditable(false);
			support.setBackground(new Color(255, 255, 255));
			support.setFont(supportHeader.getFont());

			support.setText(Language.I18N.getString("menu.help.info.support.text"));				
			general.add(support, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));

			String tum_label_text = config.getProject().getGlobal().getLanguage() == LanguageType.EN ?
					"Chair of Geoinformatics,\nTechnical University of Munich, Germany" : "Lehrstuhl für Geoinformatik,\nTechnische Universität München, Deutschland";
			JLabel copyHeader = new JLabel("Copyright");
			general.add(copyHeader, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,15,5,0,5));
			JTextArea copy = new JTextArea();
			copy.setBorder(new CompoundBorder(border, margin));
			copy.setEditable(false);
			copy.setBackground(new Color(255, 255, 255));
			copy.setFont(supportHeader.getFont());
			copy.setText("(C) 2013 - 2019\n" +
					tum_label_text + "\n" +
					"https://www.gis.bgu.tum.de/\n\n" +
					"This program is free software and licensed under the\n" +
					"Apache License, Version 2.0. For a copy of the Apache License see\n<http://www.apache.org/licenses/LICENSE-2.0>.");				
			general.add(copy, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,2,5,5,5));				

			PopupMenuDecorator.getInstance().decorate(support, copy);
		}

		return general;
	}

	private JPanel getPartnerTab() {
		JPanel partner = new JPanel();
		partner.setLayout(new GridBagLayout());
		{
			String text = Language.I18N.getString("menu.help.info.partner");
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

				String tum_label_text = config.getProject().getGlobal().getLanguage() == LanguageType.EN ?
						"Chair of Geoinformatics,<br/>nTechnical University of Munich" : "Lehrstuhl für Geoinformatik,<br/>Technische Universität München";

				JLabel tum_logo = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/org/citydb/gui/images/partner/tum_logo.png"))));
				logos.add(tum_logo, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,10,0,10,0));
				JEditorPane tum_label = new JEditorPane("text/html", "<html><b>" + tum_label_text + "</b><br/>https://www.gis.bgu.tum.de/</html>");
				((HTMLDocument)tum_label.getDocument()).getStyleSheet().addRule(bodyRule);
				tum_label.setEditable(false);
				logos.add(tum_label, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,5,15,5,5));

				JLabel vcs_logo = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/org/citydb/gui/images/partner/vcs_logo.png"))));
				logos.add(vcs_logo, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.NONE,5,5,10,0));
				JEditorPane vcs_label = new JEditorPane("text/html", "<html><b>virtualcitySYSTEMS GmbH, Berlin</b><br/>http://www.virtualcitysystems.de/</html>");
				((HTMLDocument)vcs_label.getDocument()).getStyleSheet().addRule(bodyRule);
				vcs_label.setEditable(false);				
				logos.add(vcs_label, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,5,15,5,5));

				JLabel moss_logo = new JLabel(new ImageIcon(getToolkit().getImage(this.getClass().getResource("/org/citydb/gui/images/partner/moss_logo.png"))));
				logos.add(moss_logo, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.NONE,5,5,5,0));
				JEditorPane moss_label = new JEditorPane("text/html", "<html><b>M.O.S.S. Computer Grafik Systeme GmbH,<br/>Taufkirchen, Germany</b><br/>http://www.moss.de/</html>");
				((HTMLDocument)moss_label.getDocument()).getStyleSheet().addRule(bodyRule);
				moss_label.setEditable(false);				
				logos.add(moss_label, GuiUtil.setConstraints(1,2,1,0,GridBagConstraints.HORIZONTAL,5,15,5,5));

				logos.add(new JLabel(), GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,5,5,0,5));

				PopupMenuDecorator.getInstance().decorate(tum_label, vcs_label, moss_label);
			}		
		}

		return partner;
	}

}
