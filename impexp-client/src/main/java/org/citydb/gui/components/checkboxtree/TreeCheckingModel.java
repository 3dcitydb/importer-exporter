/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
 * The interface of a model for checking/unchecking the nodes of a CheckboxTree.
 * Alterations of a node state may propagate to descendants/ancestors, according
 * to the behaviour of the checking model. See CheckingMode for the available
 * behaviours.
 *
 * @author bigagli
 * @author boldrini
 */
public interface TreeCheckingModel {

    /**
     * The checking behaviors supported by this class.
     */
    public enum CheckingMode {
        /*
         * TODO: this should be moved to DefaultTreeCheckingModel, together with
         * TreeCheckingMode
         */

        /**
         * Toggles the clicked checkbox and propagates the change down. In other
         * words, if the clicked checkbox becomes checked, all the descendants
         * will be checked; otherwise, all the descendants will be unchecked.
         */
        PROPAGATE,

        /**
         * Propagates the change not only to descendants but also to ancestors.
         * With regard to descendants this mode behaves exactly like the
         * Propagate mode. With regard to ancestors it checks/unchecks them as
         * needed so that a node is checked if and only if all of its children
         * are checked.
         */
        PROPAGATE_PRESERVING_CHECK,

        /**
         * Propagates the change not only to descendants but also to ancestors.
         * With regard to descendants this mode behaves exactly like the
         * Propagate mode. With regard to ancestors it checks/unchecks them as
         * needed so that a node is unchecked if and only if all of its children
         * are unchecked.
         */
        PROPAGATE_PRESERVING_UNCHECK,

        /**
         * The change is propagated to descendants like in the PROPAGATE mode.
         * Moreover, if the checkbox becomes unchecked, all the ancestors will
         * be unchecked.
         */
        PROPAGATE_UP_UNCHECK,

        /**
         * The check is not propagated at all, toggles the clicked checkbox
         * only.
         */
        SIMPLE,

        /**
         * The check is not propagated at all, toggles the clicked checkbox
         * only. Only one checkbox is allowed to be checked at any given time.
         */
        SINGLE

    }

    /**
     * add a path to the checking set.
     *
     * @param path the path to be added.
     */
    public void addCheckingPath(TreePath path);

    /**
     * add paths to the checking set.
     *
     * @param paths the paths to be added.
     */
    public void addCheckingPaths(TreePath[] paths);

    /**
     * Adds the specified listener to the list of those being notified upon
     * changes in the the checking set.
     *
     * @param tcl the new listener to be added.
     */
    public void addTreeCheckingListener(TreeCheckingListener tcl);

    /**
     * Clears the checking.
     */
    public void clearChecking();

    /**
     * @return Returns the CheckingMode.
     */
    public CheckingMode getCheckingMode();

    /**
     * @return Returns the paths that are in the checking set.
     */
    public TreePath[] getCheckingPaths();

    /**
     * @return Returns the paths that are in the checking set and are the
     * (upper) roots of checked trees.
     */
    public TreePath[] getCheckingRoots();

    /**
     * @return Returns the paths that are in the greying set.
     */
    public TreePath[] getGreyingPaths();

    /**
     * Returns true if the item identified by the path is currently checked.
     *
     * @param path a <code>TreePath</code> identifying a node
     * @return true if the node is checked
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
     * Removes a path from the checking set.
     *
     * @param path the path to be removed.
     */
    public void removeCheckingPath(TreePath path);

    /**
     * Remove the specified paths from the checking set.
     *
     * @param paths the paths to be added.
     */
    public void removeCheckingPaths(TreePath[] paths);

    /**
     * Removes the specified listener from the list of those being notified upon
     * changes in the checking set.
     *
     * @param tcl the listener to remove.
     */
    public void removeTreeCheckingListener(TreeCheckingListener tcl);

    /**
     * Sets the specified checking mode.
     *
     * @param mode the checking mode to set.
     */
    public void setCheckingMode(CheckingMode mode);

    /**
     * (Re)sets the checking to the specified path.
     */
    public void setCheckingPath(TreePath path);

    /**
     * (Re)sets the checking to the specified paths.
     */
    public void setCheckingPaths(TreePath[] paths);

    /**
     * Sets whether or not the specified path can be toggled.
     *
     * @param path the path to enable/disable
     */
    public void setPathEnabled(TreePath path, boolean enable);

    /**
     * Sets whether or not the specified paths can be toggled.
     *
     * @param paths the paths to enable/disable
     */
    public void setPathsEnabled(TreePath[] paths, boolean enable);

    /**
     * Toggles (check/uncheck) the checking state of the specified path, if this
     * is enabled, and possibly propagate the change, according to the checking
     * mode.
     */
    public void toggleCheckingPath(TreePath pathForRow);

}
