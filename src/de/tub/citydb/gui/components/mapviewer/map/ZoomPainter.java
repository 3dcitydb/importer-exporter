/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

public class ZoomPainter extends MouseAdapter implements Painter<JXMapViewer> {
	private final JXMapViewer map;

	private Color borderColor = new Color(200, 0, 0, 150);
	private Color regionColor = new Color(255, 255, 255, 125);

	private Rectangle start, end, selectedArea;

	public ZoomPainter(JXMapViewer map) {
		this.map = map;
		map.addMouseListener(this);
		map.addMouseMotionListener(this);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && e.isShiftDown()) {
			start = new Rectangle(e.getPoint());
			map.setPanEnabled(false);
		} else
			start = null;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (start != null) {
			end = new Rectangle(e.getPoint());
			selectedArea = start.union(end);
			map.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (selectedArea != null) {	
			// do not zoom over visible bounds
			selectedArea = map.getBounds().intersection(selectedArea);
			
			HashSet<GeoPosition> positions = new HashSet<GeoPosition>();
			positions.add(map.convertPointToGeoPosition(new Point2D.Double(selectedArea.getMinX(), selectedArea.getMinY())));
			positions.add(map.convertPointToGeoPosition(new Point2D.Double(selectedArea.getMaxX(), selectedArea.getMaxY())));	

			map.setZoom(1);
			map.calculateZoomFrom(positions);

			start = selectedArea = null;
			map.setPanEnabled(true);
			map.repaint();
		}
	}

	@Override
	public void paint(Graphics2D gd, JXMapViewer t, int i, int i1) {
		if (selectedArea != null) {
			gd.setColor(regionColor);
			gd.fillRect(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);

			gd.setColor(borderColor);
			gd.drawRect(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);
		}
	}
}
