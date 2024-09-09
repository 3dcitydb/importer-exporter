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

package org.citydb.gui.components;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;

public class ScrollablePanel extends JPanel implements Scrollable {
    private final boolean tracksViewportWidth;
    private final boolean tracksViewportHeight;

    public ScrollablePanel(boolean tracksViewportWidth, boolean tracksViewportHeight) {
        this.tracksViewportWidth = tracksViewportWidth;
        this.tracksViewportHeight = tracksViewportHeight;
    }

    public ScrollablePanel() {
        this(false, false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        int increment;
        switch (orientation) {
            case SwingConstants.VERTICAL:
                increment = visibleRect.height / 10;
                break;
            case SwingConstants.HORIZONTAL:
                increment = visibleRect.width / 10;
                break;
            default:
                increment = 10;
        }

        return UIScale.scale(increment);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return tracksViewportWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return tracksViewportHeight;
    }
}
