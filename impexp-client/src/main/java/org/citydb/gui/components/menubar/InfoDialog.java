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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LanguageType;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

public class InfoDialog extends JDialog {
	private final Config config;

	public InfoDialog(Config config, JFrame frame) {
		super(frame, Language.I18N.getString("menu.help.info.label"), true);
		this.config = config;

		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		setLayout(new GridBagLayout());
		JPanel header = new JPanel();
		header.setLayout(new GridBagLayout());
		{
			JLabel name = new JLabel("" + getClass().getPackage().getImplementationTitle());
			JLabel version = new JLabel("Version " + getClass().getPackage().getImplementationVersion());
			name.setFont(name.getFont().deriveFont(Font.BOLD));

			JLabel img = new JLabel(new ImageIcon(getClass().getResource("/org/citydb/gui/logos/logo.png")));

			header.add(name, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 10, 0, 20));
			header.add(version, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, 0, 10, 0, 20));
			header.add(img, GuiUtil.setConstraints(1, 0, 1, 2, 0, 1, GridBagConstraints.NONE, 5, 0, 5, 5));
		}

		header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));

		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.add(getGeneralTab(), Language.I18N.getString("menu.help.info.tab.general"));
		tabs.add(getPartnerTab(), Language.I18N.getString("menu.help.info.tab.partner"));
		JButton button = new JButton(Language.I18N.getString("common.button.ok"));

		add(header, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(tabs, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(button, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 15, 10, 10, 10));

		pack();

		button.addActionListener(e -> dispose());
	}

	private JPanel getGeneralTab() {
		JPanel general = new JPanel();
		general.setLayout(new GridBagLayout());

		String text = Language.I18N.getString("menu.help.info.support.text");
		JLabel support = new JLabel(MessageFormat.format(text, "<html><a href=\"\">https://github.com/3dcitydb</a>"));
		support.setCursor(new Cursor(Cursor.HAND_CURSOR));
		support.addMouseListener(openInBrowserAdapter("https://github.com/3dcitydb"));
		support.setIcon(new FlatSVGIcon("org/citydb/gui/icons/question_dialog.svg"));
		support.setIconTextGap(10);

		TitledPanel supportPanel = new TitledPanel()
				.withTitle(Language.I18N.getString("menu.help.info.support"))
				.build(support);

		String tum = config.getGlobalConfig().getLanguage() == LanguageType.EN ?
				"Chair of Geoinformatics,\nTechnical University of Munich, Germany" :
				"Lehrstuhl für Geoinformatik,\nTechnische Universität München, Deutschland";

		JTextArea copyright = createTextArea("(C) 2013 - 2020\n" +
				tum + "\n" +
				"https://www.lrg.tum.de/gis\n\n" +
				"This program is free software and licensed under the\n" +
				"Apache License, Version 2.0. For a copy of the Apache License\n" +
				"see <http://www.apache.org/licenses/LICENSE-2.0>.");

		TitledPanel copyrightPanel = new TitledPanel()
				.withTitle("Copyright")
				.withMargin(new Insets(0, 0, 0, 0))
				.build(copyright);

		general.add(supportPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 10, 0, 10));
		general.add(copyrightPanel, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 0, 10, 0, 10));

		PopupMenuDecorator.getInstance().decorate(support, copyright);

		return general;
	}

	private JPanel getPartnerTab() {
		JPanel partner = new JPanel();
		partner.setLayout(new GridBagLayout());

		String text = Language.I18N.getString("menu.help.info.partner");
		JLabel supportText = new JLabel(MessageFormat.format(text, getClass().getPackage().getImplementationTitle()));

		JPanel logos = new JPanel();
		logos.setLayout(new GridBagLayout());
		{
			String tum = config.getGlobalConfig().getLanguage() == LanguageType.EN ?
					"Chair of Geoinformatics,<br/>Technical University of Munich" :
					"Lehrstuhl für Geoinformatik,<br/>Technische Universität München";

			JLabel tumLogo = new JLabel(new ImageIcon(getClass().getResource("/org/citydb/gui/logos/tum_logo.png")));
			Component tumInfo = createInfoText(tum, "https://www.lrg.tum.de/gis");

			JLabel vcsLogo = new JLabel(new ImageIcon(getClass().getResource("/org/citydb/gui/logos/vcs_logo.png")));
			Component vcsInfo = createInfoText("Virtual City Systems, Berlin", "https://www.vc.systems");

			JLabel mossLogo = new JLabel(new ImageIcon(getClass().getResource("/org/citydb/gui/logos/moss_logo.png")));
			Component mossInfo = createInfoText("M.O.S.S. Computer Grafik Systeme GmbH,<br/>Taufkirchen, Germany",
					"https://www.moss.de");

			logos.add(tumLogo, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 5, 0));
			logos.add(tumInfo, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 20, 5, 0));
			logos.add(vcsLogo, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.NONE, 5, 0, 5, 0));
			logos.add(vcsInfo, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, 20, 5, 0));
			logos.add(mossLogo, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.NONE, 5, 5, 0, 0));
			logos.add(mossInfo, GuiUtil.setConstraints(1, 3, 1, 0, GridBagConstraints.HORIZONTAL, 5, 20, 0, 0));
		}

		partner.add(supportText, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 10, 0, 10));
		partner.add(logos, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 15, 20, 0, 10));

		return partner;
	}

	private JTextArea createTextArea(String text) {
		JTextArea textArea = new JTextArea(text);
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		textArea.setFont(UIManager.getFont("Label.font"));
		textArea.setBackground(UIManager.getColor("TabbedPane.background"));
		return textArea;
	}

	private Component createInfoText(String name, String url) {
		Box box = Box.createVerticalBox();
		{
			JLabel label = new JLabel("<html><b>" + name + "</b><br/></html>");
			JLabel link = new JLabel("<html><a href=\"\">" + url + "</a></html>");
			link.setCursor(new Cursor(Cursor.HAND_CURSOR));
			link.addMouseListener(openInBrowserAdapter(url));
			box.add(label);
			box.add(link);
		}

		return box;
	}

	private MouseAdapter openInBrowserAdapter(String url) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(URI.create(url));
				} catch (IOException ioException) {
					//
				}
			}
		};
	}
}
