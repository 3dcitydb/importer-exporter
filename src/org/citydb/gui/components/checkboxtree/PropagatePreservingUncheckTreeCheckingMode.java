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
 * PropagatePreservingUncheckTreeCheckingMode define a TreeCheckingMode with
 * down and up recursion of the check when nodes are clicked. It propagates the
 * change not only to descendants but also to ancestors. With regard to
 * descendants this mode behaves exactly like the Propagate mode. With regard to
 * ancestors it checks/unchecks them as needed so that a node is unchecked if
 * and only if all of its children are unchecked.
 * 
 * @author Boldrini
 */
public class PropagatePreservingUncheckTreeCheckingMode extends TreeCheckingMode {

    PropagatePreservingUncheckTreeCheckingMode(DefaultTreeCheckingModel model) {
	super(model);
    }

    @Override
    public void checkPath(TreePath path) {
	// check is propagated to children
	this.model.checkSubTree(path);
	// check all the ancestors with subtrees checked
	TreePath[] parents = new TreePath[path.getPathCount()];
	parents[0] = path;
	// boolean uncheckAll = false;
	boolean greyAll = false;
	for (int i = 1; i < parents.length; i++) {
	    parents[i] = parents[i - 1].getParentPath();
	    this.model.addToCheckedPathsSet(parents[i]);
	    if (greyAll) {
		this.model.addToGreyedPathsSet(parents[i]);
	    } else {
		switch (this.model.getChildrenChecking(parents[i])) {
		case HALF_CHECKED:
		    this.model.addToGreyedPathsSet(parents[i]);
		    greyAll = true;
		    break;
		case ALL_UNCHECKED:
		    System.err.println("This should not happen (PropagatePreservingUncheckTreeCheckingMode)");
		    break;
		case ALL_CHECKED:
		    this.model.removeFromGreyedPathsSet(parents[i]);
		    break;
		default:
		case NO_CHILDREN:
		    System.err.println("This should not happen (PropagatePreservingCheckTreeCheckingMode)");
		    break;
		}
	    }
	}
    }

    @Override
    public void uncheckPath(TreePath path) {
	// uncheck is propagated to children
	this.model.uncheckSubTree(path);
	TreePath parentPath = path;
	// check all the ancestors with subtrees checked
	while ((parentPath = parentPath.getParentPath()) != null) {
	    switch (this.model.getChildrenChecking(parentPath)) {
	    case HALF_CHECKED:
		this.model.addToCheckedPathsSet(parentPath);
		this.model.addToGreyedPathsSet(parentPath);
		break;
	    case ALL_UNCHECKED:
		this.model.removeFromCheckedPathsSet(parentPath);
		this.model.removeFromGreyedPathsSet(parentPath);
		break;
	    case ALL_CHECKED:
		System.err.println("This should not happen (PropagatePreservingUncheckTreeCheckingMode)");
		break;
	    default:
	    case NO_CHILDREN:
		System.err.println("This should not happen (PropagatePreservingCheckTreeCheckingMode)");
		break;
	    }
	}
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
	if (this.model.isPathChecked(parent)) {
	    // System.out.println(parent +" was removed (not checked)");
	    if (this.model.getChildrenPath(parent).length != 0) {
		if (!this.model.pathHasChildrenWithValue(parent, true)) {
		    // System.out.println("uncheking it");
		    uncheckPath(parent);
		}
	    }
	}
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
	    checkPath(parent);
	} else {
	    uncheckPath(parent);
	}
    }

}
