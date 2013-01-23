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
package de.tub.citydb.gui.components.checkboxtree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import de.tub.citydb.gui.components.checkboxtree.QuadristateButtonModel.State;

/**
 * A renderer for the CheckboxTree. This implementation decorates a
 * DefaultTreeCellRenderer (i.e. a JLabel) with a checkbox, by adding a
 * JCheckbox to the former onto a JPanel. Both can be overridden by subclasses.
 * Note that this renderer separates the checkbox form the label/icon, in that
 * double-clicking the label/icon of this renderer does not toggle the checkbox.
 * 
 * @author boldrini
 * @author bigagli
 */
@SuppressWarnings("serial")
public class DefaultCheckboxTreeCellRenderer extends JPanel implements CheckboxTreeCellRenderer {

    protected QuadristateCheckbox checkBox = new QuadristateCheckbox();

    protected DefaultTreeCellRenderer label = new DefaultTreeCellRenderer();

    public DefaultCheckboxTreeCellRenderer() {
	this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
	add(this.checkBox);
	add(this.label);
	this.checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
	this.setBackground(UIManager.getColor("Tree.textBackground"));
    }

    // @Override
    // public void doLayout() {
    // Dimension d_check = this.checkBox.getPreferredSize();
    // Dimension d_label = this.label.getPreferredSize();
    // int y_check = 0;
    // int y_label = 0;
    // if (d_check.height < d_label.height) {
    // y_check = (d_label.height - d_check.height) / 2;
    // } else {
    // y_label = (d_check.height - d_label.height) / 2;
    // }
    // this.checkBox.setLocation(0, y_check);
    // this.checkBox.setBounds(0, y_check, d_check.width, d_check.height);
    // this.label.setLocation(d_check.width, y_label);
    // this.label.setBounds(d_check.width, y_label, d_label.width,
        // d_label.height);
    // }
    
     @Override
     public Dimension getPreferredSize() {
     Dimension d_check = this.checkBox.getPreferredSize();
     Dimension d_label = this.label.getPreferredSize();
     return new Dimension(d_check.width + d_label.width, (d_check.height <
         d_label.height ? d_label.height : d_check.height));
     }

    /**
         * Decorates this renderer based on the passed in components.
         */
    public Component getTreeCellRendererComponent(JTree tree, Object object, boolean selected, boolean expanded, boolean leaf, int row,
	    boolean hasFocus) {
	/*
         * most of the rendering is delegated to the wrapped
         * DefaultTreeCellRenderer, the rest depends on the TreeCheckingModel
         */
	this.label.getTreeCellRendererComponent(tree, object, selected, expanded, leaf, row, hasFocus);
	if (tree instanceof CheckboxTree) {
	    TreeCheckingModel checkingModel = ((CheckboxTree) tree).getCheckingModel();
	    TreePath path = tree.getPathForRow(row);
	    this.checkBox.setEnabled(checkingModel.isPathEnabled(path));
	    this.label.setEnabled(this.checkBox.isEnabled());
	    boolean checked = checkingModel.isPathChecked(path);
	    boolean greyed = checkingModel.isPathGreyed(path);
	    if (checked && !greyed) {
		this.checkBox.setState(State.CHECKED);
	    }
	    if (!checked && greyed) {
		this.checkBox.setState(State.GREY_UNCHECKED);
	    }
	    if (checked && greyed) {
		this.checkBox.setState(State.GREY_CHECKED);
	    }
	    if (!checked && !greyed) {
		this.checkBox.setState(State.UNCHECKED);
	    }
	}
	return this;
    }

    /**
         * Checks if the (x,y) coordinates are on the Checkbox.
         * 
         * @return boolean
         * @param x
         * @param y
         */
    public boolean isOnHotspot(int x, int y) {
	// TODO: alternativa (ma funge???)
	//return this.checkBox.contains(x, y);
	return (this.checkBox.getBounds().contains(x, y));
    }

    /**
         * Loads an ImageIcon from the file iconFile, searching it in the
         * classpath.Guarda un po'
         */
    protected static ImageIcon loadIcon(String iconFile) {
	try {
	    return new ImageIcon(DefaultCheckboxTreeCellRenderer.class.getClassLoader().getResource(iconFile));
	} catch (NullPointerException npe) { // did not find the resource
	    return null;
	}
    }

    @Override
    public void setBackground(Color color) {
	if (color instanceof ColorUIResource) {
	    color = null;
	}
	super.setBackground(color);
    }

    /**
         * Sets the icon used to represent non-leaf nodes that are expanded.
         */
    public void setOpenIcon(Icon newIcon) {
	this.label.setOpenIcon(newIcon);
    }

    /**
         * Sets the icon used to represent non-leaf nodes that are not expanded.
         */
    public void setClosedIcon(Icon newIcon) {
	this.label.setClosedIcon(newIcon);
    }

    /**
         * Sets the icon used to represent leaf nodes.
         */
    public void setLeafIcon(Icon newIcon) {
	this.label.setLeafIcon(newIcon);
    }

}
