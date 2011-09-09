package de.tub.citydb.gui.components.mapviewer.map;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;

import de.tub.citydb.gui.components.mapviewer.geocoder.Geocoder;
import de.tub.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import de.tub.citydb.gui.components.mapviewer.geocoder.Location;
import de.tub.citydb.gui.components.mapviewer.geocoder.LocationType;
import de.tub.citydb.gui.components.mapviewer.geocoder.ResultType;
import de.tub.citydb.gui.components.mapviewer.geocoder.StatusCode;
import de.tub.citydb.gui.components.mapviewer.map.DefaultWaypoint.WaypointType;

@SuppressWarnings("serial")
public class MapPopupMenu extends JPopupMenu {
	private final Map mapViewer;
	private final JXMapViewer map;

	private List<ReverseGeocoderListener> reverseListener;
	private List<MapBoundsListener> boundsListener;

	private JMenuItem zoomIn;
	private JMenuItem zoomOut;
	private JMenuItem centerMap;
	private JMenuItem mapBounds;
	private JMenuItem geocode;

	private Point mousePosition;

	public MapPopupMenu(Map mapViewer) {
		this.mapViewer = mapViewer;
		this.map = mapViewer.getMapKit().getMainMap();
		init();
	}

	private void init() { 
		zoomIn = new JMenuItem("Zoom in");
		zoomOut = new JMenuItem("Zoom out");
		centerMap = new JMenuItem("Center map here");
		mapBounds = new JMenuItem("Get map bounds");
		geocode = new JMenuItem("Lookup address");

		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.setZoom(map.getZoom() - 1);
			}
		});

		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.setZoom(map.getZoom() + 1);
			}
		});

		centerMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.setCenterPosition(map.convertPointToGeoPosition(mousePosition));
			}
		});

		mapBounds.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				Rectangle view = map.getViewportBounds();
				TileFactory fac = map.getTileFactory();
				int zoom = map.getZoom();

				final GeoPosition[] bounds = new GeoPosition[2];
				bounds[0] = fac.pixelToGeo(new Point2D.Double(view.getMinX(), view.getMaxY()), zoom);
				bounds[1] = fac.pixelToGeo(new Point2D.Double(view.getMaxX(), view.getMinY()), zoom);

				if (!boundsListener.isEmpty()) {
					Thread t = new Thread() {
						public void run() {
							for (final MapBoundsListener listener : boundsListener) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										listener.getMapBounds(bounds);	
									}
								});								
							}
						}
					};
					t.setDaemon(true);
					t.start();
				}
			}
		});

		geocode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						for (final ReverseGeocoderListener listener : reverseListener) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									listener.searching();	
								}
							});								
						}

						GeoPosition position = map.convertPointToGeoPosition(mousePosition);
						final GeocoderResponse response = Geocoder.geocode(position);

						if (response.getStatus() == StatusCode.OK) {
							int index;

							for (index = 0; index < response.getLocations().length; ++index) {
								Location tmp = response.getLocations()[index];

								Point2D southWest = map.convertGeoPositionToPoint(tmp.getViewPort().getSouthWest());
								Rectangle2D sizeOnScreen = new Rectangle.Double(southWest.getX(), southWest.getY(), 0, 0);
								sizeOnScreen.add(map.convertGeoPositionToPoint(tmp.getViewPort().getNorthEast()));

								if (tmp.getResultTypes().contains(ResultType.POSTAL_CODE))
									continue;

								if (sizeOnScreen.getHeight() * sizeOnScreen.getWidth() >= 500)
									break;
							}

							if (index == response.getLocations().length)
								--index;

							final Location location = response.getLocations()[index];
							Set<GeoPosition> set = new HashSet<GeoPosition>(2);
							set.add(location.getPosition());
							set.add(position);
							map.calculateZoomFrom(set);

							WaypointType type = location.getLocationType() == LocationType.ROOFTOP ? 
									WaypointType.PRECISE : WaypointType.APPROXIMATE;

							mapViewer.getWaypointPainter().showWaypoints(
									new DefaultWaypoint(position, WaypointType.REVERSE),
									new DefaultWaypoint(location.getPosition(), type));
							map.repaint();

							if (!reverseListener.isEmpty()) {
								for (final ReverseGeocoderListener listener : reverseListener) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											listener.process(location);	
										}
									});								
								}
							}

						} else {
							mapViewer.getWaypointPainter().clearWaypoints();
							map.repaint();

							if (!reverseListener.isEmpty()) {
								for (final ReverseGeocoderListener listener : reverseListener) {
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											listener.error(response);							
										}
									});	
								}					
							}
						}
					}
				};
				t.setDaemon(true);
				t.start();
			}
		});

		add(zoomIn);
		add(zoomOut);
		add(centerMap);
		add(mapBounds);
		addSeparator();
		add(geocode);
	}

	public void setMousePosition(Point mousePosition) {
		this.mousePosition = mousePosition;
	}

	protected void addReverseGeocoderListener(ReverseGeocoderListener listener) {
		if (reverseListener == null)
			reverseListener = new ArrayList<ReverseGeocoderListener>();

		reverseListener.add(listener);
	}

	protected boolean removeReverseGeocoderListener(ReverseGeocoderListener listener) {
		return reverseListener != null ? reverseListener.remove(listener) : false;
	}

	protected void addMapBoundsListener(MapBoundsListener listener) {
		if (boundsListener == null)
			boundsListener = new ArrayList<MapBoundsListener>();

		boundsListener.add(listener);
	}

	protected boolean removeMapBoundsListener(MapBoundsListener listener) {
		return boundsListener != null ? boundsListener.remove(listener) : false;
	}

}
