/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.gui.util;

import org.citydb.config.i18n.Language;

import javax.swing.*;
import java.awt.*;

public class GuiUtil {

	public static GridBagConstraints setConstraints(int gridx, int gridy, double weightx, double weighty, int fill,
			int insetTop, int insetLeft, int insetBottom, int insetRight) {
		GridBagConstraints constraint = new GridBagConstraints();
		constraint.gridx = gridx;
		constraint.gridy = gridy;
		constraint.weightx = weightx;
		constraint.weighty = weighty;
		constraint.fill = fill;
		constraint.insets = new Insets(insetTop, insetLeft, insetBottom, insetRight);
		return constraint;
	}

	public static GridBagConstraints setConstraints(int gridx, int gridy, double weightx, double weighty, int anchor, int fill,
			int insetTop, int insetLeft, int insetBottom, int insetRight) {
		GridBagConstraints constraint = setConstraints(gridx, gridy, weightx, weighty, fill,
				insetTop, insetLeft, insetBottom, insetRight);
		constraint.anchor = anchor;
		return constraint;
	}

	public static GridBagConstraints setConstraints(int gridx, int gridy, int gridwidth, int gridheight, 
			double weightx, double weighty, int fill,
			int insetTop, int insetLeft, int insetBottom, int insetRight) {
		GridBagConstraints constraint = setConstraints(gridx, gridy, weightx, weighty, fill,
				insetTop, insetLeft, insetBottom, insetRight);
		constraint.gridwidth = gridwidth;
		constraint.gridheight = gridheight;
		return constraint;
	}

	public static void setMnemonic(JMenuItem item, String label, int index) {
		try {
			char mnemonic = label.charAt(index);
			item.setMnemonic(mnemonic);
			item.setDisplayedMnemonicIndex(index);
		}  catch (IndexOutOfBoundsException e) {
			//
		}
	}

	public static void setMnemonic(JMenuItem item, String labelKey, String indexKey) {		
		try {
			setMnemonic(item, Language.I18N.getString(labelKey), Integer.valueOf(Language.I18N.getString(indexKey)));
		} catch (NumberFormatException e) {
			//
		}
	}

	public static Color hexToColor(String hex) {
		if (hex != null && hex.matches("^#[0-9a-fA-F]{6,8}")) {
			hex = hex.replace("#", "");
			int r, g, b, a = 255;

			switch (hex.length()) {
				case 8:
					a = Integer.valueOf(hex.substring(0, 2), 16);
					hex = hex.substring(2);
				case 6:
					r = Integer.valueOf(hex.substring(0, 2), 16);
					g = Integer.valueOf(hex.substring(2, 4), 16);
					b = Integer.valueOf(hex.substring(4, 6), 16);
					return new Color(r, g, b, a);
			}
		}

		return null;
	}

	public static String colorToHex(Color color) {
		return color != null ? "#" + Integer.toHexString(color.getRGB()) : null;
	}

}
