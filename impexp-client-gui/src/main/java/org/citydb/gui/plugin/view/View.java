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
package org.citydb.gui.plugin.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class View {
    private List<ViewListener> viewListeners;

    public abstract String getLocalizedTitle();

    public abstract Component getViewComponent();

    public abstract String getToolTip();

    public abstract Icon getIcon();

    public final void addViewListener(ViewListener listener) {
        if (viewListeners == null)
            viewListeners = new ArrayList<>();

        viewListeners.add(listener);
    }

    public final boolean removeViewListener(ViewListener listener) {
        return viewListeners != null && viewListeners.remove(listener);
    }

    public final void fireViewEvent(final ViewEvent e) {
        if (viewListeners != null) {
            for (final ViewListener listener : viewListeners) {
                SwingUtilities.invokeLater(() -> {
                    switch (e.getViewState()) {
                        case VIEW_ACTIVATED:
                            listener.viewActivated(e);
                            break;
                        case VIEW_DEACTIVATED:
                            listener.viewDeactivated(e);
                            break;
                    }
                });
            }
        }
    }

}
