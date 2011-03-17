/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.ConfigUtil;
import de.tub.citydb.config.gui.GuiConfigUtil;
import de.tub.citydb.config.gui.window.MainWindow;
import de.tub.citydb.config.gui.window.WindowSize;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.ProjectConfigUtil;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.config.project.importer.ImportFilterConfig;
import de.tub.citydb.controller.Exporter;
import de.tub.citydb.controller.Importer;
import de.tub.citydb.controller.KmlExporter;
import de.tub.citydb.controller.XMLValidator;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.concurrent.InterruptEnum;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.gui.components.ExportStatusDialog;
import de.tub.citydb.gui.components.ImportStatusDialog;
import de.tub.citydb.gui.components.XMLValidationStatusDialog;
import de.tub.citydb.gui.menubar.MenuBar;
import de.tub.citydb.gui.panel.console.ConsoleWindow;
import de.tub.citydb.gui.panel.db.DatabasePanel;
import de.tub.citydb.gui.panel.exporter.ExportPanel;
import de.tub.citydb.gui.panel.importer.ImportPanel;
import de.tub.citydb.gui.panel.kmlExporter.KmlExportPanel;
import de.tub.citydb.gui.panel.matching.MatchingPanel;
import de.tub.citydb.gui.panel.settings.PrefPanel;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

@SuppressWarnings("serial")
public class ImpExpGui extends JFrame implements PropertyChangeListener {
	private final Logger LOG = Logger.getInstance();

	private final ReentrantLock mainLock = new ReentrantLock();
	private final Config config;
	private final JAXBContext jaxbCityGMLContext;
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final JAXBContext jaxbProjectContext;
	private final JAXBContext jaxbGuiContext;
	private final DBConnectionPool dbPool;

	private JPanel main;
	private JTextArea consoleText;
	private JLabel statusText;
	private JLabel connectText;
	private MenuBar menuBar;
	private JTabbedPane menu;
	private JSplitPane splitPane;
	private ImportPanel importPanel;
	private ExportPanel exportPanel;
	private KmlExportPanel kmlExportPanel;
	private DatabasePanel databasePanel;
	private PrefPanel prefPanel;
	private MatchingPanel matchingPanel;
	private JPanel console;
	private JLabel consoleLabel;
	private ConsoleWindow consoleWindow;
	private int tmpConsoleWidth;
	private int activePosition;

	private Importer importer;
	private Exporter exporter;
	private KmlExporter kmlExporter;
	private XMLValidator validator;

	private PrintStream out;
	private PrintStream err;

	// internal state
	private LanguageType currentLang = null;

