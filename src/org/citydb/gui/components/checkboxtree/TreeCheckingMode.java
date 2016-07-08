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
 * The model for checking/unchecking the nodes of a CheckboxTree. Alterations
 * of a node state may propagate on descendants/ascendants, according to the
 * behaviour of the model. Several default behavioural modes are defined. The
 * models must use the methods addToCheckedSet and removeFromCheckedSet from
 * DefaultTreeCheckingModel to add/remove the single paths from the checking
 * set.
 * 
 * @author bigagli
 * @author boldrini
 */
public abstract class TreeCheckingMode {

    protected DefaultTreeCheckingModel model;

    // TODO: implementare Strategy in questo modo: TreeCheckingMode classe
    // interna al TreeCheckingModel, con un metodo getModel() protetto,
    // utile
    // alle sottoclassi
    TreeCheckingMode(DefaultTreeCheckingModel model) {
	this.model = model;
    }

    /**
         * Checks the specified path and propagates the checking according to
         * the strategy
         * 
         * @param path the path to be added.
         */

    public abstract void checkPath(TreePath path);

    /**
         * Unchecks the specified path and propagates the checking according to
         * the strategy
         * 
         * @param path the path to be removed.
         */
    public abstract void uncheckPath(TreePath path);

    /**
         * Update the check of the given path after the insertion of some of its
         * children, according to the strategy
         * 
         * @param path
         */
    public abstract void updateCheckAfterChildrenInserted(TreePath path);

    /**
         * Update the check of the given path after the removal of some of its
         * children, according to the strategy
         * 
         * @param path
         */
    public abstract void updateCheckAfterChildrenRemoved(TreePath path);

    /**
         * Update the check of the given path after the structure change,
         * according to the strategy
         * 
         * @param path
         */
    public abstract void updateCheckAfterStructureChanged(TreePath path);

}
