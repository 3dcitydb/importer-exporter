/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
 * Convenience class representing an empty tree checking model (cf. the Null
 * Object pattern), whose paths are always enabled, unchecked and ungreyed. This
 * class is a singleton.
 *
 * @author Lorenzo Bigagli
 */
public class NullTreeCheckingModel implements TreeCheckingModel {

    private final static NullTreeCheckingModel singleton;

    static {
        singleton = new NullTreeCheckingModel();
    }

    private NullTreeCheckingModel() {
    }

    public static NullTreeCheckingModel getInstance() {
        return singleton;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#addCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void addCheckingPath(TreePath path) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#addCheckingPaths
     * (javax.swing.tree.TreePath[])
     */
    public void addCheckingPaths(TreePath[] paths) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @seeit.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#
     * addTreeCheckingListener
     * (eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingListener)
     */
    public void addTreeCheckingListener(TreeCheckingListener tcl) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#clearChecking
     * ()
     */
    public void clearChecking() {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#getCheckingMode
     * ()
     */
    public CheckingMode getCheckingMode() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#getCheckingPaths
     * ()
     */
    public TreePath[] getCheckingPaths() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#getCheckingRoots
     * ()
     */
    public TreePath[] getCheckingRoots() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#getGreyingPaths
     * ()
     */
    public TreePath[] getGreyingPaths() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#isPathChecked
     * (javax.swing.tree.TreePath)
     */
    public boolean isPathChecked(TreePath path) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#isPathEnabled
     * (javax.swing.tree.TreePath)
     */
    public boolean isPathEnabled(TreePath path) {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#isPathGreyed
     * (javax.swing.tree.TreePath)
     */
    public boolean isPathGreyed(TreePath path) {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#removeCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void removeCheckingPath(TreePath path) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @seeit.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#
     * removeCheckingPaths(javax.swing.tree.TreePath[])
     */
    public void removeCheckingPaths(TreePath[] paths) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @seeit.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#
     * removeTreeCheckingListener
     * (eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingListener)
     */
    public void removeTreeCheckingListener(TreeCheckingListener tcl) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#setCheckingMode
     * (eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode)
     */
    public void setCheckingMode(CheckingMode mode) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#setCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void setCheckingPath(TreePath path) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#setCheckingPaths
     * (javax.swing.tree.TreePath[])
     */
    public void setCheckingPaths(TreePath[] paths) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#setPathEnabled
     * (javax.swing.tree.TreePath, boolean)
     */
    public void setPathEnabled(TreePath path, boolean enable) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#setPathsEnabled
     * (javax.swing.tree.TreePath[], boolean)
     */
    public void setPathsEnabled(TreePath[] paths, boolean enable) {
        // nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.floraresearch.lablib.gui.checkboxtree.TreeCheckingModel#toggleCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void toggleCheckingPath(TreePath pathForRow) {
        // nothing to do (cf. the Null Object pattern)
    }

}
