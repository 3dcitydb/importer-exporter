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
package org.citydb.gui.map.map;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.util.log.Logger;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.Painter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultWaypointPainter implements Painter<JXMapViewer> {
    private final WaypointPainter<JXMapViewer> painter;
    private final List<Waypoint> waypoints;

    public DefaultWaypointPainter() {
        painter = new WaypointPainter<>();
        painter.setRenderer(new SingleWaypointRenderer());
        waypoints = new ArrayList<>();
    }

    public void showWaypoints(DefaultWaypoint... waypoints) {
        this.waypoints.clear();
        Collections.addAll(this.waypoints, waypoints);
        painter.setWaypoints(this.waypoints);
    }

    public void clearWaypoints() {
        waypoints.clear();
        painter.setWaypoints(waypoints);
    }

    @Override
    public void paint(Graphics2D arg0, JXMapViewer arg1, int arg2, int arg3) {
        painter.paint(arg0, arg1, arg2, arg3);
    }

    private static final class SingleWaypointRenderer implements WaypointRenderer {
        private ImageIcon precise;
        private ImageIcon approximate;
        private ImageIcon reverse;

        public SingleWaypointRenderer() {
            try {
                precise = approximate = new FlatSVGIcon("org/citydb/gui/map/waypoint.svg");
                reverse = new FlatSVGIcon("org/citydb/gui/map/waypoint_reverse.svg");
            } catch (Exception ex) {
                Logger.getInstance().logStackTrace(ex);
            }
        }

        public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
            if (waypoint instanceof DefaultWaypoint) {
                DefaultWaypoint tmp = (DefaultWaypoint) waypoint;
                ImageIcon img = null;
                switch (tmp.getType()) {
                    case PRECISE:
                        img = precise;
                        break;
                    case APPROXIMATE:
                        img = approximate;
                        break;
                    case REVERSE:
                        img = reverse;
                        break;
                }

                int x = -img.getIconWidth() / 2;
                int y = -img.getIconHeight() + 3;
                g.drawImage(img.getImage(), x, y, null);
            }

            return false;
        }
    }
}
