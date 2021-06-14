/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.cli.util.CliConstants;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.style.Theme;
import org.citydb.config.gui.window.MainWindow;
import org.citydb.config.gui.window.WindowSize;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LanguageType;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.event.global.SwitchLocaleEvent;
import org.citydb.gui.console.ConsoleTextPane;
import org.citydb.gui.console.ConsoleWindow;
import org.citydb.gui.console.StyledConsoleLogger;
import org.citydb.gui.menu.MenuBar;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.database.DatabasePlugin;
import org.citydb.gui.operation.exporter.CityGMLExportPlugin;
import org.citydb.gui.operation.importer.CityGMLImportPlugin;
import org.citydb.gui.operation.preferences.PreferencesPlugin;
import org.citydb.gui.operation.visExporter.VisExportPlugin;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.log.DefaultConsoleLogger;
import org.citydb.log.Logger;
import org.citydb.plugin.Plugin;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.view.View;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.ViewEvent;
import org.citydb.plugin.extension.view.ViewEvent.ViewState;
import org.citydb.plugin.extension.view.ViewExtension;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.CoreConstants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

public final class ImpExpGui extends JFrame implements ViewController, EventHandler {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final Path configFile;
	private final PluginManager pluginManager;
	private final DatabaseConnectionPool dbPool;
	private final EventDispatcher eventDispatcher;
	private final ConsoleTextPane consoleText;
	private final StyledConsoleLogger consoleLogger;
	private final PrintStream out = System.out;
	private final PrintStream err = System.err;

	private JLabel statusText;
	private JLabel connectText;
	private MenuBar menuBar;
	private JTabbedPane menu;
	private JSplitPane splitPane;
	private JPanel console;
	private FlatTabbedPane consolePane;
	private ConsolePopupMenuWrapper consolePopup;
	private ConsoleWindow consoleWindow;

	private int consoleWidth;
	private int activePosition;
	private List<View> views;
	private PreferencesPlugin preferencesPlugin;
	private LanguageType currentLang;

	public ImpExpGui(Path configFile) {
		this.configFile = Objects.requireNonNull(configFile, "configFile cannot be null.");

		config = ObjectRegistry.getInstance().getConfig();
		dbPool = DatabaseConnectionPool.getInstance();
		pluginManager = PluginManager.getInstance();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

		// required for preferences plugin
		consoleText = new ConsoleTextPane();
		consoleLogger = new StyledConsoleLogger(consoleText, StandardCharsets.UTF_8);

		CoreConstants.IS_GUI_MODE = true;
	}

	public void invoke() {
		// init GUI elements
		initGui();
		doTranslation();
		showWindow();
		initConsole();

		// log exceptions for disabled ADE extensions
		ADEExtensionManager.getInstance().logExceptions();
	}

	private void initGui() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		menuBar = new MenuBar(this, config);
		setJMenuBar(menuBar);

