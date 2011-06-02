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
