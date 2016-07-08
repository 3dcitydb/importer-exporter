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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.citydb.api.event.EventDispatcher;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.gui.components.mapviewer.map.event.BoundingBoxSelectionEvent;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

public class BBoxSelectionPainter extends MouseAdapter implements Painter<JXMapViewer> {
	private final JXMapViewer map;
	private final EventDispatcher eventDispatcher;

	private Color borderColor = new Color(0, 0, 200);
	private Color regionColor = new Color(0, 0, 200, 75);

	private Rectangle2D start, end, selectedArea;
	private boolean mouseDragged;

	private boolean outOfBounds = false;
	private Point outOfBoundsPoint = null;
	private AtomicBoolean isThreadRunning = new AtomicBoolean(false);
	private int shrinkViewPort = 30;
	private double scale = .0001;		

	public BBoxSelectionPainter(JXMapViewer map) {
		this.map = map;
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();

		map.addMouseListener(this);
		map.addMouseMotionListener(this);
	}

	public GeoPosition[] getBoundingBox() {
		GeoPosition[] bounds = null;
		if (selectedArea != null) {
			bounds = new GeoPosition[2];
			bounds[0] = new GeoPosition(selectedArea.getMinY(), selectedArea.getMinX());
			bounds[1] = new GeoPosition(selectedArea.getMaxY(), selectedArea.getMaxX());
		}

		return bounds;
	}

	public boolean setBoundingBox(GeoPosition southWest, GeoPosition northEast) {
		if (isVisibleOnScreen(southWest, northEast)) {
			selectedArea = createGeoRectangle(southWest, northEast);
			map.repaint();
			return true;
		}
		
		return false;
	}

	public void clearBoundingBox() {
		start = selectedArea = null;
		map.repaint();
	}

	public boolean isVisibleOnScreen(GeoPosition southWest, GeoPosition northEast) {
		return isVisibleOnScreen(createGeoRectangle(southWest, northEast));
	}

	private boolean isVisibleOnScreen(Rectangle2D bbox) {
		return bbox != null ? !createDrawArea(bbox, map.getTileFactory().getInfo().getMinimumZoomLevel()).isEmpty() : false;		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (start != null)
			checkOutOfBounds(e.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && e.isAltDown()) {
			start = createGeoRectangle(e.getPoint());
			map.setPanEnabled(false);
		} else
			start = null;

		mouseDragged = false;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (start != null) {
			mouseDragged = true;
			end = createGeoRectangle(e.getPoint());
			selectedArea = start.createUnion(end);
			checkOutOfBounds(e.getPoint());

			if (outOfBounds && isThreadRunning.compareAndSet(false, true)) {
				Thread t = new Thread() {
					public void run() {
						while (outOfBounds && selectedArea != null && start != null) {
							Point2D center = map.getCenter();
							Rectangle bounds = map.getBounds();
							bounds.grow(-shrinkViewPort, -shrinkViewPort);

							int outcode = bounds.outcode(outOfBoundsPoint);
							double offsetX = 0;
							double offsetY = 0;

							if ((outcode & Rectangle.OUT_BOTTOM) != 0)
								offsetY = outOfBoundsPoint.y - bounds.getMaxY();

							if ((outcode & Rectangle.OUT_TOP) != 0)
								offsetY = outOfBoundsPoint.y - bounds.getMinY();

							if ((outcode & Rectangle.OUT_RIGHT) != 0)
								offsetX = outOfBoundsPoint.x - bounds.getMaxX();

							if ((outcode & Rectangle.OUT_LEFT) != 0)
								offsetX = outOfBoundsPoint.x - bounds.getMinX();

							map.setCenter(new Point2D.Double(
									center.getX() + offsetX * scale,
									center.getY() + offsetY * scale));

							end = createGeoRectangle(outOfBoundsPoint);
							selectedArea = start.createUnion(end);

							map.repaint();
						}

						isThreadRunning.set(false);
					}
				};
				t.setDaemon(true);
				t.start();
			}

			map.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (start != null) {
			outOfBounds = false;
			selectedArea = null;

			if (mouseDragged) {
				end = createGeoRectangle(e.getPoint());
				Rectangle2D tmp = start.createUnion(end);
				if (isVisibleOnScreen(tmp)) {
					selectedArea = tmp;
					eventDispatcher.triggerEvent(new BoundingBoxSelectionEvent(getBoundingBox(), this));
				}
			}

			mouseDragged = false;
			start = null;
			map.setPanEnabled(true);
			map.repaint();
		}
	}

	@Override
	public void paint(Graphics2D gd, JXMapViewer t, int i, int i1) {
		if (selectedArea != null) {
			Rectangle drawArea = createDrawArea(selectedArea, map.getZoom());

			if (!drawArea.isEmpty()) {
				if (drawArea.contains(map.getBounds()))
					drawArea = map.getBounds();			

				gd.setColor(regionColor);
				gd.fillRect(drawArea.x, drawArea.y, drawArea.width, drawArea.height);

				gd.setColor(borderColor);
				gd.drawRect(drawArea.x, drawArea.y, drawArea.width, drawArea.height);
			}
		}
	}

	private void checkOutOfBounds(Point point) {
		Rectangle bounds = map.getBounds();
		bounds.grow(-shrinkViewPort, -shrinkViewPort);

		outOfBounds = !bounds.contains(point);
		if (outOfBounds)
			outOfBoundsPoint = point;
	}

	private Rectangle2D createGeoRectangle(GeoPosition southWest, GeoPosition northEast) {
		Rectangle2D tmp = new Rectangle2D.Double(southWest.getLatitude(), southWest.getLongitude(), 0, 0);
		tmp = tmp.createUnion(new Rectangle2D.Double(northEast.getLatitude(), northEast.getLongitude(), 0, 0));

		return tmp;
	}

	private Rectangle2D createGeoRectangle(Point point) {
		GeoPosition pos = map.convertPointToGeoPosition(point);	
		return new Rectangle2D.Double(pos.getLatitude(), pos.getLongitude(), 0, 0);
	}

	private Rectangle createDrawArea(Rectangle2D geo, int zoom) {		
		Point2D southWest = map.convertGeoPositionToPoint(new GeoPosition(geo.getMinX(), geo.getMinY()), zoom);
		Point2D northEast = map.convertGeoPositionToPoint(new GeoPosition(geo.getMaxX(), geo.getMaxY()), zoom);

		Rectangle drawArea = new Rectangle((int)southWest.getX(), (int)southWest.getY(), 0, 0);
		return drawArea.union(new Rectangle((int)northEast.getX(), (int)northEast.getY(), 0, 0));
	}
}
