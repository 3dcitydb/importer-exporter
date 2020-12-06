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

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * A tree whose nodes may be checked (e.g. the widget usually found in software
 * installers, that allows to select which features to install/uninstall). If a
 * node has some child of different checking status is greyed. You can use the
 * same constructors of JTree to instantiate a new CheckboxTree Example from a
 * TreeNode:
 *
 * <pre>
 * DefaultMutableTreeNode root = new DefaultMutableTreeNode(&quot;root&quot;);
 * root.add(new DefaultMutableTreeNode(&quot;child A&quot;));
 * root.add(new DefaultMutableTreeNode(&quot;child B&quot;));
 * CheckboxTree CheckboxTree = new CheckboxTree(root);
 * </pre>
 * <p>
 * Example from a TreeModel:
 *
 * <pre>
 * DefaultTreeModel dtm = new DefaultTreeModel(root);
 *
 * CheckboxTree CheckboxTree = new CheckboxTree(root);
 * </pre>
 * <p>
 * Default constructor (useful for gui builders):
 *
 * <pre>
 * CheckboxTree CheckboxTree = new CheckboxTree();
 * </pre>
 * <p>
 * Then you can set the checking propagation style:
 *
 * <pre>
 * CheckboxTree.getCheckingModel().setCheckingMode(
 * 		TreeCheckingModel.CheckingMode.SIMPLE);
 * CheckboxTree.getCheckingModel().setCheckingMode(
 * 		TreeCheckingModel.CheckingMode.PROPAGATE);
 * CheckboxTree.getCheckingModel().setCheckingMode(
 * 		TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_CHECK);
 * CheckboxTree.getCheckingModel().setCheckingMode(
 * 		TreeCheckingModel.CheckingMode.PROPAGATE_PRESERVING_UNCHECK);
 * </pre>
 * <p>
 * You can also set the model at a later time using:
 *
 * <pre>
 * CheckboxTree.setModel(aTreeModel);
 * </pre>
 * <p>
 * There are two methods that return the paths that are in the checking set:
 *
 * <pre>
 * TreePath[] tp = CheckboxTree.getCheckingPaths();
 *
 * TreePath[] tp = CheckboxTree.getCheckingRoots();
 * </pre>
 * <p>
 * You can also add/remove a listener of a TreeCheckingEvent in this way:
 *
 * <pre>
 * CheckboxTree.addTreeCheckingListener(new TreeCheckingListener() {
 * 	public void valueChanged(TreeCheckingEvent e) {
 * 		System.out.println(&quot;Checked paths changed: user clicked on &quot;
 * 				+ (e.getLeadingPath().getLastPathComponent()));
 *    }
 * });
 * </pre>
 *
 * @author Enrico Boldrini
 * @author Lorenzo Bigagli
 */

public class CheckboxTree extends JTree {

    /**
     * The mouse listener taking care of node checking/unchecking.
     */
    /*
     * this inner class (commented out) is actually the Swing way, but since
     * JTree is still based on the AWT mechanism for event handling (what causes
     * the last added listener to be invoked last) we could not override the
     * JTree event handler, so we would _always_ have that checking a node
     * causes it to be selected. We had to work around it (see initialize() and
     * processMouseEvent()).
     */
    // public class NodeCheckListener extends MouseAdapter {
    //
    // @Override
    // public void mousePressed(MouseEvent e) {
    // if (e.isConsumed() || !CheckboxTree.this.isEnabled()) {
    // return;
    // }
    // // we use mousePressed instead of mouseClicked for performance
    // int x = e.getX();
    // int y = e.getY();
    // int row = getRowForLocation(x, y);
    // if (row == -1) {
    // // click outside any node
    // return;
    // }
    // Rectangle rect = getRowBounds(row);
    // if (rect == null) {
    // // click on an invalid node
    // return;
    // }
    // if ((getCellRenderer()).isOnHotspot(x - rect.x, y - rect.y)) {
    // getCheckingModel().toggleCheckingPath(getPathForRow(row));
    // e.consume();
    // }
    // }
    // };

    /*
     * temporary solution for enabling spacebar checking. Should make use of
     * InputMaps?
     */
    private class SpaceListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (!isEnabled()) {
                return;
            }
            TreePath path = CheckboxTree.this.getSelectionPath();

            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (path != null) {
                    TreeCheckingModel cm = CheckboxTree.this.getCheckingModel();
                    cm.toggleCheckingPath(path);
                }
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    /*
     * The checking model of this CheckboxTree. On the contrary of JTree and its
     * TreeModel, we enforce that this field never be null (cf. the Null Object
     * pattern). Due to subtleties in the sequence of initialization calls
     * (JTree.setModel gets called in the constructor, causing nodes rendering
     * before field initialization and hence a null pointer exception in the
     * default cell renderer) we make it private and initialize it in the
     * getter.
     */
    private TreeCheckingModel checkingModel;

