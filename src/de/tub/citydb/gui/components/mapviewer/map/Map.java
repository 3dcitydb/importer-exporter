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

import de.tub.citydb.gui.components.mapviewer.MapWindow;
import de.tub.citydb.util.gui.GuiUtil;

public class Map {
	private JXMapKit mapKit;
	private BBoxSelectionPainter selectionPainter;
	private DefaultWaypointPainter waypointPainter;
	private ZoomPainter zoomPainter;
	private MapPopupMenu popupMenu;
	private JPanel hints;
	
	public Map() {
		initComponents();
	}

	private void initComponents() {
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

		final JLabel hintsLabel = new JLabel("<html><u>Show usage hints</u></html>");
		hintsLabel.setBackground(borderColor);
		hintsLabel.setForeground(Color.WHITE);
		hintsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

		headerMenu.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		headerMenu.add(hintsLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 2, 2, 2, 5));

		// usage hints
		createUsageHint("Select bounding box", new ImageIcon(getClass().getResource("/resources/img/map/selection.png")),
				"Hold Alt key and left mouse button to select bounding box", 0);
		
		createUsageHint("Lookup address", new ImageIcon(getClass().getResource("/resources/img/map/waypoint_small.png")),
				"Press right mouse button to open popup menu", 1);

		createUsageHint("Zoom in/out", new ImageIcon(getClass().getResource("/resources/img/map/magnifier.png")),
				"Use mouse wheel", 2);
		
		createUsageHint("Zoom into selected area", new ImageIcon(getClass().getResource("/resources/img/map/magnifier_plus_selection.png")),
				"Hold Shift key and left mouse button to select area", 3);

		createUsageHint("Move map", new ImageIcon(getClass().getResource("/resources/img/map/move.png")),
				"Hold left mouse button to move the map", 4);

		createUsageHint("Center map and zoom in", new ImageIcon(getClass().getResource("/resources/img/map/center.png")),
				"Double-click left mouse button to center map", 5);

		
		// footer label
		final JLabel label = new JLabel("[n/a]");
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
		hintsLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					boolean visible = !hints.isVisible();				
					hintsLabel.setText(visible ? "<html><u>Hide usage hints</u></html>" : "<html><u>Show usage hints</u></html>");
					hints.setVisible(visible);
				}
			}
		});
		
		// just to disable double-click action
		headerMenu.addMouseListener(new MouseAdapter() {});
		hints.addMouseListener(new MouseAdapter() {});
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

	public void addBBoxSelectionListener(BBoxSelectionListener listener) {
		selectionPainter.addBBoxSelectionListener(listener);
	}

	public boolean removeBBoxSelectionListener(BBoxSelectionListener listener) {
		return selectionPainter.removeBBoxSelectionListener(listener);
	}

	public void addReverseGeocoderListener(ReverseGeocoderListener listener) {
		popupMenu.addReverseGeocoderListener(listener);
	}

	public boolean removeReverseGeocoderListener(ReverseGeocoderListener listener) {
		return popupMenu.removeReverseGeocoderListener(listener);
	}

	public void addMapBoundsListener(MapBoundsListener listener) {
		popupMenu.addMapBoundsListener(listener);
	}

	public boolean removeMapBoundsListener(MapBoundsListener listener) {
		return popupMenu.removeMapBoundsListener(listener);
	}
	
	private void createUsageHint(String action, ImageIcon actionIcon, String hint, int level) {
		action = action.replaceAll("\\n", "<br/>");
		hint = hint.replaceAll("\\n", "<br/>");
		
		JLabel actionLabel = new JLabel();
		actionLabel.setIcon(actionIcon);
		actionLabel.setOpaque(false);

		JLabel hintLabel = new JLabel("<html><b>" + action + "</b><br/>" + hint + "</html>");
		
		GridBagConstraints gridBagConstraints = GuiUtil.setConstraints(0, level, 0, 0, GridBagConstraints.NONE, 5, 5, 1, 5);
		gridBagConstraints.anchor = GridBagConstraints.NORTH;
		hints.add(actionLabel, gridBagConstraints);
		hints.add(hintLabel, GuiUtil.setConstraints(1, level, 1, 0, GridBagConstraints.BOTH, 5, 5, 1, 5));
	}

}
