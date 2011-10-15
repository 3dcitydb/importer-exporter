package de.tub.citydb.gui.components.mapviewer;

import java.awt.Color;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;

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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

import org.jdesktop.swingx.mapviewer.AbstractTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.HttpProxySettings;

import de.tub.citydb.api.config.BoundingBox;
import de.tub.citydb.api.config.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
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
import de.tub.citydb.gui.components.mapviewer.map.event.BoundingBoxSelection;
import de.tub.citydb.gui.components.mapviewer.map.event.MapBoundsSelection;
import de.tub.citydb.gui.components.mapviewer.map.event.MapEvents;
import de.tub.citydb.gui.components.mapviewer.map.event.ReverseGeocoderEvent;
import de.tub.citydb.gui.components.mapviewer.map.event.ReverseGeocoderEvent.ReverseGeocoderStatus;
import de.tub.citydb.gui.components.mapviewer.validation.BoundingBoxValidator;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class MapWindow extends JDialog implements EventHandler {
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
	private JTextField reverseInfo;
	private JTextPane reverseText;
	private JLabel reverseSearchProgress;

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

	public static final MapWindow getInstance(BoundingBoxListener listener, Config config) {
		if (instance == null)
			instance = new MapWindow(config);

		instance.listener = listener;
		instance.updateHttpProxySettings();

		if (!instance.isVisible())
			instance.setSizeOnScreen();

		return instance;
	}

	private void init() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(Internal.I18N.getString("map.window.title"));
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

		// BBox
		final JPanel bbox = new JPanel();
		bbox.setBorder(BorderFactory.createTitledBorder(""));
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
		reverse.setBorder(BorderFactory.createTitledBorder(""));
		reverse.setLayout(new GridBagLayout());

		reverseTitle = new JLabel();
		reverseTitle.setFont(reverseTitle.getFont().deriveFont(Font.BOLD));
		reverseTitle.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/waypoint_small.png")));

		reverseTitle.setIconTextGap(5);
		reverseSearchProgress = new JLabel();

		reverseText = new JTextPane();
		reverseText.setEditable(false);
		reverseText.setBorder(minX.getBorder());
		reverseText.setBackground(Color.WHITE);
		reverseText.setContentType("text/html");
		((HTMLDocument)reverseText.getDocument()).getStyleSheet().addRule(
				"body { font-family: " + reverseText.getFont().getFamily() + "; " + "font-size: " + reverseText.getFont().getSize() + "pt; }");
		reverseText.setVisible(false);

		reverseInfo = new JTextField();
		reverseInfo.setBorder(BorderFactory.createEmptyBorder());
		reverseInfo.setOpaque(false);
		reverseInfo.setEditable(false);

		Box reverseTitelBox = Box.createHorizontalBox();
		reverseTitelBox.add(reverseTitle);
		reverseTitelBox.add(Box.createHorizontalGlue());
		reverseTitelBox.add(reverseSearchProgress);

		reverse.add(reverseTitelBox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		reverse.add(reverseText, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));
		reverse.add(reverseInfo, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));

		left.add(bbox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(reverse, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0, 2, 0, 1, GridBagConstraints.VERTICAL, 5, 0, 2, 0));

		left.setMinimumSize(left.getPreferredSize());

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
				if (!"comboBoxEdited".equals(e.getActionCommand())) {
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
		popupMenuDecorator.decorate((JComponent)searchBox.getEditor().getEditorComponent(), reverseText, reverseInfo);

		// popup menu
		final JPopupMenu popupMenu = new JPopupMenu();
		bboxPopups = new BBoxPopupMenu[5];

		bboxPopups[0] = new BBoxPopupMenu(popupMenuDecorator.decorate(minX), true);
		bboxPopups[1] = new BBoxPopupMenu(popupMenuDecorator.decorate(minY), true);
		bboxPopups[2] = new BBoxPopupMenu(popupMenuDecorator.decorate(maxX), true);
		bboxPopups[3] = new BBoxPopupMenu(popupMenuDecorator.decorate(maxY), true);
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
				final BoundingBox bbox = new BoundingBox();
				bbox.getLowerLeftCorner().setX(((Number)minX.getValue()).doubleValue());
				bbox.getLowerLeftCorner().setY(((Number)minY.getValue()).doubleValue());
				bbox.getUpperRightCorner().setX(((Number)maxX.getValue()).doubleValue());
				bbox.getUpperRightCorner().setY(((Number)maxY.getValue()).doubleValue());		

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
	}

	public void setBoundingBox(final BoundingBox bbox) {
		Thread t = new Thread() {
			public void run() {
				if (bbox != null) {
					switch (validator.isValid(bbox)) {
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
				}
			}
		};
		t.setDaemon(true);
		t.start();
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

		Thread t = new Thread() {
			public void run() {
				long time = System.currentTimeMillis();
				GeocoderResponse response = Geocoder.parseLatLon(searchString);
				if (response == null)
					response = Geocoder.geocode(searchString, config.getProject().getGlobal().getHttpProxy());

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
					resultMsg = Internal.I18N.getString("map.geocoder.search.fatal") + ": " + response.getStatus();
				}

				resultMsg += " (" + ((System.currentTimeMillis() - time) / 1000.0) + " " + Internal.I18N.getString("map.geocoder.search.sec") + ")";
				searchResult.setText(resultMsg);
				searchResult.setIcon(null);
			}
		};
		t.setDaemon(true);
		t.start();		
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

	private void updateHttpProxySettings() {
		HttpProxySettings proxy = new HttpProxySettings();;
		proxy.setProxy(config.getProject().getGlobal().getHttpProxy().getProxy());
		proxy.setCredentials(config.getProject().getGlobal().getHttpProxy().getBase64EncodedCredentials());

		map.getMapKit().getMainMap().getTileFactory().getInfo().setHttpProxySettings(proxy);
		map.getMapKit().getMiniMap().getTileFactory().getInfo().setHttpProxySettings(proxy);
	}

	private void doTranslation() {
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
		reverseInfo.setText(Internal.I18N.getString("map.reverseGeocoder.hint.label"));
		
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
			BoundingBoxSelection e = (BoundingBoxSelection)event;
			GeoPosition[] bbox = e.getBoundingBox();

			minX.setValue(bbox[0].getLatitude());
			minY.setValue(bbox[0].getLongitude());
			maxX.setValue(bbox[1].getLatitude());
			maxY.setValue(bbox[1].getLongitude());
		}

		else if (event.getEventType() == MapEvents.MAP_BOUNDS) {
			MapBoundsSelection e = (MapBoundsSelection)event;
			GeoPosition[] bbox = e.getBoundingBox();

			minX.setValue(bbox[0].getLongitude());
			minY.setValue(bbox[0].getLatitude());
			maxX.setValue(bbox[1].getLongitude());
			maxY.setValue(bbox[1].getLatitude());
			map.getSelectionPainter().setSelectedArea(bbox[0], bbox[1]);
		}

		else if (event.getEventType() == MapEvents.REVERSE_GEOCODER) {
			ReverseGeocoderEvent e = (ReverseGeocoderEvent)event;

			if (e.getStatus() == ReverseGeocoderStatus.SEARCHING) {
				reverseSearchProgress.setIcon(loadIcon);
			} else if (e.getStatus() == ReverseGeocoderStatus.RESULT) {
				Location location = e.getLocation();
				StringBuilder rest = new StringBuilder();
				String[] tokens = location.getFormattedAddress().split(", ");
				for (int i = 0; i < tokens.length; ++i) {
					if (i == 0) 
						rest.append("<b>").append(tokens[i]).append("</b>");
					else
						rest.append(tokens[i]);

					if (i < tokens.length - 1)
						rest.append("<br>");
				}

				reverseText.setText(rest.toString());
				reverseInfo.setText(LAT_LON_FORMATTER.format(location.getPosition().getLatitude()) + ", " + 
						LAT_LON_FORMATTER.format(location.getPosition().getLongitude()));
				reverseText.setVisible(true);
				reverseInfo.setVisible(true);
				reverseSearchProgress.setIcon(null);
			} else if (e.getStatus() == ReverseGeocoderStatus.ERROR) {
				GeocoderResponse response = e.getResponse();
				reverseInfo.setText(response.getStatus().toString());
				reverseText.setVisible(false);
				reverseInfo.setVisible(true);
				reverseSearchProgress.setIcon(null);
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
