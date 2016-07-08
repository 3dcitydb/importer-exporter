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
package org.citydb.util.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JMenuItem;

import org.citydb.config.language.Language;

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

}
