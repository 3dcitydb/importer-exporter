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

import org.citydb.config.gui.window.GeocodingServiceName;
import org.citydb.config.i18n.Language;
import org.citydb.util.event.EventDispatcher;
import org.citydb.gui.map.geocoder.Geocoder;
import org.citydb.gui.map.geocoder.GeocoderResult;
import org.citydb.gui.map.geocoder.Location;
import org.citydb.gui.map.geocoder.LocationType;
import org.citydb.gui.map.geocoder.service.GeocodingServiceException;
import org.citydb.gui.map.map.DefaultWaypoint.WaypointType;
import org.citydb.gui.map.map.event.MapBoundsSelectionEvent;
import org.citydb.gui.map.map.event.ReverseGeocoderEvent;
import org.citydb.core.registry.ObjectRegistry;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MapPopupMenu extends JPopupMenu {
	private final Map mapViewer;
	private final JXMapViewer map;
	private final EventDispatcher eventDispatcher;

	private JMenuItem zoomIn;
	private JMenuItem zoomOut;
	private JMenuItem centerMap;
	private JMenuItem mapBounds;
	private JMenuItem geocode;

	private Point mousePosition;

	public MapPopupMenu(Map mapViewer) {
		this.mapViewer = mapViewer;
		this.map = mapViewer.getMapKit().getMainMap();
		
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		init();
	}

	private void init() { 
		zoomIn = new JMenuItem("Zoom in");
		zoomOut = new JMenuItem("Zoom out");
		centerMap = new JMenuItem("Center map here");
		mapBounds = new JMenuItem("Get map bounds");
		geocode = new JMenuItem("Lookup address");

		zoomIn.addActionListener(e -> {
			map.setCenterPosition(map.convertPointToGeoPosition(mousePosition));
			map.setZoom(map.getZoom() - 1);
		});

		zoomOut.addActionListener(e -> {
			map.setCenterPosition(map.convertPointToGeoPosition(mousePosition));
			map.setZoom(map.getZoom() + 1);
		});

		centerMap.addActionListener(e -> map.setCenterPosition(map.convertPointToGeoPosition(mousePosition)));

		mapBounds.addActionListener(e -> {
			Rectangle view = map.getViewportBounds();
			TileFactory fac = map.getTileFactory();
			int zoom = map.getZoom();

			final GeoPosition[] bounds = new GeoPosition[2];
			bounds[0] = fac.pixelToGeo(new Point2D.Double(view.getMinX(), view.getMaxY()), zoom);
			bounds[1] = fac.pixelToGeo(new Point2D.Double(view.getMaxX(), view.getMinY()), zoom);

			eventDispatcher.triggerEvent(new MapBoundsSelectionEvent(bounds, MapPopupMenu.this));
		});

		geocode.addActionListener(e -> {
			eventDispatcher.triggerEvent(new ReverseGeocoderEvent(
					ReverseGeocoderEvent.ReverseGeocoderStatus.SEARCHING, MapPopupMenu.this));
			final GeoPosition position = map.convertPointToGeoPosition(mousePosition);

			new SwingWorker<GeocoderResult, Void>() {
				protected GeocoderResult doInBackground() throws Exception {
					return Geocoder.getInstance().lookupAddress(position, map.getZoom());
				}

				protected void done() {
					try {
						GeocoderResult result = get();

						if (result.isSetLocations()) {
							int i;
							for (i = 0; i < result.getLocations().size(); ++i) {
								Location location = result.getLocations().get(i);

								if (Geocoder.getInstance().getGeocodingServiceName() == GeocodingServiceName.GOOGLE_GEOCODING_API) {
									List<?> types = location.getAttribute("types", List.class);
									if (types != null && types.contains("postal_code"))
										continue;
								}

								Point2D southWest = map.convertGeoPositionToPoint(location.getViewPort().getSouthWest());
								Rectangle2D sizeOnScreen = new Rectangle.Double(southWest.getX(), southWest.getY(), 0, 0);
								sizeOnScreen.add(map.convertGeoPositionToPoint(location.getViewPort().getNorthEast()));

								if (sizeOnScreen.getHeight() * sizeOnScreen.getWidth() >= 500)
									break;
							}

							if (i == result.getLocations().size())
								--i;

							final Location location = result.getLocations().get(i);
							Set<GeoPosition> set = new HashSet<>(2);
							set.add(location.getPosition());
							set.add(position);
							map.calculateZoomFrom(set);

							WaypointType type = location.getLocationType() == LocationType.PRECISE ?
									WaypointType.PRECISE : WaypointType.APPROXIMATE;

							mapViewer.getWaypointPainter().showWaypoints(
									new DefaultWaypoint(position, WaypointType.REVERSE),
									new DefaultWaypoint(location.getPosition(), type));
							map.repaint();

							eventDispatcher.triggerEvent(new ReverseGeocoderEvent(location, MapPopupMenu.this));
						} else {
							mapViewer.getWaypointPainter().clearWaypoints();
							map.repaint();

							eventDispatcher.triggerEvent(new ReverseGeocoderEvent(
									ReverseGeocoderEvent.ReverseGeocoderStatus.NO_RESULT, MapPopupMenu.this));
						}
					} catch (InterruptedException | ExecutionException e) {
						mapViewer.getWaypointPainter().clearWaypoints();
						map.repaint();

						GeocodingServiceException exception;
						if (e.getCause() instanceof GeocodingServiceException)
							exception = (GeocodingServiceException) e.getCause();
						else {
							exception = new GeocodingServiceException("An error occured while calling the geocoding service.");
							exception.addMessage("Caused by: " + e.getMessage());
						}

						eventDispatcher.triggerEvent(new ReverseGeocoderEvent(exception, MapPopupMenu.this));
					}
				}
			}.execute();
		});

		add(zoomIn);
		add(zoomOut);
		add(centerMap);
		addSeparator();
		add(mapBounds);
		addSeparator();
		add(geocode);
	}

	public void setMousePosition(Point mousePosition) {
		this.mousePosition = mousePosition;
	}

	protected void doTranslation() {
		zoomIn.setText(Language.I18N.getString("map.popup.zoomIn"));
		zoomOut.setText(Language.I18N.getString("map.popup.zoomOut"));
		centerMap.setText(Language.I18N.getString("map.popup.centerMap"));
		mapBounds.setText(Language.I18N.getString("map.popup.mapBounds"));
		geocode.setText(Language.I18N.getString("map.popup.geocode"));
	}

}
