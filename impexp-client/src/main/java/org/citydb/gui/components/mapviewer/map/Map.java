/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.i18n.Language;
import org.citydb.gui.components.mapviewer.MapWindow;
import org.citydb.gui.util.GuiUtil;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXMapViewer.MouseWheelZoomStyle;
import org.jdesktop.swingx.mapviewer.AbstractTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.CompoundPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Map {
	private JXMapKit mapKit;
	private BBoxSelectionPainter selectionPainter;
	private DefaultWaypointPainter waypointPainter;
	private MapPopupMenu popupMenu;
	private JPanel hints;

	private JPanel headerMenu;
	private JPanel footer;
	private JLabel hintsLabel;
	private JLabel[] hintLabels;
	private JLabel label;

	public Map() {
		initComponents();
	}

	private void initComponents() {
		mapKit = new JXMapKit();
		selectionPainter = new BBoxSelectionPainter(mapKit.getMainMap());
		waypointPainter = new DefaultWaypointPainter();
		ZoomPainter zoomPainter = new ZoomPainter(mapKit.getMainMap());
		popupMenu = new MapPopupMenu(this);

		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new GridBagLayout());

		headerMenu = new JPanel();
		headerMenu.setLayout(new GridBagLayout());
		{
			hintsLabel = new JLabel();
			hintsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			hintsLabel.setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/help.png")));
			hintsLabel.setIconTextGap(new JCheckBox().getIconTextGap());

			headerMenu.add(hintsLabel, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 2, 2, 2, 5));

			// usage hints
			hints = new JPanel();
			hints.setLayout(new GridBagLayout());
			hints.setVisible(false);

			hintLabels = new JLabel[7];
			JLabel[] hintIcons = new JLabel[7];

			for (int i = 0; i < hintLabels.length; ++i) {
				hintLabels[i] = new JLabel();
				hintIcons[i] = new JLabel();
				hintIcons[i].setOpaque(false);
			}

			hintIcons[0].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/selection.png")));
			hintIcons[1].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/waypoint_small.png")));
			hintIcons[2].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/magnifier.png")));
			hintIcons[3].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/magnifier_plus_selection.png")));
			hintIcons[4].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/move.png")));
			hintIcons[5].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/center.png")));
			hintIcons[6].setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/images/map/popup.png")));

			for (int i = 0; i < hintLabels.length; ++i) {
				hints.add(hintIcons[i], GuiUtil.setConstraints(0, i, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 5, 5, 1, 5));
				hints.add(hintLabels[i], GuiUtil.setConstraints(1, i, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 5, 5, 1, 5));
			}

			header.add(headerMenu, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
			header.add(hints, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0, 0, 0));
		}

		footer = new JPanel();
		footer.setLayout(new GridBagLayout());
		JLabel copyright;
		{
			// footer label
			label = new JLabel("[n/a]");
			label.setPreferredSize(new Dimension(200, label.getPreferredSize().height));
			copyright = new JLabel("<html><body>&copy; <a href=\"\">OpenStreetMap</a> contributors</html></body>");
			copyright.setCursor(new Cursor(Cursor.HAND_CURSOR));

			footer.add(label, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.HORIZONTAL, 2, 2, 2, 2));
			footer.add(copyright, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 2, 2, 2, 105));
		}

		mapKit.getMainMap().add(footer, GuiUtil.setConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		mapKit.getMainMap().add(header, GuiUtil.setConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

		mapKit.setDefaultProvider(DefaultProviders.OpenStreetMaps);
		((AbstractTileFactory)mapKit.getMainMap().getTileFactory()).setThreadPoolSize(10);
		mapKit.setDataProviderCreditShown(false);
		mapKit.setAddressLocationShown(true);
		mapKit.getMainMap().setRecenterOnClickEnabled(true);	
		mapKit.getMainMap().setRestrictOutsidePanning(true);
		mapKit.getMainMap().setHorizontalWrapped(false);

		mapKit.getMiniMap().setMouseWheelZoomStyle(MouseWheelZoomStyle.VIEW_CENTER);
		mapKit.getMiniMap().setRestrictOutsidePanning(true);
		mapKit.getMiniMap().setHorizontalWrapped(false);

		CompoundPainter<JXMapViewer> compound = new CompoundPainter<>();
		compound.setPainters(selectionPainter, zoomPainter, waypointPainter);
		mapKit.getMainMap().setOverlayPainter(compound);

		// popup menu
		mapKit.getMainMap().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			private void showPopupMenu(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.setMousePosition(e.getPoint());
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
					popupMenu.setInvoker(mapKit.getMainMap());
				}
			}
		});

		// footer coordinates update
		mapKit.getMainMap().addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				paintCoordinates(e);
			}

			public void mouseMoved(MouseEvent e) {
				paintCoordinates(e);
			}

			private void paintCoordinates(MouseEvent e) {
				GeoPosition pos = mapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
				label.setText("[" + MapWindow.LAT_LON_FORMATTER.format(pos.getLatitude()) + ",  " + MapWindow.LAT_LON_FORMATTER.format(pos.getLongitude()) + "]");
				mapKit.repaint();
			}			
		});

		// usage hints
		MouseAdapter hintsMouseAdapter = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					boolean visible = !hints.isVisible();
					String hintsText = !visible ? Language.I18N.getString("map.hints.show") : Language.I18N.getString("map.hints.hide");
					hintsLabel.setText("<html><a href=\"\">" + hintsText + "</a></html>");
					hints.setVisible(visible);
				}
			}
		};
		
		hintsLabel.addMouseListener(hintsMouseAdapter);
		hints.addMouseListener(hintsMouseAdapter);

		copyright.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://osm.org/copyright"));
				} catch (IOException | URISyntaxException ignored) {
					//
				}
			}
		});

		// just to disable double-click action
		headerMenu.addMouseListener(new MouseAdapter() {});
		footer.addMouseListener(new MouseAdapter() {});

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(this::updateComponentUI);
			}
		});

		updateComponentUI();
	}

	private void updateComponentUI() {
		Color transparentBackground = UIManager.getColor("TabbedPane.background");
		transparentBackground = new Color(transparentBackground.getRed(), transparentBackground.getGreen(), transparentBackground.getBlue(), 200);

		headerMenu.setBackground(transparentBackground);
		hints.setBackground(transparentBackground);
		footer.setBackground(transparentBackground);
		hints.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, UIManager.getColor("Component.borderColor")));

		SwingUtilities.updateComponentTreeUI(popupMenu);
	}

	public JXMapKit getMapKit() {
		return mapKit;
	}

	public BBoxSelectionPainter getSelectionPainter() {
		return selectionPainter;
	}

	public DefaultWaypointPainter getWaypointPainter() {
		return waypointPainter;
	}

	public void doTranslation() {
		String hintsText = !hints.isVisible() ? Language.I18N.getString("map.hints.show") : Language.I18N.getString("map.hints.hide");
		hintsLabel.setText("<html><a href=\"\">" + hintsText + "</a></html>");

		hintLabels[0].setText("<html><b>" + Language.I18N.getString("map.hints.selectBoundingBox")+ "</b><br/>" + Language.I18N.getString("map.hints.selectBoundingBox.hint") + "</html>");
		hintLabels[1].setText("<html><b>" + Language.I18N.getString("map.hints.reverseGeocoder")+ "</b><br/>" + Language.I18N.getString("map.hints.reverseGeocoder.hint") + "</html>");
		hintLabels[2].setText("<html><b>" + Language.I18N.getString("map.hints.zoom")+ "</b><br/>" + Language.I18N.getString("map.hints.zoom.hint") + "</html>");
		hintLabels[3].setText("<html><b>" + Language.I18N.getString("map.hints.zoomSelected")+ "</b><br/>" + Language.I18N.getString("map.hints.zoomSelected.hint") + "</html>");
		hintLabels[4].setText("<html><b>" + Language.I18N.getString("map.hints.move")+ "</b><br/>" + Language.I18N.getString("map.hints.move.hint") + "</html>");
		hintLabels[5].setText("<html><b>" + Language.I18N.getString("map.hints.center")+ "</b><br/>" + Language.I18N.getString("map.hints.center.hint") + "</html>");
		hintLabels[6].setText("<html><b>" + Language.I18N.getString("map.hints.popup")+ "</b><br/>" + Language.I18N.getString("map.hints.popup.hint") + "</html>");
	
		popupMenu.doTranslation();
	}

}
