package de.tub.citydb.gui.components.mapviewer.map;
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

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.gui.components.mapviewer.map.event.BoundingBoxSelection;

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

	public GeoPosition[] getSelectedArea() {
		GeoPosition[] bounds = null;
		if (selectedArea != null) {
			bounds = new GeoPosition[2];
			bounds[0] = new GeoPosition(selectedArea.getMinY(), selectedArea.getMinX());
			bounds[1] = new GeoPosition(selectedArea.getMaxY(), selectedArea.getMaxX());
		}

		return bounds;
	}

	public void setSelectedArea(GeoPosition southWest, GeoPosition northEast) {
		selectedArea = new Rectangle2D.Double(southWest.getLatitude(), southWest.getLongitude(), 0, 0);
		selectedArea = selectedArea.createUnion(new Rectangle2D.Double(northEast.getLatitude(), northEast.getLongitude(), 0, 0));
		map.repaint();

		final GeoPosition[] bounds = getSelectedArea();
		if (bounds != null)
			eventDispatcher.triggerEvent(new BoundingBoxSelection(bounds, this));	
	}

	public void clearSelectedArea() {
		start = selectedArea = null;
		map.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (start != null)
			checkOutOfBounds(e.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && e.isAltDown()) {
			start = createGeoRectangle2D(e.getPoint());
			map.setPanEnabled(false);
		} else
			start = null;

		mouseDragged = false;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		if (start != null) {
			mouseDragged = true;
			end = createGeoRectangle2D(e.getPoint());
			selectedArea = start.createUnion(end);
			checkOutOfBounds(e.getPoint());

			if (outOfBounds && isThreadRunning.compareAndSet(false, true)) {
				Thread t = new Thread() {
					public void run() {
						while (outOfBounds && selectedArea != null) {
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

							end = createGeoRectangle2D(outOfBoundsPoint);
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

			if (mouseDragged) {
				end = createGeoRectangle2D(e.getPoint());
				selectedArea = start.createUnion(end);

				final GeoPosition[] bounds = getSelectedArea();
				if (bounds != null)
					eventDispatcher.triggerEvent(new BoundingBoxSelection(bounds, this));
			} else 
				selectedArea = null;

			mouseDragged = false;
			start = null;
			map.setPanEnabled(true);
			map.repaint();
		}
	}

	@Override
	public void paint(Graphics2D gd, JXMapViewer t, int i, int i1) {
		if (selectedArea != null) {
			Rectangle drawArea = createSelectionArea(selectedArea);

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

	private Rectangle2D createGeoRectangle2D(Point point) {
		GeoPosition pos = map.convertPointToGeoPosition(point);	
		return new Rectangle2D.Double(pos.getLatitude(), pos.getLongitude(), 0, 0);
	}

	private Rectangle createSelectionArea(Rectangle2D geo) {		
		Point2D southWest = map.convertGeoPositionToPoint(new GeoPosition(geo.getMinX(), geo.getMinY()));
		Point2D northEast = map.convertGeoPositionToPoint(new GeoPosition(geo.getMaxX(), geo.getMaxY()));

		Rectangle drawArea = new Rectangle((int)southWest.getX(), (int)southWest.getY(), 0, 0);
		return drawArea.union(new Rectangle((int)northEast.getX(), (int)northEast.getY(), 0, 0));
	}
}