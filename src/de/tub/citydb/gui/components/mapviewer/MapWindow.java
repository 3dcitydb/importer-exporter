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
package de.tub.citydb.gui.components.mapviewer;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.html.HTMLDocument;

import org.jdesktop.swingx.mapviewer.AbstractTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.window.WindowSize;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Database.PredefinedSrsName;
import de.tub.citydb.gui.components.bbox.BoundingBoxClipboardHandler;
import de.tub.citydb.gui.components.bbox.BoundingBoxListener;
import de.tub.citydb.gui.components.mapviewer.geocoder.Geocoder;
import de.tub.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import de.tub.citydb.gui.components.mapviewer.geocoder.Location;
import de.tub.citydb.gui.components.mapviewer.geocoder.LocationType;
import de.tub.citydb.gui.components.mapviewer.geocoder.ResponseType;
import de.tub.citydb.gui.components.mapviewer.geocoder.StatusCode;
import de.tub.citydb.gui.components.mapviewer.map.DefaultWaypoint;
import de.tub.citydb.gui.components.mapviewer.map.DefaultWaypoint.WaypointType;
import de.tub.citydb.gui.components.mapviewer.map.Map;
import de.tub.citydb.gui.components.mapviewer.map.event.BoundingBoxSelectionEvent;
import de.tub.citydb.gui.components.mapviewer.map.event.MapBoundsSelectionEvent;
import de.tub.citydb.gui.components.mapviewer.map.event.MapEvents;
import de.tub.citydb.gui.components.mapviewer.map.event.ReverseGeocoderEvent;
import de.tub.citydb.gui.components.mapviewer.map.event.ReverseGeocoderEvent.ReverseGeocoderStatus;
import de.tub.citydb.gui.components.mapviewer.validation.BoundingBoxValidator;
import de.tub.citydb.gui.components.mapviewer.validation.BoundingBoxValidator.ValidationResult;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class MapWindow extends JDialog implements EventHandler {
	private final Logger LOG = Logger.getInstance();
	private static MapWindow instance = null;
	public static DecimalFormat LAT_LON_FORMATTER = new DecimalFormat("##0.0000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

	static {
		LAT_LON_FORMATTER.setMaximumIntegerDigits(3);
		LAT_LON_FORMATTER.setMinimumIntegerDigits(1);
		LAT_LON_FORMATTER.setMinimumFractionDigits(2);
		LAT_LON_FORMATTER.setMaximumFractionDigits(7);
	}

	private final Config config;

	private Map map;	
	private JComboBox searchBox;
	private JLabel searchResult;
	private ImageIcon loadIcon;
	private volatile boolean updateSearchBox = true;

	private JFormattedTextField minX;
	private JFormattedTextField minY;
	private JFormattedTextField maxX;
	private JFormattedTextField maxY;

	private JButton goButton;
	private JButton applyButton;
	private JButton cancelButton;
	private JButton copyBBox;
	private JButton pasteBBox;
	private JButton showBBox;
	private JButton clearBBox;

	private JLabel bboxTitel;
	private JLabel reverseTitle;
	private JLabel reverseInfo;
	private JTextPane reverseText;
	private JLabel reverseSearchProgress;

	private JLabel helpTitle;
	private JLabel helpText;

	private JLabel googleMapsTitle;
	private JButton googleMapsButton;

	private BoundingBoxListener listener;
	private BBoxPopupMenu[] bboxPopups;
	private JFrame mainFrame;
	private BoundingBoxClipboardHandler clipboardHandler;
	private BoundingBoxValidator validator;

	private MapWindow(Config config) {
		super(ObjectRegistry.getInstance().getViewController().getTopFrame(), true);
		this.config = config;

		// register for events
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.SWITCH_LOCALE, this);
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(MapEvents.BOUNDING_BOX_SELECTION, this);
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(MapEvents.MAP_BOUNDS, this);
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(MapEvents.REVERSE_GEOCODER, this);

		clipboardHandler = BoundingBoxClipboardHandler.getInstance(config);
		validator = new BoundingBoxValidator(this, config);

		init();
		doTranslation();
	}

	public static final synchronized MapWindow getInstance(Config config) {
		if (instance == null)
			instance = new MapWindow(config);

		instance.applyButton.setVisible(false);
		instance.setSizeOnScreen();

		return instance;
	}

	public static final synchronized MapWindow getInstance(BoundingBoxListener listener, Config config) {
		instance = getInstance(config);

		if (listener != null) {
			instance.listener = listener;
			instance.applyButton.setVisible(true);
		}

		return instance;
	}

	private void init() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/img/map/map_icon.png")));

		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);

		mainFrame = ObjectRegistry.getInstance().getViewController().getTopFrame();

		Color borderColor = new Color(0, 0, 0, 150);
		loadIcon = new ImageIcon(getClass().getResource("/resources/img/map/loader.gif"));

		map = new Map(config);
		JPanel top = new JPanel();
		JPanel left = new JPanel();

		// map
		map.getMapKit().setBorder(BorderFactory.createMatteBorder(1, 2, 0, 0, borderColor));

		GridBagConstraints gridBagConstraints = GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0);
		gridBagConstraints.gridwidth = 2;
		add(top, gridBagConstraints);
		add(left, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		add(map.getMapKit(), GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

		// top components
		top.setLayout(new GridBagLayout());
		top.setBackground(new Color(245, 245, 245));
		top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

		goButton = new JButton();
		searchBox = new JComboBox();
		searchResult = new JLabel();
		searchResult.setPreferredSize(new Dimension(searchResult.getPreferredSize().width, loadIcon.getIconHeight()));

		searchBox.setEditable(true);
		searchBox.setPreferredSize(new Dimension(500, (int)searchBox.getPreferredSize().getHeight()));

		applyButton = new JButton();
		cancelButton = new JButton();
		applyButton.setFont(applyButton.getFont().deriveFont(Font.BOLD));
		applyButton.setEnabled(false);

		top.add(searchBox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 10, 10, 0, 5));
		top.add(goButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 10, 5, 0, 10));
		top.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 5, 0, 0));
		top.add(applyButton, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.BOTH, 10, 0, 0, 5));
		top.add(cancelButton, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.BOTH, 10, 5, 0, 5));
		top.add(searchResult, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 2, 10, 2, 10));

		// left components
		left.setLayout(new GridBagLayout());
		left.setBackground(Color.WHITE);
		Border componentBorder = BorderFactory.createCompoundBorder(UIManager.getBorder("TitledBorder.border"), BorderFactory.createEmptyBorder(5, 5, 5, 5));		

		// BBox
		final JPanel bbox = new JPanel();
		bbox.setBorder(componentBorder);
		bbox.setLayout(new GridBagLayout());	

		bboxTitel = new JLabel();
		bboxTitel.setFont(bbox.getFont().deriveFont(Font.BOLD));
		bboxTitel.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/selection.png")));
		bboxTitel.setIconTextGap(5);

		final JPanel bboxFields = new JPanel();
		bboxFields.setLayout(new GridBagLayout());		

		minX = new JFormattedTextField(LAT_LON_FORMATTER);
		minY = new JFormattedTextField(LAT_LON_FORMATTER);
		maxX = new JFormattedTextField(LAT_LON_FORMATTER);
		maxY = new JFormattedTextField(LAT_LON_FORMATTER);

		minX.setBackground(Color.WHITE);
		minY.setBackground(Color.WHITE);
		maxX.setBackground(Color.WHITE);
		maxY.setBackground(Color.WHITE);

		Dimension dim = new Dimension(90, minX.getPreferredSize().height);		
		minX.setPreferredSize(dim);
		minY.setPreferredSize(dim);
		maxX.setPreferredSize(dim);
		maxY.setPreferredSize(dim);
		minX.setMinimumSize(dim);
		minY.setMinimumSize(dim);
		maxX.setMinimumSize(dim);
		maxY.setMinimumSize(dim);

		gridBagConstraints = GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 5, 2, 0, 2);
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;		
		bboxFields.add(maxY, gridBagConstraints);
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		bboxFields.add(minX, gridBagConstraints);
		gridBagConstraints.gridx = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		bboxFields.add(maxX, gridBagConstraints);
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridx = 0;	
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		bboxFields.add(minY, gridBagConstraints);

		// BBox buttons
		JPanel bboxButtons = new JPanel();
		bboxButtons.setLayout(new GridBagLayout());
		bboxButtons.setBackground(bbox.getBackground());

		showBBox = new JButton();
		clearBBox = new JButton();

		copyBBox = new JButton();
		ImageIcon copyIcon = new ImageIcon(getClass().getResource("/resources/img/common/bbox_copy.png")); 
		copyBBox.setIcon(copyIcon);
		copyBBox.setPreferredSize(new Dimension(copyIcon.getIconWidth() + 6, copyIcon.getIconHeight() + 6));
		copyBBox.setEnabled(false);

		pasteBBox = new JButton();
		ImageIcon pasteIcon = new ImageIcon(getClass().getResource("/resources/img/common/bbox_paste.png")); 
		pasteBBox.setIcon(pasteIcon);
		pasteBBox.setPreferredSize(new Dimension(copyIcon.getIconWidth() + 6, copyIcon.getIconHeight() + 6));
		pasteBBox.setEnabled(clipboardHandler.containsPossibleBoundingBox());

		bboxButtons.add(showBBox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		bboxButtons.add(clearBBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

		Box bboxTitelBox = Box.createHorizontalBox();
		bboxTitelBox.add(bboxTitel);
		bboxTitelBox.add(Box.createHorizontalGlue());
		bboxTitelBox.add(copyBBox);
		bboxTitelBox.add(Box.createHorizontalStrut(5));
		bboxTitelBox.add(pasteBBox);

		bbox.add(bboxTitelBox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		bbox.add(bboxFields, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 5, 0));
		bbox.add(bboxButtons, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));

		// Reverse geocoder
		JPanel reverse = new JPanel();
		reverse.setBorder(componentBorder);
		reverse.setLayout(new GridBagLayout());

		reverseTitle = new JLabel();
		reverseTitle.setFont(reverseTitle.getFont().deriveFont(Font.BOLD));
		reverseTitle.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/waypoint_small.png")));

		reverseTitle.setIconTextGap(5);
		reverseSearchProgress = new JLabel();
		reverseInfo = new JLabel();

		reverseText = new JTextPane();
		reverseText.setEditable(false);
		reverseText.setBorder(minX.getBorder());
		reverseText.setBackground(Color.WHITE);
		reverseText.setContentType("text/html");
		((HTMLDocument)reverseText.getDocument()).getStyleSheet().addRule(
				"body { font-family: " + reverseText.getFont().getFamily() + "; " + "font-size: " + reverseText.getFont().getSize() + "pt; }");
		reverseText.setVisible(false);

		Box reverseTitelBox = Box.createHorizontalBox();
		reverseTitelBox.add(reverseTitle);
		reverseTitelBox.add(Box.createHorizontalGlue());
		reverseTitelBox.add(reverseSearchProgress);

		reverse.add(reverseTitelBox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		reverse.add(reverseText, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));
		reverse.add(reverseInfo, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));

		// Google maps
		JPanel googleMaps = new JPanel();
		googleMaps.setBorder(componentBorder);
		googleMaps.setLayout(new GridBagLayout());

		googleMapsTitle = new JLabel();
		googleMapsTitle.setFont(googleMapsTitle.getFont().deriveFont(Font.BOLD));
		googleMapsTitle.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/google_maps.png")));

		googleMapsButton = new JButton();
		googleMapsButton.setEnabled(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE));

		googleMaps.add(googleMapsTitle, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		googleMaps.add(googleMapsButton, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

		// help
		JPanel help = new JPanel();
		help.setBorder(componentBorder);
		help.setLayout(new GridBagLayout());	

		helpTitle = new JLabel();
		helpTitle.setFont(help.getFont().deriveFont(Font.BOLD));
		helpTitle.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/help.png")));
		helpTitle.setIconTextGap(5);		
		helpText = new JLabel();

		help.add(helpTitle, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		help.add(helpText, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));

		left.add(bbox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(reverse, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(googleMaps, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(help, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0, 4, 0, 1, GridBagConstraints.VERTICAL, 5, 0, 2, 0));

		left.setMinimumSize(left.getPreferredSize());
		left.setPreferredSize(left.getMinimumSize());

		// actions
		goButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (searchBox.getSelectedItem() != null)
					geocode(searchBox.getSelectedItem().toString());
			}
		});

		searchBox.getEditor().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geocode(e.getActionCommand());
			}
		});

		searchBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (updateSearchBox && !"comboBoxEdited".equals(e.getActionCommand())) {
					Object selectedItem = searchBox.getSelectedItem();
					if (selectedItem instanceof Location) {
						Location location = (Location)selectedItem;
						map.getMapKit().getMainMap().setZoom(1);

						HashSet<GeoPosition> viewPort = new HashSet<GeoPosition>(2);
						viewPort.add(location.getViewPort().getSouthWest());
						viewPort.add(location.getViewPort().getNorthEast());
						map.getMapKit().getMainMap().calculateZoomFrom(viewPort);

						WaypointType type = location.getLocationType() == LocationType.ROOFTOP ? 
								WaypointType.PRECISE : WaypointType.APPROXIMATE;
						map.getWaypointPainter().showWaypoints(new DefaultWaypoint(location.getPosition(), type));
					}
				}
			}
		});

		clearBBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearBoundingBox();
			}
		});

		KeyAdapter showBBoxAdapter = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					showBoundingBox();
			}
		};

		minX.addKeyListener(showBBoxAdapter);
		minY.addKeyListener(showBBoxAdapter);
		maxX.addKeyListener(showBBoxAdapter);
		maxY.addKeyListener(showBBoxAdapter);

		showBBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showBoundingBox();
			}
		});

		copyBBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyBoundingBoxToClipboard();
			}
		});

		pasteBBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pasteBoundingBoxFromClipboard();
			}
		});		

		PopupMenuDecorator popupMenuDecorator = PopupMenuDecorator.getInstance();
		popupMenuDecorator.decorate((JComponent)searchBox.getEditor().getEditorComponent(), reverseText);

		// popup menu
		final JPopupMenu popupMenu = new JPopupMenu();
		bboxPopups = new BBoxPopupMenu[5];

		bboxPopups[0] = new BBoxPopupMenu(popupMenuDecorator.decorateAndGet(minX), true);
		bboxPopups[1] = new BBoxPopupMenu(popupMenuDecorator.decorateAndGet(minY), true);
		bboxPopups[2] = new BBoxPopupMenu(popupMenuDecorator.decorateAndGet(maxX), true);
		bboxPopups[3] = new BBoxPopupMenu(popupMenuDecorator.decorateAndGet(maxY), true);
		bboxPopups[4] = new BBoxPopupMenu(popupMenu, false);

		Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new FlavorListener() {
			public void flavorsChanged(FlavorEvent e) {
				boolean enable = clipboardHandler.containsPossibleBoundingBox();

				pasteBBox.setEnabled(enable);
				for (int i = 0; i < bboxPopups.length; ++i)
					bboxPopups[i].paste.setEnabled(enable);
			}
		});

		PropertyChangeListener commitListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("value")) {
					try {
						minX.commitEdit();
						minY.commitEdit();
						maxX.commitEdit();
						maxY.commitEdit();

						setEnabledApplyBoundingBox(true);
					} catch (ParseException e1) {
						//
					}
				}
			}
		};

		minX.addPropertyChangeListener(commitListener);
		minY.addPropertyChangeListener(commitListener);
		maxX.addPropertyChangeListener(commitListener);
		maxY.addPropertyChangeListener(commitListener);

		bbox.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				showPopupMenu(e);
			}

			public void mouseReleased(MouseEvent e) {
				showPopupMenu(e);
			}

			private void showPopupMenu(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
					popupMenu.setInvoker(bbox);
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				// clear map cache
				((AbstractTileFactory)map.getMapKit().getMainMap().getTileFactory()).clearTileCache();
				((AbstractTileFactory)map.getMapKit().getMainMap().getTileFactory()).shutdownTileServicePool();
				((AbstractTileFactory)map.getMapKit().getMiniMap().getTileFactory()).clearTileCache();
				((AbstractTileFactory)map.getMapKit().getMiniMap().getTileFactory()).shutdownTileServicePool();

				WindowSize size = config.getGui().getMapWindow().getSize();
				Rectangle rect = MapWindow.this.getBounds();
				size.setX(rect.x);
				size.setY(rect.y);
				size.setWidth(rect.width);
				size.setHeight(rect.height);
			}
		});

		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double xmin = ((Number)minX.getValue()).doubleValue();
				double xmax = ((Number)maxX.getValue()).doubleValue();
				double ymin = ((Number)minY.getValue()).doubleValue();
				double ymax = ((Number)maxY.getValue()).doubleValue();

				final BoundingBox bbox = new BoundingBox();
				bbox.getLowerLeftCorner().setX(Math.min(xmin, xmax));
				bbox.getLowerLeftCorner().setY(Math.min(ymin, ymax));
				bbox.getUpperRightCorner().setX(Math.max(xmin, xmax));
				bbox.getUpperRightCorner().setY(Math.max(ymin, ymax));		

				DatabaseSrs wgs84 = null;
				for (DatabaseSrs srs : config.getProject().getDatabase().getReferenceSystems()) {
					if (srs.getSrid() == Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {
						wgs84 = srs;
						break;
					}
				}

				bbox.setSrs(wgs84);

				Thread t = new Thread() {
					public void run() {
						listener.setBoundingBox(bbox);
					}
				};
				t.setDaemon(true);
				t.start();

				copyBoundingBoxToClipboard();
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		googleMapsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Rectangle view = map.getMapKit().getMainMap().getViewportBounds();
				TileFactory fac = map.getMapKit().getMainMap().getTileFactory();
				int zoom = map.getMapKit().getMainMap().getZoom();

				GeoPosition centerPoint = fac.pixelToGeo(new Point2D.Double(view.getCenterX(), view.getCenterY()), zoom);			
				GeoPosition southWest = fac.pixelToGeo(new Point2D.Double(view.getMinX(), view.getMaxY()), zoom);
				GeoPosition northEast = fac.pixelToGeo(new Point2D.Double(view.getMaxX(), view.getMinY()), zoom);

				final StringBuilder url = new StringBuilder();
				url.append("http://maps.google.de/maps?");

				if (searchBox.getSelectedItem() instanceof Location) {
					String query = ((Location)searchBox.getSelectedItem()).getFormattedAddress();
					
					try {
						url.append("&q=").append(URLEncoder.encode(query, "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						// 
					}
				}
				
				url.append("&ll=").append(centerPoint.getLatitude()).append(",").append(centerPoint.getLongitude());
				url.append("&spn=").append((northEast.getLatitude() - southWest.getLatitude()) / 2).append(",").append((northEast.getLongitude() - southWest.getLongitude()) / 2);
				url.append("&sspn=").append(northEast.getLatitude() - southWest.getLatitude()).append(",").append(northEast.getLongitude() - southWest.getLongitude());
				url.append("&t=m");

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							Desktop.getDesktop().browse(new URI(url.toString()));
						} catch (IOException e1) {
							LOG.error("Failed to launch default browser.");
						} catch (URISyntaxException e1) {
							// 
						}
					}
				});
			}
		});
	}

	public void setBoundingBox(final BoundingBox bbox) {
		new SwingWorker<ValidationResult, Void>() {
			protected ValidationResult doInBackground() throws Exception {
				return validator.validate(bbox);
			}

			protected void done() {
				try {
					switch (get()) {
					case CANCEL:
						dispose();
						break;
					case INVALID:
						clearBoundingBox();
						break;
					default:
						minX.setValue(bbox.getLowerLeftCorner().getX());
						minY.setValue(bbox.getLowerLeftCorner().getY());
						maxX.setValue(bbox.getUpperRightCorner().getX());
						maxY.setValue(bbox.getUpperRightCorner().getY());
						showBoundingBox();
					}
				} catch (InterruptedException e) {
					//
				} catch (ExecutionException e) {
					//
				}
			}
		}.execute();
	}

	private void copyBoundingBoxToClipboard() {
		try {
			minX.commitEdit();
			minY.commitEdit();
			maxX.commitEdit();
			maxY.commitEdit();

			BoundingBox bbox = new BoundingBox();
			bbox.getLowerLeftCorner().setX(minX.isEditValid() && minX.getValue() != null ? ((Number)minX.getValue()).doubleValue() : null);
			bbox.getLowerLeftCorner().setY(minY.isEditValid() && minY.getValue() != null ? ((Number)minY.getValue()).doubleValue() : null);
			bbox.getUpperRightCorner().setX(maxX.isEditValid() && maxX.getValue() != null ? ((Number)maxX.getValue()).doubleValue() : null);
			bbox.getUpperRightCorner().setY(maxY.isEditValid() && maxY.getValue() != null ? ((Number)maxY.getValue()).doubleValue() : null);

			for (DatabaseSrs srs : config.getProject().getDatabase().getReferenceSystems()) {
				if (srs.getSrid() == Database.PREDEFINED_SRS.get(Database.PredefinedSrsName.WGS84_2D).getSrid()) {
					bbox.setSrs(srs);
					break;
				}
			}

			clipboardHandler.putBoundingBox(bbox);			
		} catch (ParseException e) {
			//
		}
	}

	private void pasteBoundingBoxFromClipboard() {
		setBoundingBox(clipboardHandler.getBoundingBox());
	}

	private void showBoundingBox() {
		try {
			minX.commitEdit();
			minY.commitEdit();
			maxX.commitEdit();
			maxY.commitEdit();

			GeoPosition southWest = new GeoPosition(((Number)minY.getValue()).doubleValue(), ((Number)minX.getValue()).doubleValue());
			GeoPosition northEast = new GeoPosition(((Number)maxY.getValue()).doubleValue(), ((Number)maxX.getValue()).doubleValue());

			map.getSelectionPainter().setSelectedArea(southWest, northEast);
			HashSet<GeoPosition> positions = new HashSet<GeoPosition>();
			positions.add(southWest);
			positions.add(northEast);
			map.getMapKit().setZoom(1);
			map.getMapKit().getMainMap().calculateZoomFrom(positions);
		} catch (ParseException e1) {
			//
		}
	}

	private void clearBoundingBox() {
		map.getSelectionPainter().clearSelectedArea();
		minX.setText("");
		maxX.setText("");
		minY.setText("");
		maxY.setText("");

		setEnabledApplyBoundingBox(false);
	}

	private void setEnabledApplyBoundingBox(boolean enable) {
		applyButton.setEnabled(enable);
		copyBBox.setEnabled(enable);		
		for (int i = 0; i < bboxPopups.length; ++i)
			bboxPopups[i].copy.setEnabled(enable);
	}

	private void geocode(final String searchString) {
		searchResult.setIcon(loadIcon);
		searchResult.setText("");
		searchResult.repaint();

		final long time = System.currentTimeMillis();
		new SwingWorker<GeocoderResponse, Void>() {
			protected GeocoderResponse doInBackground() throws Exception {
				GeocoderResponse response = Geocoder.parseLatLon(searchString);
				if (response == null)
					response = Geocoder.geocode(searchString);

				return response;
			}

			protected void done() {
				try {
					GeocoderResponse response = get();
					String resultMsg;
					if (response.getStatus() == StatusCode.OK) {
						searchBox.removeAllItems();
						for (Location tmp : response.getLocations())
							searchBox.addItem(tmp);

						searchBox.setSelectedItem(response.getLocations()[0]);

						if (response.getType() == ResponseType.LAT_LON)
							resultMsg = Internal.I18N.getString("map.geocoder.search.latLon");
						else {
							String text = Internal.I18N.getString("map.geocoder.search.result");
							Object[] args = new Object[]{ response.getLocations().length };
							resultMsg = MessageFormat.format(text, args);
						}
					} else if (response.getStatus() == StatusCode.ZERO_RESULTS) {
						String text = Internal.I18N.getString("map.geocoder.search.result");
						Object[] args = new Object[]{ 0 };
						resultMsg = MessageFormat.format(text, args);
					} else {
						switch (response.getStatus()) {
						case OVER_QUERY_LIMIT:
							resultMsg = Internal.I18N.getString("map.geocoder.search.overLimit");
							break;		
						case REQUEST_DENIED:
							resultMsg = Internal.I18N.getString("map.geocoder.search.denied");
							break;		
						default:
							LOG.error("Fatal service response from geocoder: " + response.getException().getMessage());
							resultMsg = Internal.I18N.getString("map.geocoder.search.fatal");
						}					
					}

					resultMsg += " (" + ((System.currentTimeMillis() - time) / 1000.0) + " " + Internal.I18N.getString("map.geocoder.search.sec") + ")";
					searchResult.setText(resultMsg);
					searchResult.setIcon(null);
				} catch (InterruptedException e) {
					//
				} catch (ExecutionException e) {
					//
				}
			}
		}.execute();
	}

	private void setSizeOnScreen() {
		WindowSize size = config.getGui().getMapWindow().getSize();

		Integer x = size.getX();
		Integer y = size.getY();
		Integer width = size.getWidth();
		Integer height = size.getHeight();

		// create default values for main window
		if (x == null || y == null || width == null || height == null) {
			x = mainFrame.getLocation().x + 10;
			y = mainFrame.getLocation().y + 10;
			width = 1024;
			height = 768;

			Toolkit t = Toolkit.getDefaultToolkit();
			Insets frame_insets = t.getScreenInsets(mainFrame.getGraphicsConfiguration());
			int frame_insets_x = frame_insets.left + frame_insets.right;
			int frame_insets_y = frame_insets.bottom + frame_insets.top;

			Rectangle bounds = mainFrame.getGraphicsConfiguration().getBounds();

			if (!bounds.contains(x, y, width + frame_insets_x, height + frame_insets_y)) {
				// check width
				if (x + width + frame_insets_x > bounds.width || y + height + frame_insets_y > bounds.height) {
					x = frame_insets.left;
					y = frame_insets.top;

					if (width + frame_insets_x > bounds.width)
						width = bounds.width - frame_insets_x;

					if (height + frame_insets_y > bounds.height)
						height = bounds.height - frame_insets_y;
				}
			}
		}

		setLocation(x, y);
		setSize(new Dimension(width, height));
	}

	private void doTranslation() {
		setTitle(Internal.I18N.getString("map.window.title"));
		applyButton.setText(Internal.I18N.getString("common.button.apply"));
		cancelButton.setText(Internal.I18N.getString("common.button.cancel"));
		goButton.setText(Internal.I18N.getString("map.button.go"));
		bboxTitel.setText(Internal.I18N.getString("map.boundingBox.label"));
		showBBox.setText(Internal.I18N.getString("map.boundingBox.show.button"));
		showBBox.setToolTipText(Internal.I18N.getString("map.boundingBox.show.tooltip"));
		clearBBox.setText(Internal.I18N.getString("map.boundingBox.clear.button"));
		clearBBox.setToolTipText(Internal.I18N.getString("map.boundingBox.clear.tooltip"));
		copyBBox.setToolTipText(Internal.I18N.getString("common.tooltip.boundingBox.copy"));
		pasteBBox.setToolTipText(Internal.I18N.getString("common.tooltip.boundingBox.paste"));
		reverseTitle.setText(Internal.I18N.getString("map.reverseGeocoder.label"));
		reverseInfo.setText("<html>" + Internal.I18N.getString("map.reverseGeocoder.hint.label") + "</html>");
		helpTitle.setText(Internal.I18N.getString("map.help.label"));
		helpText.setText("<html>" + Internal.I18N.getString("map.help.hint") + "</html>");
		googleMapsButton.setText(Internal.I18N.getString("map.google.label"));

		map.doTranslation();		
		for (int i = 0; i < bboxPopups.length; ++i)
			bboxPopups[i].doTranslation();
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getEventType() == GlobalEvents.SWITCH_LOCALE) {
			doTranslation();
		}

		else if (event.getEventType() == MapEvents.BOUNDING_BOX_SELECTION) {
			BoundingBoxSelectionEvent e = (BoundingBoxSelectionEvent)event;
			final GeoPosition[] bbox = e.getBoundingBox();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					minX.setValue(bbox[0].getLatitude());
					minY.setValue(bbox[0].getLongitude());
					maxX.setValue(bbox[1].getLatitude());
					maxY.setValue(bbox[1].getLongitude());				
				}
			});
		}

		else if (event.getEventType() == MapEvents.MAP_BOUNDS) {
			MapBoundsSelectionEvent e = (MapBoundsSelectionEvent)event;
			final GeoPosition[] bbox = e.getBoundingBox();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					minX.setValue(bbox[0].getLongitude());
					minY.setValue(bbox[0].getLatitude());
					maxX.setValue(bbox[1].getLongitude());
					maxY.setValue(bbox[1].getLatitude());
					map.getSelectionPainter().setSelectedArea(bbox[0], bbox[1]);				
				}
			});	
		}

		else if (event.getEventType() == MapEvents.REVERSE_GEOCODER) {
			ReverseGeocoderEvent e = (ReverseGeocoderEvent)event;

			if (e.getStatus() == ReverseGeocoderStatus.SEARCHING) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reverseSearchProgress.setIcon(loadIcon);						
					}
				});

			} else if (e.getStatus() == ReverseGeocoderStatus.RESULT) {
				final Location location = e.getLocation();
				final StringBuilder result = new StringBuilder();

				String[] tokens = location.getFormattedAddress().split(", ");
				for (int i = 0; i < tokens.length; ++i) {
					if (i == 0) 
						result.append("<b>").append(tokens[i]).append("</b>");
					else
						result.append(tokens[i]);

					if (i < tokens.length - 1)
						result.append("<br>");
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reverseText.setText(result.toString());
						reverseText.setVisible(true);
						reverseInfo.setVisible(false);
						reverseSearchProgress.setIcon(null);

						location.setFormattedAddress(location.getPosition().getLatitude() + ", " + location.getPosition().getLongitude());
						updateSearchBox = false;
						searchBox.setSelectedItem(location);
						updateSearchBox = true;
					}
				});

			} else if (e.getStatus() == ReverseGeocoderStatus.ERROR) {
				GeocoderResponse response = e.getResponse();
				final String info;

				switch (response.getStatus()) {
				case ZERO_RESULTS:
					info = Internal.I18N.getString("map.reverseGeocoder.search.noResult");
					break;
				case OVER_QUERY_LIMIT:
					info = Internal.I18N.getString("map.geocoder.search.overLimit");
					break;		
				case REQUEST_DENIED:
					info = Internal.I18N.getString("map.geocoder.search.denied");
					break;		
				default:
					LOG.error("Fatal service response from reverse geocoder: " + response.getException().getMessage());
					info = Internal.I18N.getString("map.geocoder.search.fatal");
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reverseInfo.setText(info);
						reverseText.setVisible(false);
						reverseInfo.setVisible(true);
						reverseSearchProgress.setIcon(null);
					}
				});

			}
		}
	}

	private final class BBoxPopupMenu extends JPopupMenu {
		private JMenuItem copy;	
		private JMenuItem paste;

		public BBoxPopupMenu(JPopupMenu popupMenu, boolean addSeparator) {
			copy = new JMenuItem();	
			paste = new JMenuItem();

			copy.setEnabled(false);
			paste.setEnabled(clipboardHandler.containsPossibleBoundingBox());

			if (addSeparator) popupMenu.addSeparator();
			popupMenu.add(copy);
			popupMenu.add(paste);

			copy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					copyBoundingBoxToClipboard();
				}
			});

			paste.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pasteBoundingBoxFromClipboard();
				}
			});
		}

		private void doTranslation() {
			copy.setText(Internal.I18N.getString("common.popup.boundingBox.copy"));
			paste.setText(Internal.I18N.getString("common.popup.boundingBox.paste"));
		}
	}

}
