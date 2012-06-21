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
package de.tub.citydb.gui;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.tub.citydb.api.controller.ViewController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.event.global.ViewEvent;
import de.tub.citydb.api.event.global.ViewEvent.ViewState;
import de.tub.citydb.api.gui.ComponentFactory;
import de.tub.citydb.api.plugin.Plugin;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.ConfigUtil;
import de.tub.citydb.config.gui.window.MainWindow;
import de.tub.citydb.config.gui.window.WindowSize;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.event.SwitchLocaleEventImpl;
import de.tub.citydb.gui.console.ConsoleWindow;
import de.tub.citydb.gui.factory.DefaultComponentFactory;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.menubar.MenuBar;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import de.tub.citydb.modules.citygml.importer.CityGMLImportPlugin;
import de.tub.citydb.modules.database.DatabasePlugin;
import de.tub.citydb.modules.kml.KMLExportPlugin;
import de.tub.citydb.modules.preferences.PreferencesPlugin;
import de.tub.citydb.plugin.PluginService;
import de.tub.citydb.util.gui.GuiUtil;
import de.tub.citydb.util.gui.OSXAdapter;

@SuppressWarnings("serial")
public final class ImpExpGui extends JFrame implements ViewController, EventHandler {
	private final Logger LOG = Logger.getInstance();
	private final EventDispatcher eventDispatcher; 

	private Config config;
	private JAXBContext jaxbProjectContext;
	private JAXBContext jaxbGuiContext;
	private PluginService pluginService;
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
	private JTextArea consoleText;
	
	private int tmpConsoleWidth;
	private int activePosition;

	private List<View> views;
	private PreferencesPlugin preferencesPlugin;
	private DatabasePlugin databasePlugin;

	private PrintStream out;
	private PrintStream err;

	// internal state
	private LanguageType currentLang = null;

	public ImpExpGui(Config config) {
		dbPool = DatabaseConnectionPool.getInstance();
		this.config = config;

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);

