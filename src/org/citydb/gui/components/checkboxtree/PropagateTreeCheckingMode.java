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

import javax.swing.tree.TreePath;

/**
 * PropagateTreeCheckingMode define a TreeCheckingMode with down recursion of
 * the check when nodes are clicked. It toggles the just-clicked checkbox and
 * propagates the change down. In other words, if the clicked checkbox is
 * checked all the descendants will be checked; otherwise all the descendants
 * will be unchecked.
 * 
 * @author Boldrini
 */
public class PropagateTreeCheckingMode extends TreeCheckingMode {

    PropagateTreeCheckingMode(DefaultTreeCheckingModel model) {
	super(model);
    }

    @Override
    public void checkPath(TreePath path) {
	this.model.checkSubTree(path);
	this.model.updatePathGreyness(path);
	this.model.updateAncestorsGreyness(path);
    }

    @Override
    public void uncheckPath(TreePath path) {
	this.model.uncheckSubTree(path);
	this.model.updatePathGreyness(path);
	this.model.updateAncestorsGreyness(path);
    }

    /*
         * (non-Javadoc)
         * 
         * @see it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingMode#updateCheckAfterChildrenInserted(javax.swing.tree.TreePath)
         */
    @Override
    public void updateCheckAfterChildrenInserted(TreePath parent) {
	if (this.model.isPathChecked(parent)) {
	    this.model.checkSubTree(parent);
	} else {
	    this.model.uncheckSubTree(parent);
	}
    }

    /*
         * (non-Javadoc)
         * 
         * @see it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingMode#updateCheckAfterChildrenRemoved(javax.swing.tree.TreePath)
         */
    @Override
    public void updateCheckAfterChildrenRemoved(TreePath parent) {
	this.model.updatePathGreyness(parent);
	this.model.updateAncestorsGreyness(parent);
    }

    /*
         * (non-Javadoc)
         * 
         * @see it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingMode#updateCheckAfterStructureChanged(javax.swing.tree.TreePath)
         */
    @Override
    public void updateCheckAfterStructureChanged(TreePath parent) {
	if (this.model.isPathChecked(parent)) {
	    this.model.checkSubTree(parent);
	} else {
	    this.model.uncheckSubTree(parent);
	}
    }

}
