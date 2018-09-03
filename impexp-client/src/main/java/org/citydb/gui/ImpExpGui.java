/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.gui;

import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.window.MainWindow;
import org.citydb.config.gui.window.WindowSize;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LanguageType;
import org.citydb.config.project.global.LogLevel;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.event.global.SwitchLocaleEvent;
import org.citydb.gui.components.console.ConsoleTextPane;
import org.citydb.gui.components.console.ConsoleWindow;
import org.citydb.gui.components.menubar.MenuBar;
import org.citydb.gui.factory.DefaultComponentFactory;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import org.citydb.modules.citygml.importer.CityGMLImportPlugin;
import org.citydb.modules.database.DatabasePlugin;
import org.citydb.modules.kml.KMLExportPlugin;
import org.citydb.modules.preferences.PreferencesPlugin;
import org.citydb.plugin.Plugin;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.view.View;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.ViewEvent;
import org.citydb.plugin.extension.view.ViewEvent.ViewState;
import org.citydb.plugin.extension.view.ViewExtension;
import org.citydb.plugin.extension.view.components.ComponentFactory;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public final class ImpExpGui extends JFrame implements ViewController, EventHandler {
	private final Logger log = Logger.getInstance();
	private final EventDispatcher eventDispatcher; 

	private Config config;
	private JAXBContext jaxbProjectContext;
	private JAXBContext jaxbGuiContext;
	private PluginManager pluginManager;
	private DatabaseConnectionPool dbPool;

	private JPanel main;
	private JLabel statusText;
	private JLabel connectText;
	private MenuBar menuBar;
	private JTabbedPane menu;
	private JSplitPane splitPane;

	private JPanel console;
	private JLabel consoleLabel;
	private ConsolePopupMenuWrapper consolePopup;
	private ConsoleWindow consoleWindow;
	private ConsoleTextPane consoleText;

	private int tmpConsoleWidth;
	private int activePosition;

	private List<View> views;
	private PreferencesPlugin preferencesPlugin;

	private PrintStream out = System.out;
	private PrintStream err = System.err;

	// internal state
	private LanguageType currentLang = null;

	public ImpExpGui(Config config) {
		this.config = config;

		dbPool = DatabaseConnectionPool.getInstance();
		pluginManager = PluginManager.getInstance();

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

		// required for preferences plugin
		consoleText = new ConsoleTextPane();
	}

	public void invoke(JAXBContext jaxbProjectContext,
			JAXBContext jaxbGuiContext,
			List<String> errMsgs) {		
		this.jaxbProjectContext = jaxbProjectContext;
		this.jaxbGuiContext = jaxbGuiContext;		
		
		// init GUI elements
		initGui();
		doTranslation();
		showWindow();

		// initConsole;
		initConsole();

		if (!errMsgs.isEmpty()) {
			for (String msg : errMsgs)
				log.error(msg);
			log.info("Project settings initialized using default values.");
		}

		// log exceptions for disabled ADE extensions
		ADEExtensionManager.getInstance().logExceptions();
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

		menuBar = new MenuBar(this, jaxbProjectContext, config);
		setJMenuBar(menuBar);

		console = new JPanel();
		consoleLabel = new JLabel();
		consoleText.setAutoscrolls(true);
		consoleText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize()));
		consoleText.setEditable(false);
		consoleWindow = new ConsoleWindow(console, config, this);
		consolePopup = new ConsolePopupMenuWrapper(PopupMenuDecorator.getInstance().decorateAndGet(consoleText));

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

		menu = new JTabbedPane();

		// retrieve all views
		views = new ArrayList<>();
		preferencesPlugin = pluginManager.getInternalPlugin(PreferencesPlugin.class);
		DatabasePlugin databasePlugin = pluginManager.getInternalPlugin(DatabasePlugin.class);
		views.add(pluginManager.getInternalPlugin(CityGMLImportPlugin.class).getView());
		views.add(pluginManager.getInternalPlugin(CityGMLExportPlugin.class).getView());
		views.add(pluginManager.getInternalPlugin(KMLExportPlugin.class).getView());

		for (ViewExtension viewExtension : pluginManager.getExternalViewExtensions())
			views.add(viewExtension.getView());

		views.add(databasePlugin.getView());
		views.add(preferencesPlugin.getView());

		// attach views to gui
		int index = 0;
		for (View view : views)
			menu.insertTab(null, view.getIcon(), view.getViewComponent(), view.getToolTip(), index++);

		menu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (menu.getSelectedIndex() == activePosition) 
					return;

				if (menu.getComponentAt(activePosition) == preferencesPlugin.getView().getViewComponent()) {
					if (!preferencesPlugin.requestChange()) {
						menu.setSelectedIndex(activePosition);
						return;
					}
				}

				// fire events for main views
				View newView = views.get(menu.getSelectedIndex());
				View oldView = views.get(activePosition);
				newView.fireViewEvent(new ViewEvent(newView, ViewState.VIEW_ACTIVATED, this));
				oldView.fireViewEvent(new ViewEvent(oldView, ViewState.VIEW_DEACTIVATED, this));

				activePosition = menu.getSelectedIndex();
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				shutdown();
			}
		});

		// settings specific to Mac OS X
		if (OSXAdapter.IS_MAC_OS_X) {
			try {
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("shutdown"));
				OSXAdapter.setAboutHandler(menuBar, menuBar.getClass().getDeclaredMethod("printInfo"));
				OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences"));
			} catch (SecurityException | NoSuchMethodException e) {
				//
			}
		}

		//layout
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImpExpGui.class.getResource("/org/citydb/gui/images/common/logo_small.png")));
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
			scroll.setViewportBorder(BorderFactory.createEmptyBorder());
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

		for (GraphicsDevice gd : gs) {
			GraphicsConfiguration[] gc = gd.getConfigurations();
			for (GraphicsConfiguration aGc : gc)
				virtualBounds = virtualBounds.union(aGc.getBounds());
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
		PrintStream out = consoleText.getConsolePrintStream();
		log.setDefaultOutputStream(out);
		System.setOut(out);

		StyleContext context = new StyleContext();
		Style debugStyle = context.addStyle(LogLevel.WARN.value(), null);
		StyleConstants.setForeground(debugStyle, new Color(0, 0, 238));
		log.setOutputStream(LogLevel.DEBUG, consoleText.getConsolePrintStream(debugStyle));

		Style warnStyle = context.addStyle(LogLevel.WARN.value(), null);
		StyleConstants.setForeground(warnStyle, new Color(166, 111, 0));
		log.setOutputStream(LogLevel.WARN, consoleText.getConsolePrintStream(warnStyle));

		Style errorStyle = context.addStyle(LogLevel.ERROR.value(), null);
		StyleConstants.setForeground(errorStyle, new Color(205, 0, 0));
		PrintStream err = consoleText.getConsolePrintStream(errorStyle);
		log.setOutputStream(LogLevel.ERROR, err);
		System.setErr(err);

		// show console window if required
		if (config.getGui().getConsoleWindow().isDetached()) {
			enableConsoleWindow(true, false);
			requestFocus();
		}
	}

	public void doTranslation () {
		try {
			LanguageType lang = config.getProject().getGlobal().getLanguage();
			if (lang == currentLang)
				return;

			Locale locale = new Locale(lang.value());
			Language.I18N = ResourceBundle.getBundle("org.citydb.config.i18n.language", locale);
			currentLang = lang;

			setDatabaseStatus(dbPool.isConnected());
			statusText.setText(Language.I18N.getString("main.status.ready.label"));
			consoleLabel.setText(Language.I18N.getString("main.console.label"));

			// fire translation notification to plugins
			for (Plugin plugin : pluginManager.getPlugins())
				plugin.switchLocale(locale);

			int index = 0;
			for (View view : views)
				menu.setTitleAt(index++, view.getLocalizedTitle());

			menuBar.doTranslation();
			consolePopup.doTranslation();

			eventDispatcher.triggerSyncEvent(new SwitchLocaleEvent(locale, this));
		} catch (MissingResourceException e) {
			log.error("Missing resource: " + e.getKey());
		}
	}

	@Override
	public void errorMessage(String title, String text) {
		JOptionPane.showMessageDialog(this, text, title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public int warnMessage(String title, String text) {
		return JOptionPane.showConfirmDialog(this, text, title, JOptionPane.OK_CANCEL_OPTION);
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

			if (resizeMain)	
				setSize(getWidth() + width, getHeight());

			width = main.getWidth();
			int dividerLocation = splitPane.getDividerLocation();
			splitPane.setRightComponent(console);
			main.setPreferredSize(new Dimension(width, 1));
			splitPane.setDividerLocation(dividerLocation);
		}
	}

	public boolean saveProjectSettings() {
		Path configDir = getConfigDir();
		if (configDir == null)
			return false;

		try {
			Path projectFile = configDir.resolve(ClientConstants.PROJECT_SETTINGS_FILE);
			ConfigUtil.marshal(config.getProject(), projectFile.toFile(), jaxbProjectContext);
			return true;
		} catch (JAXBException jaxbE) {
			errorMessage(Language.I18N.getString("common.dialog.error.io.title"),
					Language.I18N.getString("common.dialog.error.io.general"));
			return false;
		}
	}

	private void saveGUISettings() {
		Path configDir = getConfigDir();
		if (configDir == null)
			return;

		Path guiFile = configDir.resolve(ClientConstants.GUI_SETTINGS_FILE);

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
			ConfigUtil.marshal(config.getGui(), guiFile.toFile(), jaxbGuiContext);
		} catch (JAXBException jaxbE) {
			errorMessage(Language.I18N.getString("common.dialog.error.io.title"), 
					Language.I18N.getString("common.dialog.error.io.general"));
		}
	}

	private Path getConfigDir() {
		Path configDir = CoreConstants.IMPEXP_DATA_DIR.resolve(ClientConstants.CONFIG_DIR);
		if (!Files.exists(configDir)) {
			try {
				Files.createDirectories(configDir);
			} catch (IOException e) {
				String text = Language.I18N.getString("common.dialog.error.io.configPath");
				Object[] args = new Object[]{ configDir.toString() };
				String result = MessageFormat.format(text, args);

				errorMessage(Language.I18N.getString("common.dialog.error.io.title"), result);
				return null;
			}
		}

		return configDir;
	}

	@Override
	public void setStatusText(String message) {
		statusText.setText(message);
	}

	@Override
	public void clearConsole() {
		consoleText.setText("");
	}

	@Override
	public void setDefaultStatus() {
		statusText.setText(Language.I18N.getString("main.status.ready.label"));
	}

	@Override
	public JFrame getTopFrame() {
		return this;
	}

	@Override
	public ComponentFactory getComponentFactory() {
		return DefaultComponentFactory.getInstance(this, config);
	}

	public ConsoleTextPane getConsole() {
		return consoleText;
	}

	public void disconnectFromDatabase() {
		ObjectRegistry.getInstance().getDatabaseController().disconnect();
	}

	private void setDatabaseStatus(boolean isConnected) {
		if (!isConnected) {
			setTitle(Language.I18N.getString("main.window.title"));
			connectText.setText(Language.I18N.getString("main.status.database.disconnected.label"));
		} else {
			setTitle(Language.I18N.getString("main.window.title") + " : " + dbPool.getActiveDatabaseAdapter().getConnectionDetails().getDescription());
			String text = Language.I18N.getString("main.status.database.connected.label");
			Object[] args = new Object[]{ dbPool.getActiveDatabaseAdapter().getDatabaseType().toString() };
			String result = MessageFormat.format(text, args);
			connectText.setText(result);
		}
	}

	public void showPreferences() {
		// preferences handler for Mac OS X
		menu.setSelectedIndex(menu.indexOfComponent(preferencesPlugin.getView().getViewComponent()));
	}

	public void shutdown() {
		try {
			System.setOut(out);
			System.setErr(err);
			log.setDefaultOutputStream(out);
			log.setOutputStream(LogLevel.ERROR, err);

			eventDispatcher.shutdownNow();
			consoleWindow.dispose();

			// shutdown plugins
			if (!pluginManager.getExternalPlugins().isEmpty())
				log.info("Shutting down plugins");

			for (Plugin plugin : pluginManager.getPlugins())
				plugin.shutdown();

			log.info("Saving project settings");
			saveProjectSettings();
			saveGUISettings();

			if (dbPool.isConnected()) {
				log.info("Terminating database connection");
				dbPool.disconnect();
			}

			log.info("Application successfully terminated");
		} catch (Throwable e) {
			Util.logStackTrace(e);
			log.info("Application did not terminate normally");
		}
	}

	@Override
	public void handleEvent(Event event) {
		setDatabaseStatus(((DatabaseConnectionStateEvent)event).isConnected());
	}

	private final class ConsolePopupMenuWrapper {
		private JMenuItem clear;
		private JMenuItem detach;

		ConsolePopupMenuWrapper(JPopupMenu popupMenu) {
			clear = new JMenuItem();
			detach = new JMenuItem();

			popupMenu.addSeparator();
			popupMenu.add(clear);
			popupMenu.addSeparator();
			popupMenu.add(detach);

			clear.addActionListener(e -> clearConsole());

			detach.addActionListener(e -> {
				boolean status = !config.getGui().getConsoleWindow().isDetached();
				config.getGui().getConsoleWindow().setDetached(status);
				enableConsoleWindow(status, true);

				detach.setText(!status ? Language.I18N.getString("console.label.detach") :
						Language.I18N.getString("console.label.attach"));
			});

			popupMenu.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					clear.setEnabled(consoleText.getDocument().getLength() != 0);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					// nothing to do
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
					// nothing to do
				}
			});

		}

		private void doTranslation() {
			clear.setText(Language.I18N.getString("main.console.popup.clear"));
			detach.setText(config.getGui().getConsoleWindow().isDetached() ?
					Language.I18N.getString("console.label.attach") : Language.I18N.getString("console.label.detach"));
		}
	}
}
