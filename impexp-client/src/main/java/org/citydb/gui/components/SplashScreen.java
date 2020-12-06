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

		dynamicContent.add(message, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL, top, left, 0, 5));
		dynamicContent.add(placeholder, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		dynamicContent.add(progressBar, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		
		dynamicContent.setAlignmentX(0f);
		dynamicContent.setAlignmentY(0f);
		content.add(dynamicContent);
		
		JLabel image = new JLabel(icon);
		image.setAlignmentX(0f);
		image.setAlignmentY(0f);
		content.add(image);
		
		add(content, BorderLayout.CENTER);
		UIManager.put("ProgressBar.arc", arc);
		
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