		console = new JPanel();
		consolePane = new FlatTabbedPane();
		consoleText.setAutoscrolls(true);
		consoleText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize()));
		consoleText.setEditable(false);

		consoleWindow = new ConsoleWindow(console, config, this);
		consolePopup = new ConsolePopupMenuWrapper(PopupMenuDecorator.getInstance().decorateAndGet(consoleText));

		// retrieve all views
		views = new ArrayList<>();
		preferencesPlugin = pluginManager.getInternalPlugin(PreferencesPlugin.class);
		DatabasePlugin databasePlugin = pluginManager.getInternalPlugin(DatabasePlugin.class);
		views.add(pluginManager.getInternalPlugin(CityGMLImportPlugin.class).getView());
		views.add(pluginManager.getInternalPlugin(CityGMLExportPlugin.class).getView());
		views.add(pluginManager.getInternalPlugin(VisExportPlugin.class).getView());

		for (ViewExtension viewExtension : pluginManager.getExternalPlugins(ViewExtension.class)) {
			View view = viewExtension.getView();
			if (view == null || view.getViewComponent() == null) {
				log.error("Failed to get view component for plugin " + viewExtension.getClass().getName() + ".");
				continue;
			}

			views.add(view);
		}

		views.add(databasePlugin.getView());
		views.add(preferencesPlugin.getView());

		// attach views to gui
		menu = new JTabbedPane();
		menu.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

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

		// settings specific to macOS
		if (OSXAdapter.IS_MAC_OS) {
			try {
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("shutdown"));
				OSXAdapter.setAboutHandler(menuBar, menuBar.getClass().getDeclaredMethod("printInfo"));
				OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences"));
			} catch (SecurityException | NoSuchMethodException e) {
				//
			}
		}

		// layout
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImpExpGui.class.getResource("/org/citydb/gui/logos/logo_small.png")));
		setLayout(new GridBagLayout());

		// main panel
		JPanel main = new JPanel();
		main.setLayout(new GridBagLayout());
		{
			statusText = new JLabel();
			connectText = new JLabel();

			JPanel status = new JPanel();
			status.setLayout(new GridBagLayout());
			{
				status.add(statusText, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				status.add(connectText, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
			}

			main.add(menu, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			main.add(status, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5));
		}

		// console panel
		console.setLayout(new GridBagLayout());
		{
			JScrollPane scroll = new JScrollPane(consoleText);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.setViewportBorder(BorderFactory.createEmptyBorder());

			consolePane.addTab(null, scroll);
			consolePane.setHasFullBorder(true);
			consolePane.setUI(new FlatTabbedPaneUI() {
				protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) {
					// do not paint tab background
				}
			});

			console.add(consolePane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 10, 10, 10));
		}

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		splitPane.setLeftComponent(main);
		splitPane.setRightComponent(console);
		add(splitPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(this::updateComponentUI);
			}
		});

		updateComponentUI();
	}

	private void updateComponentUI() {
		consoleText.setBackground(UIManager.getColor("TextField.background"));
		menu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));
		consolePane.setUI(new FlatTabbedPaneUI() {
			protected void paintTabBackground(Graphics g, int p, int i, int x, int y, int w, int h, boolean s) {
				// do not paint tab background
			}
		});
	}

	private void showWindow() {
		GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(screen.getDefaultConfiguration());

		// derive virtual bounds of multiple screen layout
		Rectangle virtualBounds = new Rectangle();
		for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			for (GraphicsConfiguration configuration : device.getConfigurations()) {
				virtualBounds = virtualBounds.union(configuration.getBounds());
			}
		}

		Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
		AffineTransform transform = screen.getDefaultConfiguration().getDefaultTransform();
		screenBounds.width = screenBounds.width - (int) ((screenInsets.left + screenInsets.right) / transform.getScaleX());
		screenBounds.height = screenBounds.height - (int) ((screenInsets.top + screenInsets.bottom) / transform.getScaleY());

		// get user-defined window size from GUI configuration
		WindowSize size = config.getGuiConfig().getMainWindow().getSize();
		Integer x = size.getX();
		Integer y = size.getY();
		Integer width = size.getWidth();
		Integer height = size.getHeight();
		Integer dividerLocation = config.getGuiConfig().getMainWindow().getDividerLocation();

		// create default values for main window
		if (x == null || y == null || width == null || height == null ||
				!virtualBounds.contains(x , y, 50, 50)) {
			int preferredWidth = 1700;
			int preferredHeight = 1000;

			if (screenBounds.width - preferredWidth >= 0 && screenBounds.height - preferredHeight >= 0) {
				width = preferredWidth;
				height = preferredHeight;
			} else {
				width = screenBounds.width - 50;
				height = screenBounds.height - 50;
			}

			x = (screenBounds.width - width) / 2 + (int) (screenInsets.left / transform.getScaleX());
			y = (screenBounds.height - height) / 2 + (int) (screenInsets.top / transform.getScaleY());

			// if console is detached, also create default values for console window
			if (config.getGuiConfig().getConsoleWindow().isDetached()) {
				width = width / 2 + 20;
				consoleWindow.setLocation(x + width, y);
				consoleWindow.setSize(width - 20, height);
			} else {
				dividerLocation = width / 2 + 20;
			}
		}

		setLocation(x, y);
		setSize(width, height);
		setVisible(true);

		if (!config.getGuiConfig().getConsoleWindow().isDetached()) {
			if (dividerLocation != null && dividerLocation > 0 && dividerLocation < width)
				splitPane.setDividerLocation(dividerLocation);
		} else {
			enableConsoleWindow(true, false);
		}
	}

	private void initConsole() {
		log.setConsoleLogger(consoleLogger);
		System.setOut(consoleLogger.out());
		System.setErr(consoleLogger.err());

		// show console window if required
		if (config.getGuiConfig().getConsoleWindow().isDetached()) {
			enableConsoleWindow(true, false);
			requestFocus();
		}
	}

	public void restoreDefaults() {
		if (consoleWindow.isVisible() != config.getGuiConfig().getConsoleWindow().isDetached())
			enableConsoleWindow(config.getGuiConfig().getConsoleWindow().isDetached(), false);

		consoleWindow.setSize(0, 0);
		showWindow();
	}

	public void setLookAndFeel(Theme theme) {
		String laf = GuiUtil.getLaf(theme);
		if (laf.equals(UIManager.getLookAndFeel().getClass().getName())) {
			return;
		}

		EventQueue.invokeLater(() -> {
			try {
				FlatAnimatedLafChange.showSnapshot();

				UIManager.setLookAndFeel(laf);
				if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) {
					UIManager.put("defaultFont", null);
				}

				FlatLaf.updateUI();
				PopupMenuDecorator.getInstance().updateUI();

				config.getGuiConfig().getAppearance().setTheme(theme);
			} catch (Exception e) {
				log.error("Failed to switch to look and feel theme '" + laf + "'.", e);
			} finally {
				SwingUtilities.invokeLater(FlatAnimatedLafChange::hideSnapshotWithAnimation);
			}
		});
	}

	public void doTranslation() {
		try {
			LanguageType lang = config.getGlobalConfig().getLanguage();
			if (lang == currentLang)
				return;

			Locale locale = new Locale(lang.value());
			Language.I18N = ResourceBundle.getBundle("org.citydb.config.i18n.language", locale);
			currentLang = lang;

			setDatabaseStatus(dbPool.isConnected());
			statusText.setText(Language.I18N.getString("main.status.ready.label"));
			consolePane.setTitleAt(0, Language.I18N.getString("main.console.label"));

			// fire translation notification to plugins
			for (Plugin plugin : pluginManager.getPlugins()) {
				if (plugin instanceof ViewExtension)
					((ViewExtension) plugin).switchLocale(locale);
			}

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
	public void errorMessage(String title, Object message) {
		showOptionDialog(this, title, message, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null);
	}

	@Override
	public int warnMessage(String title, Object message) {
		return showOptionDialog(this, title, message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null);
	}

	@Override
	public int showOptionDialog(String title, Object message, int optionType, int messageType) {
		return showOptionDialog(this, title, message, optionType, messageType);
	}

	@Override
	public int showOptionDialog(Component parent, String title, Object message, int optionType, int messageType) {
		return showOptionDialog(parent, title, message, optionType, messageType, null, null);
	}

	@Override
	public int showOptionDialog(Component parent, String title, Object message, int optionType, int messageType, Object[] options, Object initialValue) {
		if (SwingUtilities.isEventDispatchThread()) {
			return JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, null, options, initialValue);
		} else {
			int[] option = new int[1];
			try {
				SwingUtilities.invokeAndWait(() -> option[0] = JOptionPane.showOptionDialog(
						parent, message, title, optionType, messageType, null, null, null));
			} catch (Exception e) {
				option[0] = JOptionPane.CANCEL_OPTION;
			}

			return option[0];
		}
	}

	public void enableConsoleWindow(boolean enable, boolean resizeMain) {
		splitPane.setEnabled(!enable);

		if (enable) {
			if (resizeMain) {
				consoleWidth = console.getWidth();
				setSize(getWidth() - consoleWidth, getHeight());
			}

			splitPane.setDividerSize(0);
			consoleWindow.activate();
		} else {
			consoleWindow.dispose();

			int dividerLocation = splitPane.getDividerLocation();
			if (dividerLocation <= 0) {
				dividerLocation = getWidth();
			}

			if (resizeMain) {
				int width = consoleWidth != 0 ? consoleWidth : consoleWindow.getWidth();
				setSize(getWidth() + width, getHeight());
			}

			int dividerSize = UIManager.getInt("SplitPane.dividerSize");
			splitPane.setDividerSize(dividerSize > 0 ? dividerSize : 5);
			splitPane.setRightComponent(console);
			splitPane.setDividerLocation(dividerLocation);
		}

		config.getGuiConfig().getConsoleWindow().setDetached(enable);
	}

	public boolean saveSettings() {
		return saveSettings(false);
	}

	private boolean saveSettings(boolean isShuttingDown) {
		if (!createConfigDir(configFile.getParent()))
			return false;

		try {
			ConfigUtil.getInstance().marshal(config.getProjectConfig(), configFile.toFile());
			return true;
		} catch (JAXBException e) {
			if (!isShuttingDown) {
				errorMessage(Language.I18N.getString("common.dialog.error.io.title"),
						Language.I18N.getString("common.dialog.error.io.general"));
			} else {
				log.error("Failed to write configuration file.", e);
			}
			return false;
		}
	}

	private void saveGUISettings() {
		Path guiConfigFile = CoreConstants.IMPEXP_DATA_DIR
				.resolve(CliConstants.CONFIG_DIR)
				.resolve(CliConstants.GUI_SETTINGS_FILE);

		if (!createConfigDir(guiConfigFile.getParent()))
			return;

		// set window size
		Rectangle rect = getBounds();
		MainWindow window = config.getGuiConfig().getMainWindow();
		window.getSize().setX(rect.x);
		window.getSize().setY(rect.y);
		window.getSize().setWidth(rect.width);
		window.getSize().setHeight(rect.height);
		window.setDividerLocation(splitPane.getDividerLocation());

		// set console window size
		consoleWindow.setSettings();

		try {
			ConfigUtil.getInstance().marshal(config.getGuiConfig(), guiConfigFile.toFile());
		} catch (JAXBException e) {
			log.error("Failed to write GUI configuration file.", e);
		}
	}

	private boolean createConfigDir(Path dir) {
		if (!Files.exists(dir)) {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				String text = Language.I18N.getString("common.dialog.error.io.configPath");
				errorMessage(Language.I18N.getString("common.dialog.error.io.title"), MessageFormat.format(text, dir.toString()));
				return false;
			}
		}

		return true;
	}

	@Override
	public void setStatusText(String message) {
		SwingUtilities.invokeLater(() -> statusText.setText(message));
	}

	@Override
	public void clearConsole() {
		SwingUtilities.invokeLater(() -> consoleText.setText(""));
	}

	@Override
	public void setDefaultStatus() {
		SwingUtilities.invokeLater(() -> statusText.setText(Language.I18N.getString("main.status.ready.label")));
	}

	@Override
	public JFrame getTopFrame() {
		return this;
	}

	public ConsoleTextPane getConsole() {
		return consoleText;
	}

	public StyledConsoleLogger getStyledConsoleLogger() {
		return consoleLogger;
	}

	public Path getConfigFile() {
		return configFile;
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
			connectText.setText(MessageFormat.format(text, dbPool.getActiveDatabaseAdapter().getDatabaseType().toString()));
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
			log.setConsoleLogger(new DefaultConsoleLogger());

			eventDispatcher.shutdownNow();
			consoleWindow.dispose();

			// shutdown plugins
			if (!pluginManager.getExternalPlugins().isEmpty())
				log.info("Shutting down plugins");

			for (Plugin plugin : pluginManager.getPlugins())
				plugin.shutdown();

			log.info("Saving project settings");
			saveSettings(true);
			saveGUISettings();

			if (dbPool.isConnected()) {
				log.info("Terminating database connection");
				dbPool.disconnect();
			}

			log.info("Application successfully terminated");
		} catch (Throwable e) {
			log.logStackTrace(e);
			log.info("Application did not terminate normally");
		} finally {
			log.close();
		}
	}

	@Override
	public void handleEvent(Event event) {
		setDatabaseStatus(((DatabaseConnectionStateEvent)event).isConnected());
	}

	private final class ConsolePopupMenuWrapper {
		private final JMenuItem clear;
		private final JMenuItem detach;

		ConsolePopupMenuWrapper(JPopupMenu popupMenu) {
			clear = new JMenuItem();
			detach = new JMenuItem();

			popupMenu.addSeparator();
			popupMenu.add(clear);
			popupMenu.addSeparator();
			popupMenu.add(detach);

			clear.addActionListener(e -> clearConsole());
			detach.addActionListener(e -> enableConsoleWindow(!config.getGuiConfig().getConsoleWindow().isDetached(), true));

			popupMenu.addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					clear.setEnabled(consoleText.getDocument().getLength() != 0);
					detach.setText(config.getGuiConfig().getConsoleWindow().isDetached() ?
							Language.I18N.getString("console.label.attach") :
							Language.I18N.getString("console.label.detach"));
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { }

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) { }
			});

		}

		private void doTranslation() {
			clear.setText(Language.I18N.getString("main.console.popup.clear"));
			detach.setText(config.getGuiConfig().getConsoleWindow().isDetached() ?
					Language.I18N.getString("console.label.attach") :
					Language.I18N.getString("console.label.detach"));
		}
	}
}
