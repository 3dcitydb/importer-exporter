package de.tub.citydb.gui.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

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

}
