/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.gui.menubar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.citydb.config.language.Language;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ReadMeDialog extends JDialog {

	public ReadMeDialog(JFrame frame) {
		super(frame, Language.I18N.getString("menu.help.readMe.label"), true);
		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton button = new JButton(Language.I18N.getString("common.button.ok"));		

		setLayout(new GridBagLayout()); {
			JPanel main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				JLabel readMeHeader = new JLabel(Language.I18N.getString("menu.help.readMe.information"));
				main.add(readMeHeader, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,10,2,2,5));

				JTextArea readMe = new JTextArea();
				readMe.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));				
				readMe.setEditable(false);
				readMe.setBackground(Color.WHITE);
				readMe.setFont(new Font(Font.MONOSPACED, 0, 11));

				try {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(this.getClass().getResourceAsStream("/README.txt"), "UTF-8"));

					// read address template
					StringBuilder builder = new StringBuilder();
					String line = null;	
					while ((line = in.readLine()) != null) {
						builder.append(line);
						builder.append("\n");
					}

					readMe.setText(builder.toString());					

				} catch (Exception e) {
					readMe.setText("The README.txt file could not be found.\n\n" + "" +
							"Please refer to the README.txt file provided with the installation package.\n\n");
				}

				readMe.setCaretPosition(0);
				PopupMenuDecorator.getInstance().decorate(readMe);

				JScrollPane scroll = new JScrollPane(readMe);
				scroll.setAutoscrolls(true);
				scroll.setBorder(BorderFactory.createEtchedBorder());

				main.add(scroll, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,2,0,0,0));

			}

			button.setMargin(new Insets(button.getMargin().top, 25, button.getMargin().bottom, 25));
			add(button, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
		}

		setPreferredSize(new Dimension(600, 400));
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
