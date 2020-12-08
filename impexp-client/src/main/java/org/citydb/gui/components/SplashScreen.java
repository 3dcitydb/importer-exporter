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
package org.citydb.gui.components;

import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class SplashScreen extends JWindow {
	private final JLabel message;
	private final JProgressBar progressBar;

	public SplashScreen(int left, int top, Color messageColor) {
		Object arc = UIManager.get("ProgressBar.arc");
		UIManager.put("ProgressBar.arc", 0);

		ImageIcon icon = new ImageIcon(getToolkit().getImage(this.getClass().getResource("/org/citydb/gui/splash/splash.png")));
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
		progressBar.setIndeterminate(false);
		progressBar.setVisible(false);

		Component placeholder = Box.createVerticalStrut(progressBar.getPreferredSize().height);

		dynamicContent.add(message, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, top, left, 0, 5));
		dynamicContent.add(placeholder, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		dynamicContent.add(progressBar, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		
		dynamicContent.setAlignmentX(0);
		dynamicContent.setAlignmentY(0);
		content.add(dynamicContent);
		
		JLabel image = new JLabel(icon);
		image.setAlignmentX(0);
		image.setAlignmentY(0);
		content.add(image);
		
		add(content, BorderLayout.CENTER);
		UIManager.put("ProgressBar.arc", arc);
		
		// center on screen
		GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());

		Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
		AffineTransform transform = screen.getDefaultConfiguration().getDefaultTransform();
		screenBounds.width = screenBounds.width - (int) ((screenInsets.left + screenInsets.right) / transform.getScaleX());
		screenBounds.height = screenBounds.height - (int) ((screenInsets.top + screenInsets.bottom) / transform.getScaleY());

		int x = (screenBounds.width - icon.getIconWidth()) / 2;
		int y = (screenBounds.height - icon.getIconHeight()) / 2;
		setMinimumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		setLocation(x, y);
		setAlwaysOnTop(true);
	}

	public void setMessage(String message) {
		this.message.setText(message);
	}

	public void nextStep(int current, int maximum) {
		if (current == 1) {
			progressBar.setMaximum(maximum);
			progressBar.setVisible(true);
		}

		progressBar.setValue(current);
	}

	public void close() {
		dispose();
	}
}
