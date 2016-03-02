/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
