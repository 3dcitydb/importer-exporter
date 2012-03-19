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
package de.tub.citydb.gui.components.checkboxtree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 * The renderer for a cell in a CheckboxTree.
 * 
 * @author bigagli
 */
public interface CheckboxTreeCellRenderer extends TreeCellRenderer {

    /**
         * This method is redeclared just to underline that the implementor has
         * to properly display the checking/graying state of <code>value</code>.
         * This should go in the method parameters, but would imply changes to
         * Swing classes that were considered unpractical. For example in
         * DefaultCheckboxTreeCellRenderer the following code is used to get the
         * checking/graying states:
         * 
         * <pre>
         * TreeCheckingModel checkingModel = ((CheckboxTree) tree).getCheckingModel();
         * 
         * TreePath path = tree.getPathForRow(row);
         * 
         * boolean enabled = checkingModel.isPathEnabled(path);
         * 
         * boolean checked = checkingModel.isPathChecked(path);
         * 
         * boolean greyed = checkingModel.isPathGreyed(path);
         * </pre>
         * 
         * You could use a QuadristateCheckbox to properly renderer the states
         * (as in DefaultCheckboxTreeCellRenderer).
         * 
         * @see TreeCellRenderer#getTreeCellRendererComponent
         */
    Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
	    boolean hasFocus);

    /**
         * Returns whether the specified relative coordinates insist on the
         * intended checkbox control. May be used by a mouse listener to figure
         * out whether to toggle a node or not.
         */
    public boolean isOnHotspot(int x, int y);

}
