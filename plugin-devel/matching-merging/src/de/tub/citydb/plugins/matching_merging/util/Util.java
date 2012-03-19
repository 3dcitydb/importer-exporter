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
package de.tub.citydb.plugins.matching_merging.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import de.tub.citydb.plugins.matching_merging.config.Workspace;

public class Util {
	public static ResourceBundle I18N;

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
	
	public static boolean checkWorkspaceTimestamp(Workspace workspace) {
		String timestamp = workspace.getTimestamp().trim();
		boolean success = true;
		
		if (timestamp.length() > 0) {		
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
			format.setLenient(false);
			try {
				format.parse(timestamp);				
			} catch (java.text.ParseException e) {
				success = false;
			}
		}

		workspace.setTimestamp(timestamp);
		return success;
	}
}
