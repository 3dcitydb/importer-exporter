package de.tub.citydb.gui.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JMenuItem;

import de.tub.citydb.config.internal.Internal;

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
	
	public static void setMnemonic(JMenuItem item, String labelKey, String indexKey) {
		try {
			int index = Integer.valueOf(Internal.I18N.getString(indexKey));
			char mnemonic = Internal.I18N.getString(labelKey).charAt(index);
			item.setMnemonic(mnemonic);
			item.setDisplayedMnemonicIndex(index);
		} catch (NumberFormatException e) {
			//
		} catch (IndexOutOfBoundsException e2) {
			//
		}
	}

}
