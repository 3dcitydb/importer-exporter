package de.tub.citydb.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import de.tub.citydb.config.gui.window.GuiWindow;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.ProjectConfigUtil;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.controller.Exporter;
import de.tub.citydb.controller.Importer;
import de.tub.citydb.controller.Matcher;
import de.tub.citydb.controller.XMLValidator;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.concurrent.InterruptEnum;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.gui.components.ExportStatusDialog;
import de.tub.citydb.gui.components.ImportStatusDialog;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.gui.components.XMLValidationStatusDialog;
import de.tub.citydb.gui.menubar.MenuBar;
import de.tub.citydb.gui.panel.db.DatabasePanel;
import de.tub.citydb.gui.panel.exporter.ExportPanel;
import de.tub.citydb.gui.panel.importer.ImportPanel;
import de.tub.citydb.gui.panel.matching.MatchingPanel;
import de.tub.citydb.gui.panel.settings.PrefPanel;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;

public class ImpExpGui extends JFrame {
	private final Logger LOG = Logger.getInstance();

	private final ReentrantLock mainLock = new ReentrantLock();
	private final Config config;
	private final JAXBContext jaxbCityGMLContext;
	private final JAXBContext jaxbProjectContext;
	private final JAXBContext jaxbGuiContext;
	private final DBConnectionPool dbPool;

	private JTextArea consoleText;
	private JLabel statusText;
	private JLabel connectText;
	private MenuBar menuBar;
	private JTabbedPane menu;
	private JSplitPane splitPane;
	private ImportPanel importPanel;
	private ExportPanel exportPanel;
	private DatabasePanel databasePanel;
	private PrefPanel prefPanel;
	private MatchingPanel matchingPanel;
	private JPanel console;
	private JLabel consoleLabel;
	private int activePosition;

	private Importer importer;
	private Exporter exporter;
	private XMLValidator validator;

	private PrintStream out;
	private PrintStream err;
	
	// internal state
	private LanguageType currentLang = null;

