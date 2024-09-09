/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
import java.util.EventObject;

/**
 * An event that characterizes a change in the current checking. The change is
 * related to a single checked/unchecked path. TreeCheckingListeners will
 * generally query the source of the event for the new checked status of each
 * potentially changed row.
 *
 * @author Lorenzo Bigagli
 * @see TreeCheckingListener
 * @see TreeCheckingModel
 */
public class TreeCheckingEvent extends EventObject {

    /**
     * The path related to this event
     */
    protected TreePath changedPath;

    private boolean checked;

    /**
     * Represents a change in the checking of a TreeCheckingModel. The specified
     * path identifies the path that have been either checked or unchecked.
     *
     * @param source  source of event
     * @param path    the path that has changed in the checking
     * @param checked whether or not the path is checked, false means that path was
     *                removed from the checking.
     */
    public TreeCheckingEvent(Object source, TreePath path, boolean checked) {
        super(source);
        this.changedPath = path;
        this.checked = checked;
    }

    /**
     * @return the path that was added or removed from the checking.
     */
    public TreePath getPath() {
        return this.changedPath;
    }

    /**
     * @return true if the path related to the event is checked. A return value
     * of false means that the path has been removed from the checking.
     */
    public boolean isCheckedPath() {
        return checked;
    }

}
