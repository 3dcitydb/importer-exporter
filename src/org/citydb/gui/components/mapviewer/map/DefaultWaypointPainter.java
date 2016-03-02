/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
