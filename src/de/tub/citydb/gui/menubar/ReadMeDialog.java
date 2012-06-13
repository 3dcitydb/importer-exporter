/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ReadMeDialog extends JDialog {

	public ReadMeDialog(JFrame frame) {
		super(frame, Internal.I18N.getString("menu.help.readMe.label"), true);
		initGUI();
	}

	private void initGUI() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton button = new JButton(Internal.I18N.getString("common.button.ok"));		

		setLayout(new GridBagLayout()); {
			JPanel main = new JPanel();
			add(main, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			main.setLayout(new GridBagLayout());
			{
				JLabel readMeHeader = new JLabel(Internal.I18N.getString("menu.help.readMe.information"));
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