	//Set Look & Feel
	{
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public ImpExpGui(JAXBContext jaxbCityGMLContext,
			JAXBContext jaxbProjectContext,
			JAXBContext jaxbGuiContext,
			DBConnectionPool dbPool,
			Config config) {
		this.jaxbCityGMLContext = jaxbCityGMLContext;
		this.jaxbProjectContext = jaxbProjectContext;
		this.jaxbGuiContext = jaxbGuiContext;
		this.dbPool = dbPool;
		this.config = config;
	}

	public void invoke(List<String> errMsgs) {
		// init GUI elements
		initGui();
		doTranslation();
		loadSettings();

		// let standard.out point to GUI elements...
		JTextAreaOutputStream jTextwriter = new JTextAreaOutputStream(consoleText, new ByteArrayOutputStream());
		PrintStream writer;
		
		try {
			writer = new PrintStream(jTextwriter, true, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			writer = new PrintStream(jTextwriter);
		}
		
		out = System.out;
		err = System.err;
		
		System.setOut(writer);
		System.setErr(writer);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);

		if (!errMsgs.isEmpty()) {
			for (String msg : errMsgs)
				LOG.error(msg);
			LOG.info("Project settings initialized using default values.");
		}		
	}

	private void initGui() {
		activePosition = 0;

		menuBar = new MenuBar(config, jaxbProjectContext);
		setJMenuBar(menuBar);

		menu = new JTabbedPane();
		importPanel = new ImportPanel(config);
		exportPanel = new ExportPanel(config);
		databasePanel = new DatabasePanel(config, this);
		prefPanel = new PrefPanel(config, this);
		matchingPanel = new MatchingPanel(config);

		menu.add(importPanel, "");
		menu.add(exportPanel, "");
		menu.add(matchingPanel, BorderLayout.NORTH);
		menu.add(databasePanel, "");
		menu.add(prefPanel, "");

		consoleText = new JTextArea();
		consoleLabel = new JLabel();
		consoleText.setAutoscrolls(true);
		statusText = new JLabel();
		connectText = new JLabel();

		consoleText.setFont(new Font(Font.MONOSPACED, 0, 11));
		consoleText.setEditable(false);

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
				if (menu.getSelectedIndex()==activePosition) return;

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

		matchingPanel.getMatchButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						matchButtonPressed();
						return null;
					}
				};
				worker.execute();
			}
		});

		matchingPanel.getMergeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						mergeButtonPressed();
						return null;
					}
				};
				worker.execute();
			}
		});
		
		matchingPanel.getDeleteButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final SwingWorker<Object, Void> worker = new SwingWorker<Object, Void>() {
					public Object doInBackground() {
						deleteButtonPressed();
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
		setTitle("");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImpExpGui.class.getResource("/resources/img/logo_small.png")));
		setLayout(new GridBagLayout());

		JPanel left = new JPanel();
		left.setBorder(BorderFactory.createEmptyBorder());
		left.setBackground(this.getBackground());
		left.setLayout(new GridBagLayout());
		{
			left.add(menu, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			JPanel status = new JPanel();
			status.setBorder(BorderFactory.createEmptyBorder());
			status.setBackground(this.getBackground());
			left.add(status, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));
			status.setLayout(new GridBagLayout());
			{
				status.add(statusText, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,2));
				status.add(connectText, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,2,0,0));
			}
		}

		console = new JPanel();
		console.setBorder(BorderFactory.createEmptyBorder());
		console.setBackground(this.getBackground());
		console.setLayout(new GridBagLayout());
		{
			consoleLabel = new JLabel();
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

		splitPane.setLeftComponent(left);
		splitPane.setRightComponent(console);

		this.add(splitPane, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));	

		// finally adapt GUI elements according to config data
		// set window size
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension sz = t.getScreenSize();

		Integer x, y, width, height, dividerLocation;
		GuiWindow window = config.getGui().getWindow();
		x = window.getSize().getX();
		y = window.getSize().getY();
		width = window.getSize().getWidth();
		height = window.getSize().getHeight();
		dividerLocation = window.getDividerLocation();

		if (x == null || y == null || width == null || height == null) {
			width = sz.width - 200;
			height = sz.height - 200;
			if (height < 700) height = 700;
			x = (sz.width - width) / 2;
			y = (sz.height - height) / 2;
		}

		if (dividerLocation != null) {
			if (dividerLocation > 0 && dividerLocation < width)
				splitPane.setDividerLocation(dividerLocation);
		}

		setLocation(x, y);
		setSize(width, height);
		left.setPreferredSize(new Dimension((int)(width * .5), 1));		
	}

	public void loadSettings() {
		importPanel.loadSettings();
		exportPanel.loadSettings();
		databasePanel.loadSettings();
		prefPanel.loadSettings();
		matchingPanel.loadSettings();
	}

	public void setSettings() {
		importPanel.setSettings();
		exportPanel.setSettings();
		prefPanel.setSettings();
		databasePanel.setSettings();
		matchingPanel.setSettings();
	}
	
	public void setLoggingSettings() {
		prefPanel.setLoggingSettings();
	}

	public void doTranslation () {
		try {
			// i18n labels initialisieren
			LanguageType lang = config.getProject().getGlobal().getLanguage();
			if (lang == currentLang)
				return;

			Internal.I18N = ResourceBundle.getBundle("de.tub.citydb.gui.Label", new Locale(lang.value()));
			currentLang = lang;

			setTitle(Internal.I18N.getString("main.window.title"));
			menu.setTitleAt(0, Internal.I18N.getString("main.tabbedPane.import"));
			menu.setTitleAt(1, Internal.I18N.getString("main.tabbedPane.export"));
			menu.setTitleAt(2, Internal.I18N.getString("main.tabbedPane.matchingTool"));
			menu.setTitleAt(3, Internal.I18N.getString("main.tabbedPane.database"));
			menu.setTitleAt(4, Internal.I18N.getString("main.tabbedPane.preferences"));
			
			consoleLabel.setText(Internal.I18N.getString("main.label.console"));

			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
			connectText.setText(Internal.I18N.getString("main.status.database.disconnected.label"));

			// doTranslation-Methoden aller anderen Klassen aufrufen
			menuBar.doTranslation();
			importPanel.doTranslation();
			exportPanel.doTranslation();
			databasePanel.doTranslation();
			prefPanel.doTranslation();
			matchingPanel.doTranslation();
		}
		catch (MissingResourceException e) {
			LOG.error("Missing resource: " + e.getKey());
		}
	}

	public void errorMessage(String title, String text) {
		JOptionPane.showMessageDialog(this, text, title, JOptionPane.ERROR_MESSAGE);
	}

	private void importButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			importPanel.setSettings();
			databasePanel.setSettings();

			FilterConfig filter = config.getProject().getImporter().getFilter();
			Internal intConfig = config.getInternal();

			// check all input values...
			if (intConfig.getImportFileName().trim().equals("")) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incompleteData"), 
						Internal.I18N.getString("import.dialog.error.incompleteData.dataset"));
				return;
			}

			// check more here...?!
			if (config.getProject().getDatabase().getWorkspace().getImportWorkspace().trim().equals("")) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incompleteData"),
						Internal.I18N.getString("common.dialog.error.incompleteData.workspace"));
				return;
			}

			// gmlId
			if (filter.isSetSimple() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getFeatureCountFilter().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCountFilter().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCountFilter().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCountFilter().getTo());

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
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getGmlNameFilter().isSet() &&
					filter.getComplexFilter().getGmlNameFilter().getValue().trim().equals("")) {
				errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// BoundingBox
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getBoundingBoxFilter().isSet()) {
				Double xMin = filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					errorMessage(Internal.I18N.getString("import.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
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
			databasePanel.setSettings();

			FilterConfig filter = config.getProject().getExporter().getFilter();
			Internal intConfig = config.getInternal();
			Database db = config.getProject().getDatabase();

			// check all input values...
			if (intConfig.getExportFileName().trim().equals("")) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incompleteData"), 
						Internal.I18N.getString("export.dialog.error.incompleteData.dataset"));
				return;
			}

			// check more here...!?
			if (db.getWorkspace().getExportWorkspace().trim().equals("")) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incompleteData"),
						Internal.I18N.getString("common.dialog.error.incompleteData.workspace"));
				return;
			}

			if (!db.getWorkspace().getExportDate().trim().equals("")) {

				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
				format.setLenient(false);
				try {
					format.parse(db.getWorkspace().getExportDate());
				} catch (ParseException e) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
							Internal.I18N.getString("export.dialog.error.incorrectData.date"));
					return;
				}

			}

			// gmlId
			if (filter.isSetSimple() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlId"));
				return;
			}

			// cityObject
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getFeatureCountFilter().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCountFilter().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCountFilter().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCountFilter().getTo());

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
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getGmlNameFilter().isSet() &&
					filter.getComplexFilter().getGmlNameFilter().getValue().trim().equals("")) {
				errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
						Internal.I18N.getString("common.dialog.error.incorrectData.gmlName"));
				return;
			}

			// BoundingBox
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getBoundingBoxFilter().isSet()) {
				Double xMin = filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getX();
				Double yMin = filter.getComplexFilter().getBoundingBoxFilter().getLowerLeftCorner().getY();
				Double xMax = filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getX();
				Double yMax = filter.getComplexFilter().getBoundingBoxFilter().getUpperRightCorner().getY();

				if (xMin == null || yMin == null || xMax == null || yMax == null) {
					errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"),
							Internal.I18N.getString("common.dialog.error.incorrectData.bbox"));
					return;
				}
			}

			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
					return;
			}

			statusText.setText(Internal.I18N.getString("main.status.export.label"));
			LOG.info("Initializing database export...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final ExportStatusDialog exportDialog = new ExportStatusDialog(this, 
					Internal.I18N.getString("export.dialog.window"),
					Internal.I18N.getString("export.dialog.msg"),
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

	private void matchButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			matchingPanel.setSettings();

			Internal intConfig = config.getInternal();

			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
					return;
			}

			statusText.setText(Internal.I18N.getString("main.status.match.label"));
			LOG.info("Initializing matching process...");
			
			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final StatusDialog status = new StatusDialog(this, 
					Internal.I18N.getString("match.step1.dialog.window"), 
					Internal.I18N.getString("match.step1.dialog.msg"),
					" ",
					Internal.I18N.getString("match.step1.dialog.details"),
					true,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(matchingPanel.getTopLevelAncestor());
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(dbPool, config, eventDispatcher);
			
			status.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of matching process.", 
									LogLevelType.INFO));
						}
					});
				}
			});
			
			boolean success = matcher.match();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});
			
			if (success) {
				LOG.info("Matching process successfully finished.");
			} else {
				LOG.warn("Matching process aborted.");
			}
			
			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void mergeButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			matchingPanel.setSettings();

			Internal intConfig = config.getInternal();

			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
					return;
			}

			statusText.setText(Internal.I18N.getString("main.status.merge.label"));
			LOG.info("Initializing merging process...");
			
			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final StatusDialog status = new StatusDialog(this, 
					Internal.I18N.getString("match.step2.dialog.window"), 
					Internal.I18N.getString("match.step2.dialog.msg"),
					" ",
					Internal.I18N.getString("match.step2.dialog.details"),
					false,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(matchingPanel.getTopLevelAncestor());
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(dbPool, config, eventDispatcher);
			boolean success = matcher.merge();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});
			
			if (success) {
				LOG.info("Merging process successfully finished.");
			} else {
				LOG.warn("Merging process aborted abnormally.");
			}
			
			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void deleteButtonPressed() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			// set configs
			consoleText.setText("");
			matchingPanel.setSettings();
			
			Internal intConfig = config.getInternal();
			
			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
					return;
			}
			
			statusText.setText(Internal.I18N.getString("main.status.delete.label"));
			LOG.info("Initializing deletion of buildings...");
			
			String msg = Internal.I18N.getString("match.tools.dialog.msg");
			Object[] args = new Object[]{ config.getProject().getMatching().getMatchingDelete().getLineage() };
			String result = MessageFormat.format(msg, args);
			
			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final StatusDialog status = new StatusDialog(this, 
					Internal.I18N.getString("match.tools.dialog.title"), 
					result,
					"",
					Internal.I18N.getString("match.tools.dialog.details"),
					false,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(matchingPanel.getTopLevelAncestor());
					status.setVisible(true);
				}
			});
			
			Matcher matcher = new Matcher(dbPool, config, eventDispatcher);
			boolean success = matcher.delete();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});
			
			if (success) {
				LOG.info("Deletion of buildings successfully finished.");
			} else {
				LOG.warn("Deletion of buildings aborted abnormally.");
			}
			
			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	public boolean saveIniFile() {
		String configPath = ConfigUtil.createConfigPath(config.getInternal().getConfigPath());

		if (configPath == null) {
			String text = Internal.I18N.getString("common.dialog.error.io.configPath");
			Object[] args = new Object[]{ config.getInternal().getConfigPath() };
			String result = MessageFormat.format(text, args);

			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), result);
			return false;
		}

		String projectConf = configPath + File.separator + config.getInternal().getConfigProject();
		String guiConf = configPath + File.separator + config.getInternal().getConfigGui();
		setSettings();

		// get window size
		Rectangle rect = this.getBounds();
		GuiWindow window = config.getGui().getWindow();
		window.getSize().setX(rect.x);
		window.getSize().setY(rect.y);
		window.getSize().setWidth(rect.width);
		window.getSize().setHeight(rect.height);
		window.setDividerLocation(splitPane.getDividerLocation());

		try {
			ProjectConfigUtil.marshal(config.getProject(), projectConf, jaxbProjectContext);
		} catch (JAXBException jaxbE) {
			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					Internal.I18N.getString("common.dialog.error.io.general"));
			return false;
		}

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

	public JLabel getConnectText() {
		return connectText;
	}

	public JTextArea getConsoleText() {
		return consoleText;
	}

	public DBConnectionPool getDBPool() {
		return dbPool;
	}

	private void closeDBConnection() {
		if (dbPool != null) {
			try {
				dbPool.close();
			} catch (SQLException e) {
				String text = Internal.I18N.getString("db.dialog.error.closeConn");
				Object[] args = new Object[]{ e.getMessage() };
				String result = MessageFormat.format(text, args);

				errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
			}
		}
	}
	
	private void shutdown() {
		System.setOut(out);
		System.setErr(err);
		
		LOG.info("Saving project settings");
		saveIniFile();
		
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
}