    /**
     * Whether checking a node causes it to be selected, too.
     */
    private boolean selectsByChecking;

    /**
     * For GUI builders. It returns a CheckboxTree with a default tree model to
     * show something interesting. Creates a CheckboxTree with visible handles,
     * a default CheckboxTreeCellRenderer and a default TreeCheckingModel.
     */
    public CheckboxTree() {
        super(getDefaultTreeModel());
        initialize();
    }

    /**
     * Create a CheckboxTree with visible handles, a default
     * CheckboxTreeCellRenderer and a default TreeCheckingModel. The tree is
     * based on the specified tree model.
     */
    public CheckboxTree(TreeModel treemodel) {
        super(treemodel);
        initialize();
    }

    /**
     * Create a CheckboxTree with visible handles, a default
     * CheckboxTreeCellRenderer and a default TreeCheckingModel. The tree root
     * is the specified tree node.
     *
     * @param root the root of the tree
     */
    public CheckboxTree(TreeNode root) {
        super(root);
        initialize();
    }

    /**
     * Add a path in the checking.
     */
    public void addCheckingPath(TreePath path) {
        getCheckingModel().addCheckingPath(path);
    }

    /**
     * Add paths in the checking.
     */
    public void addCheckingPaths(TreePath[] paths) {
        getCheckingModel().addCheckingPaths(paths);
    }

    /**
     * Add a listener for <code>TreeChecking</code> events.
     *
     * @param tsl the <code>TreeCheckingListener</code> that will be notified
     *            when a node is checked
     */
    public void addTreeCheckingListener(TreeCheckingListener tsl) {
        this.checkingModel.addTreeCheckingListener(tsl);
    }

    /**
     * Clear the checking set.
     */
    public void clearChecking() {
        getCheckingModel().clearChecking();
    }

    /**
     * Expand the tree completely.
     */
    public void expandAll() {
        expandSubTree(new TreePath(getModel().getRoot()));
    }

    private void expandSubTree(TreePath path) {
        expandPath(path);
        Object node = path.getLastPathComponent();
        int childrenNumber = getModel().getChildCount(node);
        TreePath[] childrenPath = new TreePath[childrenNumber];
        for (int childIndex = 0; childIndex < childrenNumber; childIndex++) {
            childrenPath[childIndex] = path.pathByAddingChild(getModel().getChild(node, childIndex));
            expandSubTree(childrenPath[childIndex]);
        }
    }

    /**
     * Return the TreeCheckingModel of this CheckboxTree. This method never
     * returns null (although it may return the NullTreeCheckingModel
     * singleton).
     *
     * @return the TreeCheckingModel of this CheckboxTree.
     */
    public TreeCheckingModel getCheckingModel() {
        if (checkingModel == null) {
            checkingModel = NullTreeCheckingModel.getInstance();
        }
        return this.checkingModel;
    }

    /**
     * Return paths that are in the checking.
     */
    public TreePath[] getCheckingPaths() {
        return getCheckingModel().getCheckingPaths();
    }

    /**
     * @return the paths that are in the checking set and are the (upper) roots
     * of checked trees.
     */
    public TreePath[] getCheckingRoots() {
        return getCheckingModel().getCheckingRoots();
    }

    /**
     * @return the paths that are in the greying.
     */
    public TreePath[] getGreyingPaths() {
        return getCheckingModel().getGreyingPaths();
    }

    /**
     * Convenience initialization method.
     */
    private void initialize() {
        setCheckingModel(new DefaultTreeCheckingModel(this.treeModel));
        setCellRenderer(new DefaultCheckboxTreeCellRenderer());
        /*
         * the next line (commented out) is actually the Swing way, but since
         * JTree is still based on the AWT mechanism for event handling (what
         * causes the last added listener to be invoked last) we could not
         * override the JTree event handler, so we would _always_ have that
         * checking a node causes it to be selected. We had to work around it
         * (see processMouseEvent()).
         */
        // addMouseListener(new NodeCheckListener());
        setSelectsByChecking(true);
        addKeyListener(new SpaceListener());
        this.selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setShowsRootHandles(true);
    }

    /**
     * Return true if the item identified by the path is currently checked.
     *
     * @param path a <code>TreePath</code> identifying a node
     * @return true if the node is checked
     */
    public boolean isPathChecked(TreePath path) {
        return getCheckingModel().isPathChecked(path);
    }

    /**
     * Return whether checking a node causes it to be selected, too.
     *
     * @return the intended behavior of checking with respect to selection.
     */
    public boolean isSelectsByChecking() {
        return selectsByChecking;
    }

