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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import org.citydb.api.event.EventDispatcher;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.language.Language;
import org.citydb.gui.components.mapviewer.geocoder.Geocoder;
import org.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import org.citydb.gui.components.mapviewer.geocoder.Location;
import org.citydb.gui.components.mapviewer.geocoder.LocationType;
import org.citydb.gui.components.mapviewer.geocoder.ResultType;
import org.citydb.gui.components.mapviewer.geocoder.StatusCode;
import org.citydb.gui.components.mapviewer.map.DefaultWaypoint.WaypointType;
import org.citydb.gui.components.mapviewer.map.event.MapBoundsSelectionEvent;
import org.citydb.gui.components.mapviewer.map.event.ReverseGeocoderEvent;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;

@SuppressWarnings("serial")
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

		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.setCenterPosition(map.convertPointToGeoPosition(mousePosition));
				map.setZoom(map.getZoom() - 1);
			}
		});

		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				map.setCenterPosition(map.convertPointToGeoPosition(mousePosition));
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
				
				eventDispatcher.triggerEvent(new MapBoundsSelectionEvent(bounds, MapPopupMenu.this));
			}
		});

		geocode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eventDispatcher.triggerEvent(new ReverseGeocoderEvent(MapPopupMenu.this));
				final GeoPosition position = map.convertPointToGeoPosition(mousePosition);

				new SwingWorker<GeocoderResponse, Void>() {
					protected GeocoderResponse doInBackground() throws Exception {
						return Geocoder.geocode(position);
					}

					protected void done() {
						try {
							GeocoderResponse response = get();
							
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

								eventDispatcher.triggerEvent(new ReverseGeocoderEvent(location, MapPopupMenu.this));
							} else {
								mapViewer.getWaypointPainter().clearWaypoints();
								map.repaint();
								
								eventDispatcher.triggerEvent(new ReverseGeocoderEvent(response, MapPopupMenu.this));
							}
						} catch (InterruptedException e) {
							//
						} catch (ExecutionException e) {
							//
						}
					}
				}.execute();
			}
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
