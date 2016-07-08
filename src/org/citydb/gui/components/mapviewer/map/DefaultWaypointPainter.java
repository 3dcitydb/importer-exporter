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
package org.citydb.gui.components.mapviewer.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.Painter;

public class DefaultWaypointPainter implements Painter<JXMapViewer> {
	private WaypointPainter<JXMapViewer> painter;
	private List<Waypoint> waypoints;	

	public DefaultWaypointPainter() {
		painter = new WaypointPainter<JXMapViewer>();
		painter.setRenderer(new SingleWaypointRenderer());
		waypoints = new ArrayList<Waypoint>();
	}

	public void showWaypoints(DefaultWaypoint... waypoints) {
		this.waypoints.clear();
		for (DefaultWaypoint waypoint : waypoints)
			this.waypoints.add(waypoint);

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

	private final class SingleWaypointRenderer implements WaypointRenderer {
		private BufferedImage precise;
		private BufferedImage approximate;
		private BufferedImage reverse;

		public SingleWaypointRenderer() {
			try {
				precise = ImageIO.read(getClass().getResource("/resources/img/map/waypoint_precise.png"));
				approximate = ImageIO.read(getClass().getResource("/resources/img/map/waypoint_approximate.png"));
				reverse = ImageIO.read(getClass().getResource("/resources/img/map/waypoint_reverse.png"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
			if (waypoint instanceof DefaultWaypoint) {
				DefaultWaypoint tmp = (DefaultWaypoint)waypoint;
				BufferedImage img = null;
				int x = -9;

				switch (tmp.getType()) {
				case PRECISE:
					img = precise;
					x = -9;
					break;
				case APPROXIMATE:
					img = approximate;
					x = -9;
					break;
				case REVERSE:
					img = reverse;
					x = -11;
					break;
				}

				int y = -img.getHeight();		
				g.drawImage(img, x, y, null);
			}

			return false;
		}

	}

}
