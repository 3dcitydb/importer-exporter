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

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Vector;

/**
 * The default checking model, providing methods for storing and retrieving the
 * checked TreePaths. Adding/removing paths is delegated to a CheckingMode
 * instance. This implementation is based on TreePath only and does not take
 * advantage of TreeNode convenience methods (this is left to future/alternative
 * implementations).
 *
 * @author Lorenzo Bigagli
 * @author Enrico Boldrini
 */
// TODO: DefaultTreeCheckingModel allows to set null tree models, which will
// however
// cause nullPointerException (setTreeModel calls clearChecking...)
// Fix, either allowing or banning null tree models.
public class DefaultTreeCheckingModel implements TreeCheckingModel {

    private final static TreeModel NULL_TREE_MODEL = new TreeModel() {

        public void addTreeModelListener(TreeModelListener l) {
            // nothing to do (cf. the Null Object pattern)
        }

        public Object getChild(Object parent, int index) {
            return null;
        }

        public int getChildCount(Object parent) {
            return 0;
        }

        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }

        public Object getRoot() {
            return null;
        }

        public boolean isLeaf(Object node) {
            return false;
        }

        public void removeTreeModelListener(TreeModelListener l) {
            // nothing to do (cf. the Null Object pattern)
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
            // nothing to do (cf. the Null Object pattern)
        }

    };

    public enum ChildrenChecking {
        ALL_CHECKED, ALL_UNCHECKED, HALF_CHECKED, NO_CHILDREN
    }

    private class PropagateCheckingListener implements TreeModelListener {

        /**
         * Updates the tree greyness in case of nodes changes.
         */
        public void treeNodesChanged(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            updateSubTreeGreyness(path);
            updateAncestorsGreyness(path);
        }

        /**
         * Updates the check of the just inserted nodes.
         */
        public void treeNodesInserted(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.checkingMode.updateCheckAfterChildrenInserted(path);
        }

        /**
         * Nothing to do if nodes were removed.
         */
        public void treeNodesRemoved(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.checkingMode.updateCheckAfterChildrenRemoved(path);
        }

        /**
         * Updates the tree greyness in case of structure changes.
         */
        public void treeStructureChanged(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            DefaultTreeCheckingModel.this.checkingMode.updateCheckAfterStructureChanged(path);
        }
    }

    private HashSet<TreePath> checkedPathsSet;

    protected TreeCheckingMode checkingMode;

    private HashSet<TreePath> disabledPathsSet;

    private HashSet<TreePath> greyedPathsSet;

    /**
     * Event listener list.
     */
    protected EventListenerList listenerList = new EventListenerList();

    protected TreeModel model;

    private PropagateCheckingListener propagateCheckingListener;

    /**
     * Creates a DefaultTreeCheckingModel with PropagateTreeCheckingMode.
     */
    public DefaultTreeCheckingModel(TreeModel model) {
        if (model == null) {
            this.model = NULL_TREE_MODEL;
        } else {
            this.model = model;
        }
        this.checkedPathsSet = new HashSet<TreePath>();
        this.greyedPathsSet = new HashSet<TreePath>();
        this.disabledPathsSet = new HashSet<TreePath>();
        this.propagateCheckingListener = new PropagateCheckingListener();
        this.setCheckingMode(CheckingMode.PROPAGATE);
    }

    /**
     * Adds a path to the checked paths set.
     *
     * @param path the path to be added.
     */
    public void addCheckingPath(TreePath path) {
        this.checkingMode.checkPath(path);
        TreeCheckingEvent event = new TreeCheckingEvent(this, path, true);
        fireValueChanged(event);
    }

    /**
     * Adds the paths to the checked paths set.
     *
     * @param paths the paths to be added.
     */
    public void addCheckingPaths(TreePath[] paths) {
        for (TreePath path : paths) {
            addCheckingPath(path);
        }
    }

    void addToCheckedPathsSet(TreePath path) {
        this.checkedPathsSet.add(path);
    }

    void addToGreyedPathsSet(TreePath path) {
        this.greyedPathsSet.add(path);
    }

    /**
     * Adds x to the list of listeners that are notified each time the set of
     * checking TreePaths changes.
     *
     * @param x the new listener to be added
     */
    public void addTreeCheckingListener(TreeCheckingListener x) {
        this.listenerList.add(TreeCheckingListener.class, x);
    }

    /**
     * Checks the subtree with root path.
     *
     * @param path root of the tree to be checked
     */
    public void checkSubTree(final TreePath path) {
        addToCheckedPathsSet(path);
        removeFromGreyedPathsSet(path);
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            checkSubTree(childPath);
        }
    }

    /**
     * Clears the checking.
     */
    public void clearChecking() {
        this.checkedPathsSet.clear();
        this.greyedPathsSet.clear();
        if (model != null && model.getRoot() != null) {
            fireValueChanged(new TreeCheckingEvent(this, new TreePath(model.getRoot()), false));
        }
    }

    /**
     * Notifies all listeners that are registered for tree selection events on
     * this object.
     *
     * @see #addTreeCheckingListener
     * @see EventListenerList
     */
    protected void fireValueChanged(TreeCheckingEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeCheckingListener.class) {
                ((TreeCheckingListener) listeners[i + 1]).valueChanged(e);
            }
        }
    }

    /**
     * @return The CheckingMode.
     */
    public CheckingMode getCheckingMode() {
        if (this.checkingMode instanceof SimpleTreeCheckingMode) {
            return CheckingMode.SIMPLE;
        }
        if (this.checkingMode instanceof PropagateTreeCheckingMode) {
            return CheckingMode.PROPAGATE;
        }
        if (this.checkingMode instanceof PropagatePreservingCheckTreeCheckingMode) {
            return CheckingMode.PROPAGATE_PRESERVING_CHECK;
        }
        if (this.checkingMode instanceof PropagatePreservingUncheckTreeCheckingMode) {
            return CheckingMode.PROPAGATE_PRESERVING_UNCHECK;
        }
        if (this.checkingMode instanceof PropagateUpWhiteTreeCheckingMode) {
            return CheckingMode.PROPAGATE_UP_UNCHECK;
        }
        return null;
    }

    /**
     * @return Returns the paths that are in the checking.
     */
    public TreePath[] getCheckingPaths() {
        return checkedPathsSet.toArray(new TreePath[checkedPathsSet.size()]);
    }

    /**
     * @return Returns the path of any root of a subtree in the checking.
     */
    public TreePath[] getCheckingRoots() {
        TreePath[] retVal = new TreePath[]{};
        if (model.getRoot() != null) {
            Vector<TreePath> roots = getCheckingRoots(new TreePath(this.model.getRoot()));
            retVal = roots.toArray(retVal);
        }
        return retVal;
    }

    /**
     * @param path
     * @return
     */
    private Vector<TreePath> getCheckingRoots(TreePath path) {
        Object node = path.getLastPathComponent();
        Vector<TreePath> roots = new Vector<TreePath>();
        if (!isPathGreyed(path)) {
            if (isPathChecked(path)) {
                roots.add(path);
            }
            return roots;
        }
        // path is greyed
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            roots.addAll(getCheckingRoots(childPath));
        }
        return roots;
    }

    public ChildrenChecking getChildrenChecking(TreePath path) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        boolean someChecked = false;
        boolean someUnchecked = false;
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (isPathGreyed(childPath)) {
                return ChildrenChecking.HALF_CHECKED;
            }
            // not greyed
            if (isPathChecked(childPath)) {
                if (someUnchecked) {
                    return ChildrenChecking.HALF_CHECKED;
                }
                someChecked = true;
            } else {
                if (someChecked) {
                    return ChildrenChecking.HALF_CHECKED;
                }
                someUnchecked = true;
            }
        }
        if (someChecked) {
            return ChildrenChecking.ALL_CHECKED;
        }
        if (someUnchecked) {
            return ChildrenChecking.ALL_UNCHECKED;
        }
        return ChildrenChecking.NO_CHILDREN;
    }

    /**
     * Return the paths that are children of path, using methods of TreeModel.
     * Nodes don't have to be of type TreeNode.
     *
     * @param path the parent path
     * @return the array of children path
     */
    protected TreePath[] getChildrenPath(TreePath path) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        TreePath[] childrenPath = new TreePath[childrenNumber];
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            childrenPath[childIndex] = path.pathByAddingChild(this.model.getChild(node, childIndex));
        }
        return childrenPath;
    }

    /**
     * @return The paths that are in the greying.
     */
    public TreePath[] getGreyingPaths() {
        return greyedPathsSet.toArray(new TreePath[greyedPathsSet.size()]);
    }

    /*
     * I'm commenting out this out, since the TreeModel should be a write-only
     * field for this class.
     */
    // public TreeModel getTreeModel() {
    // return this.model;
    // }

    /**
     * @param path the root path of the tree to be checked.
     * @return true if exists a child of node with a value different from
     * itself.
     */
    public boolean hasDifferentChildren(TreePath path) {
        return pathHasChildrenWithValue(path, !isPathChecked(path));
    }

    public boolean isPathChecked(TreePath path) {
        return this.checkedPathsSet.contains(path);
    }

    public boolean isPathEnabled(TreePath path) {
        return !this.disabledPathsSet.contains(path);
    }

    public boolean isPathGreyed(TreePath path) {
        return this.greyedPathsSet.contains(path);
    }

    /**
     * @param path the root of the subtree to be checked.
     * @return true if exists a checked node in the subtree of path.
     */
    public boolean pathHasCheckedChildren(TreePath path) {
        return pathHasChildrenWithValue(path, true);
    }

    /**
     * @param path  the root of the subtree to be searched.
     * @param value the value to be found.
     * @return true if exists a node with checked status value in the subtree of
     * path.
     */
    protected boolean pathHasChildrenWithValue(TreePath path, boolean value) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (isPathChecked(childPath) == value) {
                return true;
            }
        }
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (pathHasChildrenWithValue(childPath, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Note: The checking and the greyness of children MUST be consistent to
     * work properly.
     *
     * @param path the root of the subtree to be checked.
     * @return true if exists an unchecked node in the subtree of path.
     */
    public boolean pathHasUncheckedChildren(TreePath path) {
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            if (isPathGreyed(childPath) | !isPathChecked(childPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a path from the checked paths set
     *
     * @param path the path to be removed
     */
    public void removeCheckingPath(TreePath path) {
        this.checkingMode.uncheckPath(path);
        TreeCheckingEvent event = new TreeCheckingEvent(this, path, false);
        fireValueChanged(event);
    }

    /**
     * Removes the paths from the checked paths set
     *
     * @param paths the paths to be removed
     */
    public void removeCheckingPaths(TreePath[] paths) {
        for (TreePath path : paths) {
            removeCheckingPath(path);
        }
    }

    void removeFromCheckedPathsSet(TreePath path) {
        this.checkedPathsSet.remove(path);
    }

    void removeFromGreyedPathsSet(TreePath path) {
        this.greyedPathsSet.remove(path);
    }

    /**
     * Removes x from the list of listeners that are notified each time the set
     * of checking TreePaths changes.
     *
     * @param x the listener to remove
     */
    public void removeTreeCheckingListener(TreeCheckingListener x) {
        this.listenerList.remove(TreeCheckingListener.class, x);
    }

    /**
     * Sets the specified checking mode. The consistence of the existing
     * checking is not enforced nor controlled.
     */
    public void setCheckingMode(CheckingMode mode) {
        /*
         * CheckingMode implements togglePath method (cf. the Strategy Pattern).
         */
        switch (mode) {
            case SIMPLE:
                this.checkingMode = new SimpleTreeCheckingMode(this);
                break;
            case SINGLE:
                this.checkingMode = new SingleTreeCheckingMode(this);
                break;
            case PROPAGATE:
                this.checkingMode = new PropagateTreeCheckingMode(this);
                break;
            case PROPAGATE_PRESERVING_CHECK:
                this.checkingMode = new PropagatePreservingCheckTreeCheckingMode(this);
                break;
            case PROPAGATE_PRESERVING_UNCHECK:
                this.checkingMode = new PropagatePreservingUncheckTreeCheckingMode(this);
                break;
            case PROPAGATE_UP_UNCHECK:
                this.checkingMode = new PropagateUpWhiteTreeCheckingMode(this);
                break;
            default:
                break;
        }
        // // TODO: safe to delete???
        // updateTreeGreyness();
    }

    /**
     * Sets the specified checking mode. The consistence of the existing
     * checking is not enforced nor controlled.
     */
    public void setCheckingMode(TreeCheckingMode mode) {
        this.checkingMode = mode;
    }

    /**
     * Sets the checking to path.
     */
    public void setCheckingPath(TreePath path) {
        clearChecking();
        addCheckingPath(path);
    }

    /**
     * Sets the checking to the specified paths.
     */
    public void setCheckingPaths(TreePath[] paths) {
        clearChecking();
        for (TreePath path : paths) {
            addCheckingPath(path);
        }
    }

    /**
     * Sets whether or not the path is enabled.
     *
     * @param path the path to enable/disable
     */
    public void setPathEnabled(TreePath path, boolean enable) {
        if (enable) {
            this.disabledPathsSet.remove(path);
        } else {
            this.disabledPathsSet.add(path);
        }
    }

    /**
     * Sets whether or not the paths are enabled.
     *
     * @param paths the paths to enable/disable
     */
    public void setPathsEnabled(TreePath[] paths, boolean enable) {
        for (TreePath path : paths) {
            setPathEnabled(path, enable);
        }
    }

    /**
     * Sets the specified tree model. The current checking set is cleared.
     */
    public void setTreeModel(TreeModel newModel) {
        TreeModel oldModel = this.model;
        if (oldModel != null) {
            oldModel.removeTreeModelListener(this.propagateCheckingListener);
        }
        this.model = newModel;
        if (newModel != null) {
            newModel.addTreeModelListener(this.propagateCheckingListener);
        }
        clearChecking();
    }

    /**
     * Delegates to the current checkingMode the toggling style, using the
     * Strategy Pattern.
     */
    public void toggleCheckingPath(TreePath path) {
        if (!isPathEnabled(path)) {
            return;
        }
        if (isPathChecked(path)) {
            removeCheckingPath(path);
        } else {
            addCheckingPath(path);
        }
    }

    /**
     * Return a string that describes the tree model including the values of
     * checking, enabling, greying.
     */
    @Override
    public String toString() {
        if (model.getRoot() != null) {
            return toString(new TreePath(this.model.getRoot()));
        } else
            return null;
    }

    /**
     * Convenience method for getting a string that describes the tree starting
     * at the specified path.
     *
     * @param path the root of the subtree to describe.
     */
    private String toString(TreePath path) {
        String checkString = "n";
        String greyString = "n";
        String enableString = "n";
        if (isPathChecked(path)) {
            checkString = "y";
        }
        if (isPathEnabled(path)) {
            enableString = "y";
        }
        if (isPathGreyed(path)) {
            greyString = "y";
        }
        String description = "Path checked: " + checkString + " greyed: " + greyString + " enabled: " + enableString + " Name: "
                + path.toString() + "\n";
        for (TreePath childPath : getChildrenPath(path)) {
            description += toString(childPath);
        }
        return description;
    }

    /**
     * Unchecks the subtree rooted at path.
     *
     * @param path root of the tree to be unchecked
     */
    public void uncheckSubTree(TreePath path) {
        removeFromCheckedPathsSet(path);
        removeFromGreyedPathsSet(path);
        Object node = path.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(node);
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            TreePath childPath = path.pathByAddingChild(this.model.getChild(node, childIndex));
            uncheckSubTree(childPath);
        }
    }

    /**
     * Ungreys the subtree rooted at path.
     *
     * @param path the root of the tree to be checked
     */
    public void ungreySubTree(TreePath path) {
        removeFromGreyedPathsSet(path);
        for (TreePath childPath : getChildrenPath(path)) {
            ungreySubTree(childPath);
        }
    }

    /**
     * Updates the grayness value of the parents of path. Note: the greyness and
     * checking of the other nodes (not ancestors) MUST BE consistent.
     *
     * @param path the treepath containing the ancestors to be grey-updated
     */
    public void updateAncestorsGreyness(TreePath path) {
        TreePath[] parents = new TreePath[path.getPathCount()];
        parents[0] = path;
        boolean greyAll = isPathGreyed(path);
        for (int i = 1; i < parents.length; i++) {
            parents[i] = parents[i - 1].getParentPath();
            if (greyAll) {
                addToGreyedPathsSet(parents[i]);
            } else {
                updatePathGreyness(parents[i]);
                greyAll = isPathGreyed(parents[i]);
            }
        }
    }

    /**
     * Updates consistency of the checking, by invoking
     * updateSubTreeCheckingConsistency on the root node.
     */
    public void updateCheckingConsistency() {
        if (model.getRoot() != null) {
            updateSubTreeCheckingConsistency(new TreePath(model.getRoot()));
        }
    }

    /**
     * Updates the greyness value value for the given path if there are children
     * with different values. Note: the greyness and cheking of children MUST BE
     * consistent.
     *
     * @param ancestor the path to be grey-updated.
     */
    protected void updatePathGreyness(TreePath ancestor) {
        boolean value = isPathChecked(ancestor);
        Object ancestorNode = ancestor.getLastPathComponent();
        int childrenNumber = this.model.getChildCount(ancestorNode);
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            Object childNode = this.model.getChild(ancestorNode, childIndex);
            TreePath childPath = ancestor.pathByAddingChild(childNode);
            if (isPathGreyed(childPath)) {
                addToGreyedPathsSet(ancestor);
                return;
            }
            if (isPathChecked(childPath) != value) {
                addToGreyedPathsSet(ancestor);
                return;
            }
        }
        removeFromGreyedPathsSet(ancestor);
    }

    /**
     * Updates consistency of the checking of sub-tree starting at path. It's
     * based on paths greyness.
     *
     * @param path the root of the sub-tree to be grey-updated
     */
    public void updateSubTreeCheckingConsistency(TreePath path) {
        if (isPathGreyed(path)) {
            // greyed
            for (TreePath childPath : getChildrenPath(path)) {
                updateSubTreeCheckingConsistency(childPath);
            }
            updatePathGreyness(path);
        } else {
            // not greyed
            if (isPathChecked(path)) {
                checkSubTree(path);
            } else {
                uncheckSubTree(path);
            }
            return;
        }
    }

    /**
     * Updates the greyness of sub-tree starting at path.
     *
     * @param path the root of the sub-tree to be grey-updated
     */
    public void updateSubTreeGreyness(TreePath path) {
        if (pathHasChildrenWithValue(path, !isPathChecked(path))) {
            addToGreyedPathsSet(path);
        } else {
            removeFromGreyedPathsSet(path);
        }
        if (isPathGreyed(path)) {
            for (TreePath childPath : getChildrenPath(path)) {
                updateSubTreeGreyness(childPath);
            }
            return;
        } else {
            ungreySubTree(path);
        }
    }

    /**
     * Updates the greyness state of the entire tree.
     */
    public void updateTreeGreyness() {
        if (model.getRoot() != null) {
            updateSubTreeGreyness(new TreePath(this.model.getRoot()));
        }
    }

}