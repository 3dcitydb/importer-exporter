package de.tub.citydb.gui.components.mapviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import de.tub.citydb.gui.components.mapviewer.geocoder.Geocoder;
import de.tub.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import de.tub.citydb.gui.components.mapviewer.geocoder.Location;
import de.tub.citydb.gui.components.mapviewer.geocoder.LocationType;
import de.tub.citydb.gui.components.mapviewer.geocoder.ResponseType;
import de.tub.citydb.gui.components.mapviewer.geocoder.StatusCode;
import de.tub.citydb.gui.components.mapviewer.map.BBoxSelectionListener;
import de.tub.citydb.gui.components.mapviewer.map.DefaultWaypoint;
import de.tub.citydb.gui.components.mapviewer.map.DefaultWaypoint.WaypointType;
import de.tub.citydb.gui.components.mapviewer.map.Map;
import de.tub.citydb.gui.components.mapviewer.map.MapBoundsListener;
import de.tub.citydb.gui.components.mapviewer.map.ReverseGeocoderListener;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.util.gui.BBoxClipboardHandler;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class MapWindow extends JFrame {
	private static MapWindow mapWindow = null;
	public static DecimalFormat LAT_LON_FORMATTER = new DecimalFormat("##0.000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

	private Map map;	
	private JComboBox searchBox;
	private JLabel searchResult;
	private ImageIcon loadIcon;

	private JFormattedTextField minX;
	private JFormattedTextField minY;
	private JFormattedTextField maxX;
	private JFormattedTextField maxY;

	private MapWindow() {
		// just to thwart instantiation
	}
	
	public static final MapWindow getInstance() {
		if (mapWindow == null) {
			mapWindow = new MapWindow();
			mapWindow.init();
		}
		
		return mapWindow;
	}

	private void init() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("3DCityDB OSM Prototype");
		setLayout(new GridBagLayout());
		getContentPane().setBackground(Color.WHITE);

		Color borderColor = new Color(0, 0, 0, 150);
		loadIcon = new ImageIcon(getClass().getResource("/resources/img/map/loader.gif"));

		map = new Map();
		JPanel top = new JPanel();
		JPanel left = new JPanel();
		left.setMaximumSize(new Dimension(200, 1));
		left.setPreferredSize(left.getMaximumSize());

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

		JButton searchButton = new JButton("Go");
		searchBox = new JComboBox();
		searchResult = new JLabel();
		searchResult.setPreferredSize(new Dimension(searchResult.getPreferredSize().width, loadIcon.getIconHeight()));

		searchBox.setEditable(true);
		Font font = new Font(searchBox.getFont().getName(), searchBox.getFont().getStyle(), searchBox.getFont().getSize() + 4);
		searchBox.setFont(font);
		searchBox.setPreferredSize(new Dimension(500, (int)searchBox.getPreferredSize().getHeight()));
		searchButton.setFont(font);

		JButton okButton = new JButton("Ok");
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setFont(font);
		okButton.setFont(font.deriveFont(Font.BOLD));
		
		top.add(searchBox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 10, 10, 0, 5));
		top.add(searchButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 10, 5, 0, 10));
		top.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 5, 0, 0));
		top.add(okButton, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.BOTH, 10, 0, 0, 5));
		top.add(cancelButton, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.BOTH, 10, 5, 0, 5));
		top.add(searchResult, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 2, 10, 2, 10));

		// left components
		left.setLayout(new GridBagLayout());
		left.setBackground(Color.WHITE);

		// BBox
		final JPanel bbox = new JPanel();
		bbox.setBorder(BorderFactory.createTitledBorder(""));
		bbox.setLayout(new GridBagLayout());	

		JLabel bboxTitel = new JLabel("Bounding Box");
		bboxTitel.setFont(bbox.getFont().deriveFont(Font.BOLD));
		bboxTitel.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/selection.png")));
		bboxTitel.setIconTextGap(5);

		final JPanel bboxFields = new JPanel();
		bboxFields.setLayout(new GridBagLayout());
		DecimalFormat f = new DecimalFormat("###.######", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		f.setMaximumIntegerDigits(3);
		f.setMinimumIntegerDigits(1);
		f.setMinimumFractionDigits(2);
		f.setMaximumFractionDigits(6);

		minX = new JFormattedTextField(f);
		minY = new JFormattedTextField(f);
		maxX = new JFormattedTextField(f);
		maxY = new JFormattedTextField(f);

		minX.setFocusLostBehavior(JFormattedTextField.COMMIT);
		minY.setFocusLostBehavior(JFormattedTextField.COMMIT);
		maxX.setFocusLostBehavior(JFormattedTextField.COMMIT);
		maxY.setFocusLostBehavior(JFormattedTextField.COMMIT);

		minX.setBackground(Color.WHITE);
		minY.setBackground(Color.WHITE);
		maxX.setBackground(Color.WHITE);
		maxY.setBackground(Color.WHITE);

		Dimension dim = new Dimension(85, minX.getPreferredSize().height);		
		minX.setPreferredSize(dim);
		minY.setPreferredSize(dim);
		maxX.setPreferredSize(dim);
		maxY.setPreferredSize(dim);

		minX.setValue(0);
		minY.setValue(0);
		maxX.setValue(0);
		maxY.setValue(0);

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
		
		JButton showBBox = new JButton("Show");
		showBBox.setToolTipText("Show bounding box in map");
		
		JButton copyBBox = new JButton();
		ImageIcon copyIcon = new ImageIcon(getClass().getResource("/resources/img/common/bbox_copy.png")); 
		copyBBox.setIcon(copyIcon);
		copyBBox.setPreferredSize(new Dimension(copyIcon.getIconWidth() + 6, copyIcon.getIconHeight() + 6));
		copyBBox.setToolTipText("Copy bounding box to clipboard");

		JButton pasteBBox = new JButton();
		ImageIcon pasteIcon = new ImageIcon(getClass().getResource("/resources/img/common/bbox_paste.png")); 
		pasteBBox.setIcon(pasteIcon);
		pasteBBox.setPreferredSize(new Dimension(copyIcon.getIconWidth() + 6, copyIcon.getIconHeight() + 6));
		pasteBBox.setToolTipText("Paste bounding box from clipboard");
				
		bboxButtons.add(showBBox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		bboxButtons.add(Box.createHorizontalGlue(), GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		bboxButtons.add(copyBBox, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
		bboxButtons.add(pasteBBox, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

		bbox.add(bboxTitel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		bbox.add(bboxFields, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 5, 0));
		bbox.add(bboxButtons, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));

		// Reverse geocoder
		JPanel reverse = new JPanel();
		reverse.setBorder(BorderFactory.createTitledBorder(""));
		reverse.setLayout(new GridBagLayout());

		JLabel reverseTitle = new JLabel("Address lookup");
		reverseTitle.setFont(reverseTitle.getFont().deriveFont(Font.BOLD));
		reverseTitle.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/waypoint_small.png")));

		reverseTitle.setIconTextGap(5);
		final JLabel reverseSearchProgress = new JLabel();

		final JTextPane reverseText = new JTextPane();
		reverseText.setEditable(false);
		reverseText.setBorder(minX.getBorder());
		reverseText.setBackground(Color.WHITE);
		reverseText.setContentType("text/html");
		((HTMLDocument)reverseText.getDocument()).getStyleSheet().addRule(
				"body { font-family: " + reverseText.getFont().getFamily() + "; " + "font-size: " + reverseText.getFont().getSize() + "pt; }");
		reverseText.setVisible(false);

		final JTextField reverseInfo = new JTextField("Use popup menu for queries.");
		reverseInfo.setBorder(BorderFactory.createEmptyBorder());
		reverseInfo.setOpaque(false);
		reverseInfo.setEditable(false);

		Box box = Box.createHorizontalBox();
		box.add(reverseTitle);
		box.add(Box.createHorizontalGlue());
		box.add(reverseSearchProgress);

		reverse.add(box, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 2, 0));
		reverse.add(reverseText, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));
		reverse.add(reverseInfo, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));

		left.add(bbox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(reverse, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 5, 0));		
		left.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0, 2, 0, 1, GridBagConstraints.VERTICAL, 5, 0, 2, 0));

		// actions
		searchButton.addActionListener(new ActionListener() {
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

		map.addBBoxSelectionListener(new BBoxSelectionListener() {
			public void bboxSelected(final GeoPosition[] bbox) {	
				minX.setValue(bbox[0].getLatitude());
				minY.setValue(bbox[0].getLongitude());
				maxX.setValue(bbox[1].getLatitude());
				maxY.setValue(bbox[1].getLongitude());
			}
		});

		map.addReverseGeocoderListener(new ReverseGeocoderListener() {
			public void searching() {
				reverseSearchProgress.setIcon(loadIcon);
			}

			public void process(final Location location) {
				final StringBuilder rest = new StringBuilder();
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
			}

			public void error(final GeocoderResponse response) {
				reverseInfo.setText(response.getStatus().toString());
				reverseText.setVisible(false);
				reverseInfo.setVisible(true);
				reverseSearchProgress.setIcon(null);
			}
		});

		map.addMapBoundsListener(new MapBoundsListener() {
			public void getMapBounds(GeoPosition[] bbox) {
				minX.setValue(bbox[0].getLongitude());
				minY.setValue(bbox[0].getLatitude());
				maxX.setValue(bbox[1].getLongitude());
				maxY.setValue(bbox[1].getLatitude());
				map.getSelectionPainter().clearSelectedArea();
			}
		});

		PopupMenuDecorator popupMenuDecorator = PopupMenuDecorator.getInstance();
		popupMenuDecorator.decorate((JComponent)searchBox.getEditor().getEditorComponent(), reverseText, reverseInfo);

		createBoundingBoxPopupMenu(popupMenuDecorator.decorate(minX), true);
		createBoundingBoxPopupMenu(popupMenuDecorator.decorate(minY), true);
		createBoundingBoxPopupMenu(popupMenuDecorator.decorate(maxX), true);
		createBoundingBoxPopupMenu(popupMenuDecorator.decorate(maxY), true);
		
		// popup menu
		final JPopupMenu popupMenu = new JPopupMenu();
		createBoundingBoxPopupMenu(popupMenu, false);
		
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
		
		setSize(new Dimension(1024, 768));
	}
	
	private void createBoundingBoxPopupMenu(JPopupMenu popupMenu, boolean addSeparator) {
		JMenuItem copy = new JMenuItem("Copy bounding box");	
		JMenuItem paste = new JMenuItem("Paste bounding box");
		
		if (addSeparator)
			popupMenu.addSeparator();
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
	
	private void copyBoundingBoxToClipboard() {
		try {
			minX.commitEdit();
			minY.commitEdit();
			maxX.commitEdit();
			maxY.commitEdit();
			
			BBoxClipboardHandler.getInstance().putBoundingBox(
					((Number)minX.getValue()).doubleValue(),
					((Number)minY.getValue()).doubleValue(),
					((Number)maxX.getValue()).doubleValue(),
					((Number)maxY.getValue()).doubleValue());			
		} catch (ParseException e1) {
			//
		}
	}
	
	private void pasteBoundingBoxFromClipboard() {
		double[] bbox = BBoxClipboardHandler.getInstance().getBoundingBox();
		
		if (bbox != null && bbox.length == 4) {
			minX.setValue(bbox[0]);
			minY.setValue(bbox[1]);
			maxX.setValue(bbox[2]);
			maxY.setValue(bbox[3]);
		}
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

	private void geocode(final String searchString) {
		searchResult.setIcon(loadIcon);
		searchResult.setText("");
		searchResult.repaint();

		Thread t = new Thread() {
			public void run() {
				long time = System.currentTimeMillis();
				GeocoderResponse response = Geocoder.parseLatLon(searchString);
				if (response == null)
					response = Geocoder.geocode(searchString);

				String resultMsg;
				if (response.getStatus() == StatusCode.OK) {
					searchBox.removeAllItems();
					for (Location tmp : response.getLocations())
						searchBox.addItem(tmp);

					searchBox.setSelectedItem(response.getLocations()[0]);

					if (response.getType() == ResponseType.LAT_LON)
						resultMsg = "Map positioned to geographic coordinates";
					else {
						int num = response.getLocations().length;
						resultMsg = num + " match" + ((num == 1) ? " " : "es ") + "returned from geocoding";
					}
				} else if (response.getStatus() == StatusCode.ZERO_RESULTS) {
					resultMsg = "No match returned";
				} else {
					resultMsg = "Fatal service response: " + response.getStatus();
				}

				resultMsg += " (" + ((System.currentTimeMillis() - time) / 1000.0) + " seconds)";
				searchResult.setText(resultMsg);
				searchResult.setIcon(null);
			}
		};
		t.setDaemon(true);
		t.start();		
	}
}
