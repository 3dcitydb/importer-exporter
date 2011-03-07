package de.tub.citydb.gui;

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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.gui.menubar.MenuBar;
import de.tub.citydb.log.Logger;

public class ImpExpGui extends JFrame {
	private Logger LOG = Logger.getInstance();

	private JTextArea consoleText;
	private JLabel statusText;
	private JLabel connectText;
	private MenuBar menuBar;
	private JTabbedPane menu;
	JSplitPane splitPane;
	private ImportPanel importPanel;
	private ExportPanel exportPanel;
	private DatabasePanel databasePanel;
	private PrefPanel prefPanel;
	private MatchingPanel matchingPanel;
	private JPanel console;
	private JLabel consoleLabel;
	private ImpExpStatusDialog impExpStatusDialog;
	private int activePosition;

	private ReentrantLock mainLock = new ReentrantLock();
	private Config config;
	private JAXBContext jaxbCityGMLContext;
	private JAXBContext jaxbProjectContext;
	private JAXBContext jaxbGuiContext;
	private DBConnectionPool dbPool;
	private Importer importer;
	private Exporter exporter;

	// internal state
	private LanguageType currentLang = null;
	public static ResourceBundle labels;

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
		JTextAreaOutputStream jTextwriter = new JTextAreaOutputStream(consoleText);
		PrintStream writer;
		try {
			writer = new PrintStream(jTextwriter, true, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			writer = new PrintStream(jTextwriter);
		}
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
		//gui-elemente anlegen
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
		menu.add(databasePanel, "");
		menu.add(prefPanel, "");
		menu.add(matchingPanel, "");
		menu.setEnabledAt(4, false);

		consoleText = new JTextArea("");
		consoleLabel = new JLabel();
		consoleText.setAutoscrolls(true);
		statusText = new JLabel();
		connectText = new JLabel();

		//gui-elemente mit funktionalität ausstatten
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
					//Funktion aus PrefPanel aufrufen, die den Preferences-Dialog zeigt
					if (!prefPanel.requestChange()) {
						menu.setSelectedIndex(activePosition);
					}
				}
				activePosition = menu.getSelectedIndex();
			}
		});

		importPanel.getImportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						importButtonPressed();
					}
				};
				thread.start();
			}
		});

		exportPanel.getExportButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						exportButtonPressed();
					}
				};
				thread.start();
			}
		});

		addWindowListener(new WindowListener() {
			public void windowClosed(WindowEvent e) {
				saveIniFile();
				closeDBConnection();
			}
			//nicht schön, aber selten
			public void windowActivated(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});

		//layout

		this.setTitle("");
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
	}

	public void setSettings() {
		importPanel.setSettings();
		exportPanel.setSettings();
		prefPanel.setSettings();
		databasePanel.setSettings();
	}

	public void doTranslation () {
		try {
			// i18n labels initialisieren
			LanguageType lang = config.getProject().getGlobal().getLanguage();

			if (lang == null || lang == LanguageType.SYSTEM) {
				String selLocale = System.getProperty("user.language");

				if (!(selLocale.equals("de") || selLocale.equals("en")))
					selLocale = "en";

				lang = LanguageType.fromValue(selLocale);
				config.getProject().getGlobal().setLanguage(lang);
			}

			if (lang == currentLang)
				return;

			labels = ResourceBundle.getBundle("de.tub.citydb.gui.Label", new Locale(lang.value()));
			currentLang = lang;

			setTitle(labels.getString("main.window.title"));
			menu.setTitleAt(0, labels.getString("main.tabbedPane.import"));
			menu.setTitleAt(1, labels.getString("main.tabbedPane.export"));
			menu.setTitleAt(2, labels.getString("main.tabbedPane.database"));
			menu.setTitleAt(3, labels.getString("main.tabbedPane.preferences"));
			menu.setTitleAt(4, labels.getString("main.tabbedPane.matchingTool"));
			consoleLabel.setText(labels.getString("main.label.console"));

			statusText.setText(labels.getString("main.status.ready.label"));
			connectText.setText(labels.getString("main.status.database.disconnected.label"));
			
			// doTranslation-Methoden aller anderen Klassen aufrufen
			menuBar.doTranslation();
			importPanel.doTranslation();
			exportPanel.doTranslation();
			databasePanel.doTranslation();
			prefPanel.doTranslation();
		}
		catch (MissingResourceException e) {
			System.out.println(e.getKey());
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
				errorMessage("Importdaten unvollständig", "Bitte geben Sie den zu importierenden Datensatz an.");
				return;
			}

			// check more here...?!
			if (config.getProject().getDatabase().getWorkspace().getImportWorkspace().trim().equals("")) {
				errorMessage("Importdaten unvollständig", "Bitte geben Sie einen Datenbank-Workspace an (Default: LIVE).");
				return;
			}

			// gmlId
			if (filter.isSetSimple() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage("Importdaten fehlerhaft", "Bitte geben Sie eine gültige GML-ID an.");
				return;
			}

			// cityObject
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getFeatureCountFilter().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCountFilter().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCountFilter().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCountFilter().getTo());

				if (coStart == null || (!coEndValue.trim().equals("") && coEnd == null)) {
					errorMessage("Importdaten fehlerhaft", "Bitte geben Sie einen gültigen Bereich für die zu importierenden City Objects an.");
					return;
				}

				if ((coStart != null && coStart < 0) || (coEnd != null && coEnd < 0)) {
					errorMessage("Importdaten fehlerhaft", "Bitte geben Sie einen gültigen Bereich für die zu importierenden City Objects an.");
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					errorMessage("Importdaten fehlerhaft", "Bitte geben Sie einen gültigen Bereich für die zu importierenden City Objects an.");
					return;
				}
			}

			// gmlName
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getGmlNameFilter().isSet() &&
					filter.getComplexFilter().getGmlNameFilter().getValue().trim().equals("")) {
				errorMessage("Importdaten fehlerhaft", "Bitte geben Sie einen gültigen GML-Namen an.");
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
					errorMessage("Importdaten fehlerhaft", "Bitte geben Sie eine gültige Bounding Box an.");
					return;
				}
			}

			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
					return;
			}

			statusText.setText(labels.getString("main.status.import.label"));
			LOG.info("Datenbankimport wird initialisiert...");

			// initialize event dispatcher
			EventDispatcher eventDispatcher = new EventDispatcher();
			impExpStatusDialog = new ImpExpStatusDialog(this, 
					"Import", 
					"Importiere...", 
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					impExpStatusDialog.pack();
					impExpStatusDialog.setLocationRelativeTo(importPanel.getTopLevelAncestor());
					impExpStatusDialog.setVisible(true);
				}
			});

			importer = new Importer(jaxbCityGMLContext, dbPool, config, eventDispatcher);
			final Thread importerThread = Thread.currentThread();

			impExpStatusDialog.getAbbrechenButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								impExpStatusDialog.getHeaderLabel().setText("Abbruch - Bitte warten...");
								importer.shutdown(importerThread);
							} catch (IOException e) {
								//
							}
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
					impExpStatusDialog.dispose();
				}
			});

			if (success) {
				LOG.info("Datenbankimport erfolgreich abgeschlossen.");
			} else {
				LOG.warn("Datenbankimport wurde abgebrochen.");
			}

			statusText.setText(labels.getString("main.status.ready.label"));
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
				errorMessage("Exportdaten unvollständig", "Bitte geben Sie den zu exportierenden Datensatz an.");
				return;
			}

			// check more here...!?
			if (db.getWorkspace().getExportWorkspace().trim().equals("")) {
				errorMessage("Importdaten unvollständig", "Bitte geben Sie einen Datenbank-Workspace an (Default: LIVE).");
				return;
			}

			if (!db.getWorkspace().getExportDate().trim().equals("")) {

				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
				format.setLenient(false);
				try {
					Date date = format.parse(db.getWorkspace().getExportDate());
				} catch (ParseException e1) {
					errorMessage("Exportdaten fehlerhaft", "Bitte geben Sie ein gültiges Datum im Format 'DD.MM.YYYY' an.");
					return;
				}

			}

			// gmlId
			if (filter.isSetSimple() &&
					filter.getSimpleFilter().getGmlIdFilter().getGmlIds().isEmpty()) {
				errorMessage("Exportdaten fehlerhaft", "Bitte geben Sie eine gültige GML-ID an.");
				return;
			}

			// cityObject
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getFeatureCountFilter().isSet()) {
				Long coStart = filter.getComplexFilter().getFeatureCountFilter().getFrom();
				Long coEnd = filter.getComplexFilter().getFeatureCountFilter().getTo();
				String coEndValue = String.valueOf(filter.getComplexFilter().getFeatureCountFilter().getTo());

				if (coStart == null || (!coEndValue.trim().equals("") && coEnd == null)) {
					errorMessage("Exportdaten fehlerhaft", "Bitte geben Sie einen gültigen Bereich für die zu exportierenden City Objects an.");
					return;
				}

				if ((coStart != null && coStart < 0) || (coEnd != null && coEnd < 0)) {
					errorMessage("Exportdaten fehlerhaft", "Bitte geben Sie einen gültigen Bereich für die zu importierenden City Objects an.");
					return;
				}

				if (coEnd != null && coEnd < coStart) {
					errorMessage("Exportdaten fehlerhaft", "Bitte geben Sie einen gültigen Bereich für die zu exportierenden City Objects an.");
					return;
				}
			}

			// gmlName
			if (filter.isSetComplex() &&
					filter.getComplexFilter().getGmlNameFilter().isSet() &&
					filter.getComplexFilter().getGmlNameFilter().getValue().trim().equals("")) {
				errorMessage("Exportdaten fehlerhaft", "Bitte geben Sie einen gültigen GML-Namen an.");
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
					errorMessage("Importdaten fehlerhaft", "Bitte geben Sie eine gültige Bounding Box an.");
					return;
				}
			}

			if (!intConfig.isDbIsConnected()) {
				databasePanel.connect();

				if (!intConfig.isDbIsConnected())
					return;
			}

			statusText.setText(labels.getString("main.status.export.label"));
			LOG.info("Datenbankexport wird initialisiert...");
			
			// initialize event dispatcher
			EventDispatcher eventDispatcher = new EventDispatcher();
			impExpStatusDialog = new ImpExpStatusDialog(this, 
					"Export", 
					"Exportiere...",
					eventDispatcher);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					impExpStatusDialog.pack();
					impExpStatusDialog.setLocationRelativeTo(exportPanel.getTopLevelAncestor());
					impExpStatusDialog.setVisible(true);
				}
			});

			exporter = new Exporter(jaxbCityGMLContext, dbPool, config, eventDispatcher);

			impExpStatusDialog.getAbbrechenButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							impExpStatusDialog.getHeaderLabel().setText("Abbruch - Bitte warten...");

							Thread thread = new Thread() {
								public void run() {
		                       		exporter.shutdown();
								}
							};
							thread.start();
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
					impExpStatusDialog.dispose();
				}
			});

			if (success) {
				LOG.info("Datenbankexport erfolgreich abgeschlossen.");
			} else {
				LOG.warn("Datenbankexport wurde abgebrochen.");
			}

			statusText.setText(labels.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}
	
	protected void saveIniFile() {
		String configPath = ConfigUtil.createConfigPath(System.getProperty("user.dir"), config.getInternal().getConfigPath());

		if (configPath == null) {
			errorMessage("I/O-Fehler", "Konfigurationsdatei konnte nicht geschrieben werden: Der Pfad " +
					System.getProperty("user.dir") + File.separator + config.getInternal().getConfigPath() +
			" ist nicht verfügbar.");
			return;
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
			errorMessage("I/O-Fehler", "Konfigurationsdatei konnte nicht geschrieben werden:\n\n" + jaxbE.getMessage());
		}

		try {
			GuiConfigUtil.marshal(config.getGui(), guiConf, jaxbGuiContext);
		} catch (JAXBException jaxbE) {
			errorMessage("I/O-Fehler", "Konfigurationsdatei konnte nicht geschrieben werden:\n\n" + jaxbE.getMessage());
		}
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
				errorMessage("Datenbankfehler", "Die Verbindung zur Datenbank konnte nicht korrekt beendet werden:\n\n" + e.getMessage());
			}
		}
	}

	private class JTextAreaOutputStream extends OutputStream {
		JTextArea ta;
		public JTextAreaOutputStream (JTextArea t)
		{
			super();
			this.ta = t;
		}

		public void write (int i)
		{
			byte[] bytes = new byte[1];
			bytes[0] = (byte)i;
			String s = new String(bytes);
			ta.append(s);
			ta.setCaretPosition(ta.getText().length());
		}

		public void write (char[] buf, int off, int len)
		{
			String s = new String (buf, off, len);
			ta.append(s);
			ta.setCaretPosition(ta.getText().length());
		}
	}

}
