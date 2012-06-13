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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXMapViewer.MouseWheelZoomStyle;
import org.jdesktop.swingx.mapviewer.AbstractTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.CompoundPainter;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.components.mapviewer.MapWindow;
import de.tub.citydb.util.gui.GuiUtil;

public class Map {
	private JXMapKit mapKit;
	private BBoxSelectionPainter selectionPainter;
	private DefaultWaypointPainter waypointPainter;
	private ZoomPainter zoomPainter;
	private MapPopupMenu popupMenu;
	private JPanel hints;

	private JLabel hintsLabel;
	private JLabel hintLabels[];
	private JLabel hintIcons[];
	private JLabel label;

	public Map(Config config) {
		initComponents(config);
	}

	private void initComponents(Config config) {
		mapKit = new JXMapKit();
		selectionPainter = new BBoxSelectionPainter(mapKit.getMainMap());
		waypointPainter = new DefaultWaypointPainter();
		zoomPainter = new ZoomPainter(mapKit.getMainMap());
		popupMenu = new MapPopupMenu(this);

		Color borderColor = new Color(0, 0, 0, 150);

		final JPanel footer = new JPanel();
		footer.setBackground(borderColor);
		footer.setLayout(new GridBagLayout());

		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0);
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
		mapKit.getMainMap().add(footer, gridBagConstraints);		
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		mapKit.getMainMap().add(header, gridBagConstraints);

		JPanel headerMenu = new JPanel();
		headerMenu.setBackground(borderColor);
		headerMenu.setLayout(new GridBagLayout());

		hints = new JPanel();
		hints.setBackground(new Color(hints.getBackground().getRed(), hints.getBackground().getBlue(), hints.getBackground().getGreen(), 220));
		hints.setLayout(new GridBagLayout());
		hints.setBorder(BorderFactory.createMatteBorder(0, 2, 2, 2, borderColor));
		hints.setVisible(false);

		header.add(headerMenu, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		gridBagConstraints = GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0);
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		header.add(hints, gridBagConstraints);

		hintsLabel = new JLabel();
		hintsLabel.setBackground(borderColor);
		hintsLabel.setForeground(Color.WHITE);
		hintsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		hintsLabel.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/help.png")));
		hintsLabel.setIconTextGap(5);

		headerMenu.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		headerMenu.add(hintsLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 2, 2, 2, 5));

		// usage hints
		hintLabels = new JLabel[7];
		hintIcons = new JLabel[7];

		for (int i = 0; i < hintLabels.length; ++i) {
			hintLabels[i] = new JLabel();
			hintIcons[i] = new JLabel();
			hintIcons[i].setOpaque(false);
		}
		
		hintIcons[0].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/selection.png")));
		hintIcons[1].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/waypoint_small.png")));
		hintIcons[2].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/magnifier.png")));
		hintIcons[3].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/magnifier_plus_selection.png")));
		hintIcons[4].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/move.png")));
		hintIcons[5].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/center.png")));
		hintIcons[6].setIcon(new ImageIcon(getClass().getResource("/resources/img/map/popup.png")));

		gridBagConstraints = GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 5, 5, 1, 5);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;

		for (int i = 0; i < hintLabels.length; ++i) {			
			gridBagConstraints.gridy = i;
			gridBagConstraints.gridx = 0;
			hints.add(hintIcons[i], gridBagConstraints);
			gridBagConstraints.gridx = 1;
			hints.add(hintLabels[i], gridBagConstraints);
		}

		// footer label
		label = new JLabel("[n/a]");
		label.setForeground(Color.WHITE);
		label.setPreferredSize(new Dimension(200, label.getPreferredSize().height));
		JLabel copyright = new JLabel("<html><body>Map data &copy; 'OpenStreetMap' <i>(and)</i> contributors, CC-BY-SA</html></body>");
		copyright.setForeground(Color.WHITE);

		footer.add(label, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.HORIZONTAL, 2, 2, 2, 2));
		gridBagConstraints = GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.NONE, 2, 2, 2, 105);
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		footer.add(copyright, gridBagConstraints);

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

		CompoundPainter<JXMapViewer> compound = new CompoundPainter<JXMapViewer>();		
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
					String hintsText = !visible ? Internal.I18N.getString("map.hints.show") : Internal.I18N.getString("map.hints.hide");
					hintsLabel.setText("<html><u>" + hintsText + "</u></html>");
					hints.setVisible(visible);
				}
			}
		};
		
		hintsLabel.addMouseListener(hintsMouseAdapter);
		hints.addMouseListener(hintsMouseAdapter);

		// just to disable double-click action
		headerMenu.addMouseListener(new MouseAdapter() {});
		footer.addMouseListener(new MouseAdapter() {});
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
		String hintsText = !hints.isVisible() ? Internal.I18N.getString("map.hints.show") : Internal.I18N.getString("map.hints.hide");
		hintsLabel.setText("<html><u>" + hintsText + "</u></html>");

		hintLabels[0].setText("<html><b>" + Internal.I18N.getString("map.hints.selectBoundingBox")+ "</b><br/>" + Internal.I18N.getString("map.hints.selectBoundingBox.hint") + "</html>");
		hintLabels[1].setText("<html><b>" + Internal.I18N.getString("map.hints.reverseGeocoder")+ "</b><br/>" + Internal.I18N.getString("map.hints.reverseGeocoder.hint") + "</html>");
		hintLabels[2].setText("<html><b>" + Internal.I18N.getString("map.hints.zoom")+ "</b><br/>" + Internal.I18N.getString("map.hints.zoom.hint") + "</html>");
		hintLabels[3].setText("<html><b>" + Internal.I18N.getString("map.hints.zoomSelected")+ "</b><br/>" + Internal.I18N.getString("map.hints.zoomSelected.hint") + "</html>");
		hintLabels[4].setText("<html><b>" + Internal.I18N.getString("map.hints.move")+ "</b><br/>" + Internal.I18N.getString("map.hints.move.hint") + "</html>");
		hintLabels[5].setText("<html><b>" + Internal.I18N.getString("map.hints.center")+ "</b><br/>" + Internal.I18N.getString("map.hints.center.hint") + "</html>");
		hintLabels[6].setText("<html><b>" + Internal.I18N.getString("map.hints.popup")+ "</b><br/>" + Internal.I18N.getString("map.hints.popup.hint") + "</html>");
	
		popupMenu.doTranslation();
	}

}
