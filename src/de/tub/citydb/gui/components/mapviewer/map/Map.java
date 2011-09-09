package de.tub.citydb.gui.components.mapviewer.map;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
		Color iconBackground = new Color(0, 0, 0, 70);
		
		final JPanel footer = new JPanel();
		footer.setBackground(borderColor);
		footer.setLayout(new GridBagLayout());

		JPanel header = new JPanel();
		header.setBackground(borderColor);
		header.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0);
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
		mapKit.getMainMap().add(footer, gridBagConstraints);		
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		mapKit.getMainMap().add(header, gridBagConstraints);

		// header icons
		JLabel magnifierSelection = new JLabel();
		magnifierSelection.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/magnifier_plus_selection.png")));
		magnifierSelection.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		magnifierSelection.setBackground(iconBackground);
		magnifierSelection.setOpaque(true);

		JLabel magnifierSelectionAction = new JLabel("+ SHIFT");
		magnifierSelectionAction.setFont(new Font("SansSerif", Font.BOLD, magnifierSelectionAction.getFont().getSize()));
		magnifierSelectionAction.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/mouse_left.png")));
		magnifierSelectionAction.setIconTextGap(0);
		magnifierSelectionAction.setForeground(Color.WHITE);

		JLabel magnifier = new JLabel();
		magnifier.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/magnifier.png")));
		magnifier.setForeground(Color.WHITE);
		magnifier.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		magnifier.setBackground(iconBackground);
		magnifier.setOpaque(true);

		JLabel magnifierAction = new JLabel();
		magnifierAction.setFont(new Font("SansSerif", Font.BOLD, magnifierSelectionAction.getFont().getSize()));
		magnifierAction.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/mouse_middle.png")));
		magnifierAction.setIconTextGap(0);
		magnifierAction.setForeground(Color.WHITE);

		JLabel bbox = new JLabel();
		bbox.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/selection.png")));
		bbox.setForeground(Color.WHITE);
		bbox.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		bbox.setBackground(iconBackground);
		bbox.setOpaque(true);

		JLabel bboxAction = new JLabel("+ ALT");
		bboxAction.setFont(new Font("SansSerif", Font.BOLD, magnifierSelectionAction.getFont().getSize()));
		bboxAction.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/mouse_left.png")));
		bboxAction.setIconTextGap(0);
		bboxAction.setForeground(Color.WHITE);

		JLabel address = new JLabel();
		address.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/waypoint_small.png")));
		address.setForeground(Color.WHITE);
		address.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		address.setBackground(iconBackground);
		address.setOpaque(true);

		JLabel addressAction = new JLabel();
		addressAction.setFont(new Font("SansSerif", Font.BOLD, magnifierSelectionAction.getFont().getSize()));
		addressAction.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/mouse_right.png")));
		addressAction.setIconTextGap(0);
		addressAction.setForeground(Color.WHITE);	

		header.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		header.add(magnifier, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 1, 0));
		header.add(magnifierAction, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 1, 0));
		header.add(magnifierSelection, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.BOTH, 0, 20, 1, 0));
		header.add(magnifierSelectionAction, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 1, 0));
		header.add(bbox, GuiUtil.setConstraints(5, 0, 0, 0, GridBagConstraints.BOTH, 0, 20, 1, 0));
		header.add(bboxAction, GuiUtil.setConstraints(6, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 1, 0));
		header.add(address, GuiUtil.setConstraints(7, 0, 0, 0, GridBagConstraints.BOTH, 0, 20, 1, 0));
		header.add(addressAction, GuiUtil.setConstraints(8, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 1, 0));		

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

}
