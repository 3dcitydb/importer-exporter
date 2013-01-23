/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.components.mapviewer.map;

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
