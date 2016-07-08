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
package org.citydb.gui.components.checkboxtree;

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
