/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.event.global.SwitchLocaleEvent;
import org.citydb.gui.components.console.ConsoleTextPane;
import org.citydb.gui.components.console.ConsoleWindow;
import org.citydb.gui.components.console.StyledConsoleLogger;
import org.citydb.gui.components.menubar.MenuBar;
import org.citydb.gui.factory.DefaultComponentFactory;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.log.DefaultConsoleLogger;
import org.citydb.log.Logger;
import org.citydb.gui.modules.exporter.CityGMLExportPlugin;
import org.citydb.gui.modules.importer.CityGMLImportPlugin;
import org.citydb.gui.modules.database.DatabasePlugin;
import org.citydb.gui.modules.kml.KMLExportPlugin;
import org.citydb.gui.modules.preferences.PreferencesPlugin;
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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

@SuppressWarnings("serial")
public final class ImpExpGui extends JFrame implements ViewController, EventHandler {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final Path configFile;
	private final PluginManager pluginManager;
	private final DatabaseConnectionPool dbPool;
	private final EventDispatcher eventDispatcher;
	private final ConsoleTextPane consoleText;
	private final StyledConsoleLogger consoleLogger;
	private final ComponentFactory componentFactory;
	private final PrintStream out = System.out;
	private final PrintStream err = System.err;

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

	private int tmpConsoleWidth;
	private int activePosition;
	private List<View> views;
	private PreferencesPlugin preferencesPlugin;
	private LanguageType currentLang;

	public ImpExpGui(Path configFile) {
		this.configFile = Objects.requireNonNull(configFile, "configFile cannot be null.");

		config = ObjectRegistry.getInstance().getConfig();
		dbPool = DatabaseConnectionPool.getInstance();
		pluginManager = PluginManager.getInstance();
		componentFactory = new DefaultComponentFactory(this);
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

	public void restoreDefaults() {
		if (consoleWindow.isVisible() != config.getGuiConfig().getConsoleWindow().isDetached())
			enableConsoleWindow(config.getGuiConfig().getConsoleWindow().isDetached(), false);

		consoleWindow.setSize(0, 0);
		showWindow();
	}

	private void initGui() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		activePosition = 0;
		main = new JPanel();

		menuBar = new MenuBar(this, config);
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
		WindowSize size = config.getGuiConfig().getMainWindow().getSize();

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
		Integer dividerLocation = config.getGuiConfig().getMainWindow().getDividerLocation();

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
			if (config.getGuiConfig().getConsoleWindow().isDetached()) {
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

		if (!config.getGuiConfig().getConsoleWindow().isDetached())
			main.setPreferredSize(new Dimension((int)(width * .5) + 20, 1));

		if (dividerLocation != null && dividerLocation > 0 && dividerLocation < width)
			splitPane.setDividerLocation(dividerLocation);
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
			consoleLabel.setText(Language.I18N.getString("main.console.label"));

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

		config.getGuiConfig().getConsoleWindow().setDetached(enable);
	}

	public boolean saveProjectSettings() {
		return saveProjectSettings(false);
	}

	private boolean saveProjectSettings(boolean isShuttingDown) {
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
				.resolve(ClientConstants.CONFIG_DIR)
				.resolve(ClientConstants.GUI_SETTINGS_FILE);

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

	@Override
	public ComponentFactory getComponentFactory() {
		return componentFactory;
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
			saveProjectSettings(true);
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