	// set look & feel
	{
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public ImpExpGui(JAXBContext jaxbCityGMLContext,
			JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			JAXBContext jaxbProjectContext,
			JAXBContext jaxbGuiContext,
			DBConnectionPool dbPool,
			Config config) {
		this.jaxbCityGMLContext = jaxbCityGMLContext;
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.jaxbProjectContext = jaxbProjectContext;
		this.jaxbGuiContext = jaxbGuiContext;
		this.dbPool = dbPool;
		this.config = config;

		config.getInternal().addPropertyChangeListener(this);
	}

	public void invoke(List<String> errMsgs) {
		// init GUI elements
		initGui();
		doTranslation();
		loadSettings();
		showWindow();

		// initConsole;
		initConsole();

		if (!errMsgs.isEmpty()) {
			for (String msg : errMsgs)
				LOG.error(msg);
			LOG.info("Project settings initialized using default values.");
		}		
	}

	public void restoreDefaults() {
		if (consoleWindow.isVisible() != config.getGui().getConsoleWindow().isDetached())
			enableConsoleWindow(config.getGui().getConsoleWindow().isDetached(), false);

		consoleWindow.setSize(0, 0);
		showWindow();
	}

	private void initGui() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		activePosition = 0;
		main = new JPanel();

		menuBar = new MenuBar(config, jaxbProjectContext, this);
		setJMenuBar(menuBar);

		menu = new JTabbedPane();
		importPanel = new ImportPanel(config);
		exportPanel = new ExportPanel(config);
		kmlExportPanel = new KmlExportPanel(config);
		databasePanel = new DatabasePanel(config, this);
		prefPanel = new PrefPanel(config, this);
		matchingPanel = new MatchingPanel(config, this);

		menu.add(importPanel, "");
		menu.add(exportPanel, "");
		// javier
		menu.add(kmlExportPanel, "");
		menu.add(matchingPanel, BorderLayout.NORTH);
		menu.add(databasePanel, "");
		menu.add(prefPanel, "");

		console = new JPanel();
		consoleText = new JTextArea();
		consoleLabel = new JLabel();
		consoleText.setAutoscrolls(true);
		consoleText.setFont(new Font(Font.MONOSPACED, 0, 11));
		consoleText.setEditable(false);
		consoleWindow = new ConsoleWindow(console, config, this);

		statusText = new JLabel();
		connectText = new JLabel();

		Border border = BorderFactory.createEtchedBorder();
		Border margin = BorderFactory.createEmptyBorder(0, 2, 0, 2);
		statusText.setBorder(new CompoundBorder(border, margin));
		statusText.setOpaque(true);
		statusText.setBackground(new Color(255,255,255));		

		connectText.setBorder(new CompoundBorder(border, margin));
		connectText.setBackground(new Color(255,255,255));
		connectText.setOpaque(true);

		menu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (menu.getSelectedIndex() == activePosition) return;

				if (menu.getComponentAt(activePosition) == prefPanel) {
					if (!prefPanel.requestChange())
						menu.setSelectedIndex(activePosition);
				}

				activePosition = menu.getSelectedIndex();
			}
		});

		importPanel.getImportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						importButtonPressed();
						return null;
					}
				};
				worker.execute();
			}
		});

		importPanel.getValidateButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						validateButtonPressed();
						return null;
					}
				};
				worker.execute();
			}
		});

		exportPanel.getExportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						exportButtonPressed();
						return null;
					}
				};
				worker.execute();
			}
		});

		kmlExportPanel.getExportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						kmlExportButtonPressed();
						return null;
					}
				};
				worker.execute();
			}
		});

		addWindowListener(new WindowListener() {
			public void windowClosed(WindowEvent e) {
				shutdown();
			}

			public void windowActivated(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});

		//layout
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImpExpGui.class.getResource("/resources/img/logo_small.png")));
		setLayout(new GridBagLayout());

		// main panel
		main.setBorder(BorderFactory.createEmptyBorder());
		main.setBackground(this.getBackground());
		main.setLayout(new GridBagLayout());
		{
			main.add(menu, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			JPanel status = new JPanel();
			status.setBorder(BorderFactory.createEmptyBorder());
			status.setBackground(this.getBackground());
			main.add(status, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));
			status.setLayout(new GridBagLayout());
			{
				status.add(statusText, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,2));
				status.add(connectText, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,2,0,0));
			}
		}

		// console panel
		console.setBorder(BorderFactory.createEmptyBorder());
		console.setBackground(this.getBackground());
		console.setLayout(new GridBagLayout());
		{
			console.add(consoleLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,0,5));
			JScrollPane scroll = new JScrollPane(consoleText);
			scroll.setBorder(BorderFactory.createEtchedBorder());
			console.add(scroll, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setOpaque(false);
		splitPane.setUI(new BasicSplitPaneUI() {
			public BasicSplitPaneDivider createDefaultDivider() {
				return new BasicSplitPaneDivider(this) {
					public void setBorder(Border b) {
					}
				};
			}
		});

		splitPane.setLeftComponent(main);
		splitPane.setRightComponent(console);
		add(splitPane, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));
	}

	private void showWindow() {
		WindowSize size = config.getGui().getMainWindow().getSize();

		Toolkit t = Toolkit.getDefaultToolkit();
		Insets frame_insets = t.getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		int frame_insets_x = frame_insets.left + frame_insets.right;
		int frame_insets_y = frame_insets.bottom + frame_insets.top;

		// derive virtual bounds of multiple screen layout
		Rectangle virtualBounds = new Rectangle();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		for (int j = 0; j < gs.length; j++) { 
			GraphicsDevice gd = gs[j];
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (int i=0; i < gc.length; i++)
				virtualBounds = virtualBounds.union(gc[i].getBounds());
		}

		Integer x = size.getX();
		Integer y = size.getY();
		Integer width = size.getWidth();
		Integer height = size.getHeight();
		Integer dividerLocation = config.getGui().getMainWindow().getDividerLocation();

		// create default values for main window
		if (x == null || y == null || width == null || height == null || 
				!virtualBounds.contains(x , y, frame_insets_x != 0 ? frame_insets_x : 40, frame_insets_y != 0 ? frame_insets_y : 40)) {
			Dimension dim = t.getScreenSize();
			int user_insets_x = 100;
			int user_insets_y = 50;

			width = dim.width - frame_insets_x - user_insets_x;
			height = dim.height - frame_insets_y - user_insets_y;
			x = user_insets_x / 2 + frame_insets.left;
			y = user_insets_y / 2 + frame_insets.top;

			// if console is detached, also create default values for console window
			if (config.getGui().getConsoleWindow().isDetached()) {
				x -= 15;
				width = width / 2 + 30;
				consoleWindow.setLocation(x + width, y);
				consoleWindow.setSize(width - 30, height);
			} else
				dividerLocation = (int)(width * .5) + 20;
		}

		setLocation(x, y);
		setSize(width, height);
		setVisible(true);

		if (!config.getGui().getConsoleWindow().isDetached())
			main.setPreferredSize(new Dimension((int)(width * .5) + 20, 1));

		if (dividerLocation != null && dividerLocation > 0 && dividerLocation < width)
			splitPane.setDividerLocation(dividerLocation);
	}

	private void initConsole() {
		// let standard out point to console
		JTextAreaOutputStream jTextwriter = new JTextAreaOutputStream(consoleText, new ByteArrayOutputStream());
		PrintStream writer;

		try {
			writer = new PrintStream(jTextwriter, true, "UTF8");
		} catch (UnsupportedEncodingException e) {
			writer = new PrintStream(jTextwriter);
		}

		out = System.out;
		err = System.err;

		System.setOut(writer);
		System.setErr(writer);

		// show console window if required
		if (config.getGui().getConsoleWindow().isDetached()) {
			enableConsoleWindow(true, false);
			requestFocus();
		}
	}

	public void loadSettings() {
		importPanel.loadSettings();
		exportPanel.loadSettings();
		kmlExportPanel.loadSettings();
		databasePanel.loadSettings();
		prefPanel.loadSettings();
		matchingPanel.loadSettings();
	}

	public void setSettings() {
		importPanel.setSettings();
		exportPanel.setSettings();
		kmlExportPanel.setSettings();
		databasePanel.setSettings();
		prefPanel.setSettings();
		matchingPanel.setSettings();
	}

	public void setLoggingSettings() {
		prefPanel.setLoggingSettings();
	}

	public void doTranslation () {
		try {
			LanguageType lang = config.getProject().getGlobal().getLanguage();
			if (lang == currentLang)
				return;

			Internal.I18N = ResourceBundle.getBundle("de.tub.citydb.gui.Label", new Locale(lang.value()));
			currentLang = lang;

			setTitle(Internal.I18N.getString("main.window.title"));
			menu.setTitleAt(0, Internal.I18N.getString("main.tabbedPane.import"));
			menu.setTitleAt(1, Internal.I18N.getString("main.tabbedPane.export"));
			// javier
			menu.setTitleAt(2, Internal.I18N.getString("main.tabbedPane.kmlExport"));
			menu.setTitleAt(3, Internal.I18N.getString("main.tabbedPane.matchingTool"));
			menu.setTitleAt(4, Internal.I18N.getString("main.tabbedPane.database"));
			menu.setTitleAt(5, Internal.I18N.getString("main.tabbedPane.preferences"));

			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
			connectText.setText(Internal.I18N.getString("main.status.database.disconnected.label"));

			menuBar.doTranslation();
			importPanel.doTranslation();
			exportPanel.doTranslation();
			kmlExportPanel.doTranslation();
			databasePanel.doTranslation();
			matchingPanel.doTranslation();
			prefPanel.doTranslation();

			consoleLabel.setText(Internal.I18N.getString("main.label.console"));
		}
		catch (MissingResourceException e) {
			LOG.error("Missing resource: " + e.getKey());
		}
	}

	public void errorMessage(String title, String text) {
		JOptionPane.showMessageDialog(this, text, title, JOptionPane.ERROR_MESSAGE);
	}

	public void enableConsoleWindow(boolean enable, boolean resizeMain) {
		splitPane.setEnabled(!enable);

		if (enable) {
			if (resizeMain) {
				tmpConsoleWidth = console.getWidth();
				if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
					Toolkit t = Toolkit.getDefaultToolkit();
					Insets insets = t.getScreenInsets(getGraphicsConfiguration());
					Rectangle bounds = getGraphicsConfiguration().getBounds();
					setLocation(bounds.x, bounds.y);
					setSize(bounds.width - insets.left - tmpConsoleWidth - insets.right, 
							bounds.height - insets.top - insets.bottom);					
				} else				
					setSize(getWidth() - tmpConsoleWidth, getHeight());
			}

			consoleWindow.activate();			
		} else {
			consoleWindow.dispose();

			int width = tmpConsoleWidth;
			if (width == 0)
				width = consoleWindow.getWidth();

			if (resizeMain) {		
				setSize(getWidth() + width, getHeight());
			}

			width = main.getWidth();
			int dividerLocation = splitPane.getDividerLocation();
			splitPane.setRightComponent(console);
			main.setPreferredSize(new Dimension(width, 1));
			splitPane.setDividerLocation(dividerLocation);
		}
	}

	private void importButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			importPanel.setSettings();

			ImportFilterConfig filter = config.getProject().getImporter().getFilter();

			// check all input values...
			if (config.getInternal().getImportFileName().trim().equals("")) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incompleteData"), 
						Internal.I18N.getString("import.dialog.error.incompleteData.dataset"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getFeatureCount().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCount().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCount().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCount().getTo());

				if (coStart == null || (!coEndValue.trim().equals("") && coEnd == null)) {
					errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("import.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getGmlName().isSet() &&
					filter.getComplexFilter().getGmlName().getValue().trim().equals("")) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// BoundingBox
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			if (!config.getInternal().isConnected()) {
				databasePanel.connect();

				if (!config.getInternal().isConnected())
					return;
			}

			statusText.setText(Internal.I18N.getString("main.status.import.label"));
			LOG.info("Initializing database import...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final ImportStatusDialog importDialog = new ImportStatusDialog(this, 
					Internal.I18N.getString("import.dialog.window"), 
					Internal.I18N.getString("import.dialog.msg"), 
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					importDialog.setLocationRelativeTo(importPanel.getTopLevelAncestor());
					importDialog.setVisible(true);
				}
			});

			importer = new Importer(jaxbCityGMLContext, dbPool, config, eventDispatcher);

			importDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database import.", 
									LogLevelType.INFO));
						}
					});
				}
			});

			boolean success = importer.doProcess();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					importDialog.dispose();
				}
			});

			if (success) {
				LOG.info("Database import successfully finished.");
			} else {
				LOG.warn("Database import aborted.");
			}

			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void validateButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			consoleText.setText("");
			importPanel.setSettings();

			// check for input files...
			if (config.getInternal().getImportFileName().trim().equals("")) {
				errorMessage(Internal.I18N.getString("validate.dialog.error.incompleteData"),
						Internal.I18N.getString("validate.dialog.error.incompleteData.dataset"));
				return;
			}

			statusText.setText(Internal.I18N.getString("main.status.validate.label"));
			LOG.info("Initializing XML validation...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final XMLValidationStatusDialog validatorDialog = new XMLValidationStatusDialog(this, 
					Internal.I18N.getString("validate.dialog.window"), 
					Internal.I18N.getString("validate.dialog.title"), 
					" ", 
					Internal.I18N.getString("validate.dialog.details") , 
					true, 
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					validatorDialog.setLocationRelativeTo(importPanel.getTopLevelAncestor());
					validatorDialog.setVisible(true);
				}
			});

			validator = new XMLValidator(jaxbCityGMLContext, config, eventDispatcher);

			validatorDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of XML validation.", 
									LogLevelType.INFO));
						}
					});
				}
			});

			boolean success = validator.doProcess();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					validatorDialog.dispose();
				}
			});

			if (success) {
				LOG.info("XML validation finished.");
			} else {
				LOG.warn("XML validation aborted.");
			}

			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void exportButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			exportPanel.setSettings();

			ExportFilterConfig filter = config.getProject().getExporter().getFilter();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incompleteData"), 
						Internal.I18N.getString("export.dialog.error.incompleteData.dataset"));
				return;
			}

			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getFeatureCount().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCount().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCount().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCount().getTo());

				if (coStart == null || (!coEndValue.trim().equals("") && coEnd == null)) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}

				if ((coStart != null && coStart <= 0) || (coEnd != null && coEnd <= 0)) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.range"));
					return;
				}
			}

			// gmlName
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getGmlName().isSet() &&
					filter.getComplexFilter().getGmlName().getValue().trim().equals("")) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// tiled bounding box
			int tileAmount = 0;
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}

				if (filter.getComplexFilter().getTiledBoundingBox().getTiling().getMode() != TilingMode.NO_TILING) {
					int rows = filter.getComplexFilter().getTiledBoundingBox().getTiling().getRows();
					int columns = filter.getComplexFilter().getTiledBoundingBox().getTiling().getColumns(); 
					tileAmount = rows * columns;
				}
			}

			if (!config.getInternal().isConnected()) {
				databasePanel.connect();

				if (!config.getInternal().isConnected())
					return;
			}

			statusText.setText(Internal.I18N.getString("main.status.export.label"));
			LOG.info("Initializing database export...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final ExportStatusDialog exportDialog = new ExportStatusDialog(this, 
					Internal.I18N.getString("export.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
					tileAmount,
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.setLocationRelativeTo(exportPanel.getTopLevelAncestor());
					exportDialog.setVisible(true);
				}
			});

			exporter = new Exporter(jaxbCityGMLContext, dbPool, config, eventDispatcher);

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database export.", 
									LogLevelType.INFO));
						}
					});
				}
			});

			boolean success = exporter.doProcess();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.dispose();
				}
			});

			if (success) {
				LOG.info("Database export successfully finished.");
			} else {
				LOG.warn("Database export aborted.");
			}

			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void kmlExportButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			kmlExportPanel.setSettings();

			ExportFilterConfig filter = config.getProject().getKmlExporter().getFilter();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (config.getInternal().getExportFileName().trim().equals("")) {
				errorMessage(Internal.I18N.getString("kmlExport.dialog.error.incompleteData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incompleteData.dataset"));
				return;
			}
			
			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getExportWorkspace())) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("export.dialog.error.incorrectData.date"));
				return;
			}

			// gmlId
			if (filter.isSetSimpleFilter() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// DisplayLevels
			int activeDisplayLevelAmount = config.getProject().getKmlExporter().getActiveDisplayLevelAmount(); 
			if (activeDisplayLevelAmount == 0) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("kmlExport.dialog.error.incorrectData.displayLevels"));
	            return;
			}

			if (!config.getInternal().isConnected()) {
				databasePanel.connect();

				if (!config.getInternal().isConnected())
					return;
			}

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			kmlExporter = new KmlExporter(jaxbKmlContext, jaxbColladaContext, dbPool, config, eventDispatcher);

			int tileAmount = 1;
			// BoundingBox
			if (filter.isSetComplexFilter() &&
					filter.getComplexFilter().getTiledBoundingBox().isSet()) {
				Double xMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getTiledBoundingBox().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getTiledBoundingBox().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}

				try {
					tileAmount = kmlExporter.calculateRowsColumnsAndDelta();
				}
				catch (SQLException sqle) {
					String srsDescription = filter.getComplexFilter().getBoundingBox().getSRS().getDescription();
					Logger.getInstance().error(srsDescription + " " + sqle.getMessage());
					return;
				}
			}
			tileAmount = tileAmount * activeDisplayLevelAmount;

			statusText.setText(Internal.I18N.getString("main.status.kmlExport.label"));
			LOG.info("Initializing database export...");

			final ExportStatusDialog exportDialog = new ExportStatusDialog(this, 
					Internal.I18N.getString("kmlExport.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
					tileAmount,
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.setLocationRelativeTo(exportPanel.getTopLevelAncestor());
					exportDialog.setVisible(true);
				}
			});

			exportDialog.getCancelButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of database export.", 
									LogLevelType.INFO));
						}
					});
				}
			});

			boolean success = kmlExporter.doProcess();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					exportDialog.dispose();
				}
			});

			if (success) {
				LOG.info("Database export successfully finished.");
			} else {
				LOG.warn("Database export aborted.");
			}

			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}
	
	public void connectToDatabase() {
		databasePanel.connect();
	}

	public boolean saveProjectSettings() {
		String configPath = ConfigUtil.createConfigPath(config.getInternal().getConfigPath());

		if (configPath == null) {
			String text = Internal.I18N.getString("common.dialog.error.io.configPath");
			Object[] args = new Object[]{ config.getInternal().getConfigPath() };
			String result = MessageFormat.format(text, args);

			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), result);
			return false;
		}

		String projectConf = configPath + File.separator + config.getInternal().getConfigProject();

		try {
			ProjectConfigUtil.marshal(config.getProject(), projectConf, jaxbProjectContext);
		} catch (JAXBException jaxbE) {
			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					Internal.I18N.getString("common.dialog.error.io.general"));
			return false;
		}

		return true;
	}

	private boolean saveGUISettings() {
		String configPath = ConfigUtil.createConfigPath(config.getInternal().getConfigPath());

		if (configPath == null) {
			String text = Internal.I18N.getString("common.dialog.error.io.configPath");
			Object[] args = new Object[]{ config.getInternal().getConfigPath() };
			String result = MessageFormat.format(text, args);

			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), result);
			return false;
		}

		String guiConf = configPath + File.separator + config.getInternal().getConfigGui();

		// set window size
		Rectangle rect = getBounds();
		MainWindow window = config.getGui().getMainWindow();
		window.getSize().setX(rect.x);
		window.getSize().setY(rect.y);
		window.getSize().setWidth(rect.width);
		window.getSize().setHeight(rect.height);
		window.setDividerLocation(splitPane.getDividerLocation());

		// set console window size
		consoleWindow.setSettings();

		try {
			GuiConfigUtil.marshal(config.getGui(), guiConf, jaxbGuiContext);
		} catch (JAXBException jaxbE) {
			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					Internal.I18N.getString("common.dialog.error.io.general"));
			return false;
		}

		return true;
	}

	public JLabel getStatusText() {
		return statusText;
	}

	public JTextArea getConsoleText() {
		return consoleText;
	}

	public DBConnectionPool getDBPool() {
		return dbPool;
	}

	private void shutdown() {
		config.getInternal().setShuttingDown(true);
		System.setOut(out);
		System.setErr(err);

		consoleWindow.dispose();

		LOG.info("Saving project settings");
		setSettings();
		saveProjectSettings();
		saveGUISettings();

		LOG.info("Terminating database connection");
		try {
			dbPool.close();
		} catch (SQLException e) {
			LOG.error("Failed to terminate database connection: " + e.getMessage());
			LOG.info("Application did not terminate normally");
			System.exit(1);
		}	

		LOG.info("Application successfully terminated");
	}

	private class JTextAreaOutputStream extends FilterOutputStream {
		private int MAX_DOC_LENGTH = 10000;
		private JTextArea ta;

		public JTextAreaOutputStream (JTextArea ta, OutputStream stream) {
			super(stream);
			this.ta = ta;
		}

		@Override
		public void write(final byte[] b) {
			try {
				ta.append(new String(b));
			} catch (Error e) {
				//
			}

			flush();
		}

		@Override
		public void write(final byte b[], final int off, final int len) {
			try {
				ta.append(new String(b, off, len));
			} catch (Error e) {
				//
			}

			flush();
		}

		public void flush() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ta.setCaretPosition(ta.getDocument().getLength());	
					if (ta.getLineCount() > MAX_DOC_LENGTH)
						ta.setText("...truncating console output after " + MAX_DOC_LENGTH + " log messages...");
				}
			});
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("database.isConnected")) {
			if (!(Boolean)evt.getNewValue())
				connectText.setText(Internal.I18N.getString("main.status.database.disconnected.label"));
			else
				connectText.setText(Internal.I18N.getString("main.status.database.connected.label"));
		}
	}
}