    /*
     * This is overridden to work around an AWT limitation (see the comment
     * inside initialize()). Basically, if a mouse_pressed event insists on a
     * checkbox control _and_ we don't want the node to be selected, we stop
     * processing the event. Simply consuming the event wouldn't work, because
     * the BasicTreeUI would select the node on the mouse_released event!
     *
     * @see javax.swing.JComponent#processMouseEvent(java.awt.event.MouseEvent)
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            // we use mousePressed instead of mouseClicked for performance
            if (!e.isConsumed() && this.isEnabled()) {
                int x = e.getX();
                int y = e.getY();
                int row = getRowForLocation(x, y);
                if (row != -1) {
                    // click inside some node
                    Rectangle rect = getRowBounds(row);
                    if (rect != null) {
                        // click on a valid node
                        if ((getCellRenderer()).isOnHotspot(x - rect.x, y - rect.y)) {
                            getCheckingModel().toggleCheckingPath(getPathForRow(row));
                            if (!isSelectsByChecking())
                                return;
                        }
                    }
                }
            }
        }
        super.processMouseEvent(e);
    }

    /**
     * Remove a path from the checking.
     */
    public void removeCheckingPath(TreePath path) {
        getCheckingModel().removeCheckingPath(path);
    }

    /**
     * Remove paths from the checking.
     */
    public void removeCheckingPaths(TreePath[] paths) {
        getCheckingModel().removeCheckingPaths(paths);
    }

    /**
     * Remove a <code>TreeChecking</code> listener.
     *
     * @param tcl the <code>TreeCheckingListener</code> to remove
     */
    public void removeTreeCheckingListener(TreeCheckingListener tcl) {
        this.checkingModel.removeTreeCheckingListener(tcl);
    }

    /**
     * Set the <code>CheckboxTreeCellRenderer</code> that will be used to draw
     * each cell.
     *
     * @param tcl the <code>TreeCellRenderer</code> that is to render each cell
     * @throws IllegalArgumentException if the argument is not a
     *                                  <code>CheckboxTreeCellRenderer</code>.
     */
    @Override
    public void setCellRenderer(TreeCellRenderer tcl) {
        if (!(tcl instanceof CheckboxTreeCellRenderer)) {
            throw new IllegalArgumentException("The argument does not implement the CheckboxTreeCellRenderer interface: " + tcl);
        }
        super.setCellRenderer(tcl);
    }

    /**
     * Co-variant method for retrieving the
     * <code>CheckboxTreeCellRenderer</code> of this tree.
     */
    @Override
    public CheckboxTreeCellRenderer getCellRenderer() {
        if (cellRenderer == null) {
            cellRenderer = new DefaultCheckboxTreeCellRenderer();
        }
        return (CheckboxTreeCellRenderer) cellRenderer;
    }

    /**
     * Set the checking model of this CheckboxTree. If the parameter is null,
     * the checking model is set to the NullTreeCheckingModel singleton.
     *
     * @param newCheckingModel the new TreeCheckingModel of this CheckboxTree.
     */
    public void setCheckingModel(TreeCheckingModel newCheckingModel) {
        /*
         * in case we are dealing with DefaultTreeCheckingModel, we link/unlink
         * it from the model of this tree
         */
        TreeCheckingModel oldCheckingModel = this.checkingModel;
        if (oldCheckingModel instanceof DefaultTreeCheckingModel) {
            // null the model to avoid dangling pointers
            ((DefaultTreeCheckingModel) oldCheckingModel).setTreeModel(null);
        }
        if (newCheckingModel != null) {
            this.checkingModel = newCheckingModel;
            if (newCheckingModel instanceof DefaultTreeCheckingModel) {
                ((DefaultTreeCheckingModel) newCheckingModel).setTreeModel(getModel());
            }
            // add a treeCheckingListener to repaint upon checking modifications
            newCheckingModel.addTreeCheckingListener(new TreeCheckingListener() {
                public void valueChanged(TreeCheckingEvent e) {
                    repaint();
                }
            });
        } else {
            this.checkingModel = NullTreeCheckingModel.getInstance();
        }
    }

    /**
     * Set path in the checking.
     */
    public void setCheckingPath(TreePath path) {
        getCheckingModel().setCheckingPath(path);
    }

    /**
     * Set paths that are in the checking.
     */
    public void setCheckingPaths(TreePath[] paths) {
        getCheckingModel().setCheckingPaths(paths);
    }

    /**
     * Set the TreeModel and links it to the existing checkingModel.
     */
    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        if (checkingModel instanceof DefaultTreeCheckingModel) {
            ((DefaultTreeCheckingModel) checkingModel).setTreeModel(newModel);
        }
    }

    /**
     * Specify whether checking a node causes it to be selected, too, or else
     * the selection is not affected. The default behavior is the former.
     *
     * @param selectsByChecking the intended behavior of checking with respect to selection.
     */
    public void setSelectsByChecking(boolean selectsByChecking) {
        this.selectsByChecking = selectsByChecking;
    }

    /**
     * @return a string representation of the tree, including the checking,
     * enabling and greying sets.
     */
    @Override
    public String toString() {
        String retVal = super.toString();
        TreeCheckingModel tcm = getCheckingModel();
        if (tcm != null) {
            return retVal + "\n" + tcm.toString();
        }
        return retVal;
    }

}