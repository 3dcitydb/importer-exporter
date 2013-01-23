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
package de.tub.citydb.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.OverlayLayout;

import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class SplashScreen extends JWindow {
	private JLabel message;
	private JProgressBar progressBar;
	private ImageIcon icon;
	
	private int step;

	public SplashScreen(int numberOfSteps, int messageX, int messageY, Color messageColor) {
		icon = new ImageIcon(getToolkit().getImage(this.getClass().getResource("/resources/img/splash/splash.png")));
		
		init(numberOfSteps, messageX, messageY, messageColor);
	}

	private void init(int numberOfSteps, int messageX, int messageY, Color messageColor) {
		JPanel content = new JPanel() {
			public boolean isOptimizedDrawingEnabled() {
				return false;
			}
		};
		
		content.setLayout(new OverlayLayout(content));
		content.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		
		JPanel dynamicContent = new JPanel();
		dynamicContent.setOpaque(false);
		dynamicContent.setLayout(new GridBagLayout());
		
		message = new JLabel();
		message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		message.setForeground(messageColor);
		
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(icon.getIconWidth(), 18));
		progressBar.setIndeterminate(false);
		progressBar.setMaximum(numberOfSteps);
		progressBar.setVisible(false);
		
		GridBagConstraints c = GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL, 5 + messageY, 5 + messageX, 5, 5);
		c.anchor = GridBagConstraints.NORTH;
		dynamicContent.add(message, c);
		
		c = GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5);
		c.anchor = GridBagConstraints.SOUTH;
		dynamicContent.add(progressBar, c);
		
		dynamicContent.setAlignmentX(0f);
		dynamicContent.setAlignmentY(0f);
		content.add(dynamicContent);
		
		JLabel image = new JLabel(icon);
		image.setAlignmentX(0f);
		image.setAlignmentY(0f);
		content.add(image);
		
		add(content, BorderLayout.CENTER);
		
		// center on screen
		Toolkit t = Toolkit.getDefaultToolkit();
		Insets frame_insets = t.getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		int frame_insets_x = frame_insets.left + frame_insets.right;
		int frame_insets_y = frame_insets.bottom + frame_insets.top;
		
		Dimension dim = t.getScreenSize();
		int x = (dim.width - icon.getIconWidth() - frame_insets_x) / 2;
		int y = (dim.height - icon.getIconHeight() - frame_insets_y) / 2;		
		setMinimumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		setLocation(x, y);
		setAlwaysOnTop(true);
	}
	
	public void setMessage(String message) {
		this.message.setText(message);
	}
	
	public void nextStep() {
		if (step == 0)
			progressBar.setVisible(true);
		
		progressBar.setValue(++step);
	}
	
	public void close() {
		dispose();
	}
}