		// required for preferences plugin
		consoleText = new JTextArea();
	}

	public void invoke(JAXBContext jaxbProjectContext,
			JAXBContext jaxbGuiContext,
			PluginService pluginService,
			List<String> errMsgs) {		
		this.jaxbProjectContext = jaxbProjectContext;
		this.jaxbGuiContext = jaxbGuiContext;
		this.pluginService = pluginService;

		// init GUI elements
		initGui();
		doTranslation();
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

		menuBar = new MenuBar(pluginService, config, jaxbProjectContext, this);
		setJMenuBar(menuBar);

		console = new JPanel();
		consoleLabel = new JLabel();
		consoleText.setAutoscrolls(true);
		consoleText.setFont(new Font(Font.MONOSPACED, 0, 11));
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
		views = new ArrayList<View>();
		preferencesPlugin = pluginService.getInternalPlugin(PreferencesPlugin.class);
		databasePlugin = pluginService.getInternalPlugin(DatabasePlugin.class);
		views.add(pluginService.getInternalPlugin(CityGMLImportPlugin.class).getView());
		views.add(pluginService.getInternalPlugin(CityGMLExportPlugin.class).getView());
		views.add(pluginService.getInternalPlugin(KMLExportPlugin.class).getView());

		for (ViewExtension viewExtension : pluginService.getExternalViewExtensions())
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
				OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("shutdown", (Class[])null));
				OSXAdapter.setAboutHandler(menuBar, menuBar.getClass().getDeclaredMethod("printInfo", (Class[])null));
				OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("showPreferences", (Class[])null));
			} catch (SecurityException e) {
				//
			} catch (NoSuchMethodException e) {
				//
			}
		}

		//layout
		setIconImage(Toolkit.getDefaultToolkit().getImage(ImpExpGui.class.getResource("/resources/img/common/logo_small.png")));
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
		Charset encoding;

		try {
			encoding = Charset.forName("UTF-8");
		} catch (Exception e) {
			encoding = Charset.defaultCharset();
		}

		// let standard out point to console
		JTextAreaOutputStream consoleWriter = new JTextAreaOutputStream(consoleText, new ByteArrayOutputStream(), encoding);
		PrintStream writer;

		try {
			writer = new PrintStream(consoleWriter, true, encoding.displayName());
		} catch (UnsupportedEncodingException e) {
			writer = new PrintStream(consoleWriter);
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

	public void doTranslation () {
		try {
			LanguageType lang = config.getProject().getGlobal().getLanguage();
			if (lang == currentLang)
				return;

			Locale locale = new Locale(lang.value());
			Internal.I18N = ResourceBundle.getBundle("de.tub.citydb.gui.Label", locale);
			currentLang = lang;

			setDatabaseStatus(dbPool.isConnected());
			statusText.setText(Internal.I18N.getString("main.status.ready.label"));
			consoleLabel.setText(Internal.I18N.getString("main.console.label"));

			// fire translation notification to plugins
			for (Plugin plugin : pluginService.getPlugins())
				plugin.switchLocale(locale);

			int index = 0;
			for (View view : views)
				menu.setTitleAt(index++, view.getLocalizedTitle());

			menuBar.doTranslation();
			consolePopup.doTranslation();

			eventDispatcher.triggerSyncEvent(new SwitchLocaleEventImpl(locale, this));
		} catch (MissingResourceException e) {
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
		String configPath = ConfigUtil.createConfigPath(config.getInternal().getConfigPath());

		if (configPath == null) {
			String text = Internal.I18N.getString("common.dialog.error.io.configPath");
			Object[] args = new Object[]{ config.getInternal().getConfigPath() };
			String result = MessageFormat.format(text, args);

			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), result);
			return false;
		}

		File projectFile = new File(configPath + File.separator + config.getInternal().getConfigProject());

		try {
			ConfigUtil.marshal(config.getProject(), projectFile, jaxbProjectContext);
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

		File guiFile = new File(configPath + File.separator + config.getInternal().getConfigGui());

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
			ConfigUtil.marshal(config.getGui(), guiFile, jaxbGuiContext);
		} catch (JAXBException jaxbE) {
			errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					Internal.I18N.getString("common.dialog.error.io.general"));
			return false;
		}

		return true;
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
		statusText.setText(Internal.I18N.getString("main.status.ready.label"));
	}

	@Override
	public JFrame getTopFrame() {
		return this;
	}

	@Override
	public ComponentFactory getComponentFactory() {
		return DefaultComponentFactory.getInstance(config);
	}

	public JTextArea getConsole() {
		return consoleText;
	}

	public void connectToDatabase() {
		try {
			ObjectRegistry.getInstance().getDatabaseController().connect(true);
		} catch (DatabaseConfigurationException e) {
			//
		} catch (SQLException e) {
			//
		}
	}

	public void disconnectFromDatabase() {
		try {
			ObjectRegistry.getInstance().getDatabaseController().disconnect(true);
		} catch (SQLException e) {
			//
		}
	}

	private void setDatabaseStatus(boolean isConnected) {
		if (!isConnected) {
			setTitle(Internal.I18N.getString("main.window.title"));
			connectText.setText(Internal.I18N.getString("main.status.database.disconnected.label"));
		} else {
			setTitle(Internal.I18N.getString("main.window.title") + " : " + dbPool.getActiveConnection().getDescription());
			connectText.setText(Internal.I18N.getString("main.status.database.connected.label"));
		}
	}

	public void showPreferences() {
		// preferences handler for Mac OS X
		menu.setSelectedIndex(menu.indexOfComponent(preferencesPlugin.getView().getViewComponent()));
	}

	public void shutdown() {		
		System.setOut(out);
		System.setErr(err);
		boolean success = true;

		consoleWindow.dispose();

		// shutdown plugins
		if (!pluginService.getExternalPlugins().isEmpty())
			LOG.info("Shutting down plugins");

		for (Plugin plugin : pluginService.getPlugins())
			plugin.shutdown();

		LOG.info("Saving project settings");
		saveProjectSettings();
		saveGUISettings();

		if (dbPool.isConnected()) {
			LOG.info("Terminating database connection");
			try {
				dbPool.disconnect();
			} catch (SQLException e) {
				LOG.error("Failed to terminate database connection: " + e.getMessage());
				success = false;
			}
		}

		if (success)
			LOG.info("Application successfully terminated");
		else {
			LOG.info("Application did not terminate normally");
			System.exit(1);
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		setDatabaseStatus(((DatabaseConnectionStateEvent)event).isConnected());
	}
	
	private class JTextAreaOutputStream extends FilterOutputStream {
		private final int MAX_DOC_LENGTH = 10000;
		private final JTextArea ta;
		private final Charset encoding;

		public JTextAreaOutputStream (JTextArea ta, OutputStream stream, Charset encoding) {
			super(stream);
			this.ta = ta;
			this.encoding = encoding;
		}

		@Override
		public void write(final byte[] b) {
			try {
				ta.append(new String(b, encoding));
			} catch (Error e) {
				//
			}

			flush();
		}

		@Override
		public void write(final byte b[], final int off, final int len) {
			try {
				ta.append(new String(b, off, len, encoding));
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
	
	private final class ConsolePopupMenuWrapper {
		private JMenuItem clear;	

		public ConsolePopupMenuWrapper(JPopupMenu popupMenu) {
			clear = new JMenuItem();	

			popupMenu.addSeparator();
			popupMenu.add(clear);

			clear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearConsole();
				}
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
			clear.setText(Internal.I18N.getString("main.console.popup.clear"));
		}
	}
}
