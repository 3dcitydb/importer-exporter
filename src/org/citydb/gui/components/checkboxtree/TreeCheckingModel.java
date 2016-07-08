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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * The model for checking/unchecking the nodes of a CheckboxTree. Alterations of
 * a node state may propagate on descendants/ascendants, according to the
 * behaviour of the checking model. See CheckingMode for the available
 * behaviours.
 * 
 * @author bigagli
 * @author boldrini
 */
public interface TreeCheckingModel {

    /**
         * The checking behaviours provided by this class.
         * 
         * @author boldrini
         */
    public enum CheckingMode {

	/**
         * The check is not propagated at all, toggles the just-clicked checkbox
         * only.
         */
	SIMPLE,

	/**
         * Toggles the just-clicked checkbox and propagates the change down. In
         * other words, if the clicked checkbox is checked all the descendants
         * will be checked; otherwise all the descendants will be unchecked
         */
	PROPAGATE,

	/**
         * The check is propagated, like the Propagate mode to descendants. If a
         * user unchecks a checkbox the uncheck will also be propagated to
         * ancestors.
         */
	PROPAGATE_UP_UNCHECK,

	/**
         * Propagates the change not only to descendants but also to ancestors.
         * With regard to descendants this mode behaves exactly like the
         * Propagate mode. With regard to ancestors it checks/unchecks them as
         * needed so that a node is unchecked if and only if all of its children
         * are unchecked.
         */
	PROPAGATE_PRESERVING_UNCHECK,

	/**
         * Propagates the change not only to descendants but also to ancestors.
         * With regard to descendants this mode behaves exactly like the
         * Propagate mode. With regard to ancestors it checks/unchecks them as
         * needed so that a node is checked if and only if all of its children
         * are checked.
         */
	PROPAGATE_PRESERVING_CHECK
    }

    /**
         * Returns whether the specified path is checked.
         */
    public boolean isPathChecked(TreePath path);

    /**
         * Returns whether the specified path checking state can be toggled.
         */
    public boolean isPathEnabled(TreePath path);

    /**
         * Returns whether the specified path is greyed.
         */
    public boolean isPathGreyed(TreePath path);

    /**
         * Alter (check/uncheck) the checking state of the specified path if
         * possible and also propagate the new state if needed by the mode.
         */
    public void toggleCheckingPath(TreePath pathForRow);

    /**
         * add a path to the checked paths set
         * 
         * @param path the path to be added.
         */
    public void addCheckingPath(TreePath path);

    /**
         * add paths to the checked paths set
         * 
         * @param paths the paths to be added.
         */
    public void addCheckingPaths(TreePath[] paths);

    /**
         * remove a path from the checked paths set
         * 
         * @param path the path to be added.
         */
    public void removeCheckingPath(TreePath path);

    /**
         * remove paths from the checked paths set
         * 
         * @param paths the paths to be added.
         */
    public void removeCheckingPaths(TreePath[] paths);

    /**
         * Sets whether or not the path is enabled.
         * 
         * @param path the path to enable/disable
         */
    public void setPathEnabled(TreePath path, boolean enable);

    /**
         * Sets whether or not the paths are enabled.
         * 
         * @param paths the paths to enable/disable
         */
    public void setPathsEnabled(TreePath[] paths, boolean enable);

    /**
         * @return Returns the paths that are in the checking set.
         */
    public TreePath[] getCheckingPaths();

    /**
         * @return Returns the paths that are in the checking set and are the
         *         (upper) roots of checked trees.
         */
    public TreePath[] getCheckingRoots();

    /**
         * @return Returns the paths that are in the greying set.
         */
    public TreePath[] getGreyingPaths();

    /**
         * Set the checking to paths.
         */
    public void setCheckingPaths(TreePath[] paths);

    /**
         * Set the checking to path.
         */
    public void setCheckingPath(TreePath path);

    /**
         * Clears the checking.
         */
    public void clearChecking();

    /**
         * Set the checking mode.
         * 
         * @param mode The checkingMode to set.
         */
    public void setCheckingMode(CheckingMode mode);

    /**
         * @return Returns the CheckingMode.
         */
    public CheckingMode getCheckingMode();

    /**
         * Get the listener for the <code>TreeModelEvent</code> posted after
         * the tree changes.
         * 
         * @deprecated use get/setTreeModel instead
         */
    @Deprecated
    public TreeModelListener getTreeModelListener();

    /**
         * Adds x to the list of listeners that are notified each time the set
         * of checking TreePaths changes.
         * 
         * @param x the new listener to be added
         */
    public void addTreeCheckingListener(TreeCheckingListener x);

    /**
         * Removes x from the list of listeners that are notified each time the
         * set of checking TreePaths changes.
         * 
         * @param x the listener to remove
         */
    public void removeTreeCheckingListener(TreeCheckingListener x);

    /**
         * Returns the tree model to which this checking model is bound, or null
         * if not set.
         */
    public TreeModel getTreeModel();

    /**
         * Set the tree model to which this checking model is (possibly) bound.
         * A checking model may use a tree model to propagate the checking. A
         * checking model may also listen to the model, to adjust the checking
         * upon model events. The current checking is cleared.
         */
    public void setTreeModel(TreeModel model);

}
