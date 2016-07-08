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
