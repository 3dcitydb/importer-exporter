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

public class TreeCheckingSynchronizer implements TreeCheckingListener {

    protected TreeCheckingModel model1;

    protected TreeCheckingModel model2;

    public TreeCheckingSynchronizer(CheckboxTree tree1, CheckboxTree tree2) {

        this.model1 = tree1.getCheckingModel();
        this.model2 = tree2.getCheckingModel();

        tree1.addTreeCheckingListener(this);
        tree2.addTreeCheckingListener(this);
    }

    public void valueChanged(TreeCheckingEvent e) {

        Object source = e.getSource();
        TreePath leadingPath = e.getPath();

        boolean checked = e.isCheckedPath();

        TreeCheckingModel dest = source.equals(model1) ? model2 : model1;

        if (checked) {
            if (!dest.isPathChecked(leadingPath)) {
                dest.addCheckingPath(leadingPath);
            }
        } else {
            if (dest.isPathChecked(leadingPath)) {
                dest.removeCheckingPath(leadingPath);
            }
        }
    }
}
