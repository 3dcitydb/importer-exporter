/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ProxySelector;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;

import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.plugin.Plugin;
import de.tub.citydb.api.plugin.extension.config.ConfigExtension;
import de.tub.citydb.api.plugin.extension.config.PluginConfig;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.cmd.ImpExpCmd;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.ConfigUtil;
import de.tub.citydb.config.controller.PluginConfigControllerImpl;
import de.tub.citydb.config.gui.Gui;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.Project;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.config.project.global.Logging;
import de.tub.citydb.database.DatabaseControllerImpl;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.SplashScreen;
import de.tub.citydb.io.DirectoryScanner;
import de.tub.citydb.io.IOControllerImpl;
import de.tub.citydb.io.InternalProxySelector;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import de.tub.citydb.modules.citygml.importer.CityGMLImportPlugin;
import de.tub.citydb.modules.database.DatabasePlugin;
import de.tub.citydb.modules.kml.KMLExportPlugin;
import de.tub.citydb.modules.preferences.PreferencesPlugin;
import de.tub.citydb.plugin.IllegalPluginEventChecker;
import de.tub.citydb.plugin.PluginService;
import de.tub.citydb.plugin.PluginServiceFactory;
import de.tub.citydb.util.gui.OSXAdapter;

public class ImpExp {

	// set look & feel
	static {
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			if (OSXAdapter.IS_MAC_OS_X) {
				OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(ImpExp.class.getResource("/resources/img/common/logo_small.png")));
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Option(name="-config", usage="config file containing project settings", metaVar="fileName")
	private File configFile;

	@Option(name="-version", usage="print product version and exit")
	private boolean version;

	@Option(name="-h", aliases={"-help"}, usage="print this help message and exit")
	private boolean help;

	@Option(name="-shell", usage="to execute in a shell environment,\nwithout graphical user interface")
	private boolean shell;

	@Option(name="-import", usage="a ; separated list of directories and files to import,\nwildcards allowed\n(shell version only)", metaVar="fileName[s]")
	private String importFile;

	@Option(name="-validate", usage="a ; separated list of directories and files to\nvalidate, wildcards allowed\n(shell version only)", metaVar="fileName[s]")
	private String validateFile;

	@Option(name="-export", usage="export data to this file\n(shell version only)", metaVar="fileName")
	private String exportFile;

	@Option(name="-kmlExport", usage="export KML/COLLADA data to this file\n(shell version only)", metaVar="fileName")
	private String kmlExportFile;

	@Option(name="-noSplash")
	private boolean noSplash;

	private final Logger LOG = Logger.getInstance();
	private JAXBBuilder jaxbBuilder;
	private JAXBContext kmlContext, colladaContext, projectContext, guiContext;
	private PluginService pluginService;
	private Config config;

	private SplashScreen splashScreen;
	private boolean useSplashScreen;	

	private List<String> errMsgs = new ArrayList<String>();

	public static void main(String[] args) {
		new ImpExp().doMain(args);
	}

	@SuppressWarnings("unused")
	private void doMain(String[] args, Plugin[] plugins) {
		if (plugins != null) {
			PluginService pluginService = PluginServiceFactory.getPluginService();
			for (Plugin plugin : plugins)
				pluginService.registerExternalPlugin(plugin);
		}

		doMain(args);
	}

	private void doMain(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);

		try {
			parser.parseArgument(args);			
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printUsage(parser, System.err);
			System.exit(1);
		}

		if (help) {
			printUsage(parser, System.out);
			System.exit(0);
		}

		if (version) {
			System.out.println(
					this.getClass().getPackage().getImplementationTitle() + ", version \"" +
					this.getClass().getPackage().getImplementationVersion() + "\"");
			System.out.println(this.getClass().getPackage().getImplementationVendor());
			System.exit(0);			
		}

		if (shell) {
			byte commands = 0;

			if (validateFile != null)
				++commands;
			if (importFile != null)
				++commands;
			if (exportFile != null)
				++commands;
			if (kmlExportFile != null)
				++commands;

			if (commands == 0) {
				System.out.println("Choose either command \"-import\", \"-export\", \"-kmlExport\" or \"-validate\" for shell version");
				printUsage(parser, System.out);
				System.exit(1);
			}

			if (commands > 1) {
				System.out.println("Commands \"-import\", \"-export\", \"-kmlExport\" and \"-validate\" may not be mixed");
				printUsage(parser, System.out);
				System.exit(1);
			}
		}

		// initialize splash screen
		useSplashScreen = !shell && !noSplash;
		if (useSplashScreen) {
			splashScreen = new SplashScreen(4, 3, 50, Color.BLACK);
			splashScreen.setMessage("Version \"" + this.getClass().getPackage().getImplementationVersion() + "\"");

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					splashScreen.setVisible(true);
				}
			});		

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
		}

		LOG.info("Starting " +
				this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + "\"");

		// load external plugins
		printInfoMessage("Loading plugins");

		try {			
			DirectoryScanner directoryScanner = new DirectoryScanner(true);
			directoryScanner.addFilenameFilter(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toUpperCase().endsWith(".JAR");
				}
			});

			for (File file : directoryScanner.getFiles(new File(Internal.PLUGINS_PATH)))
				PluginServiceFactory.addPluginDirectory(file.getParentFile());

			pluginService = PluginServiceFactory.getPluginService();
			pluginService.loadPlugins();
		} catch (IOException e) {
			LOG.error("Failed to initialize plugin support: " + e.getMessage());
			System.exit(1);
		} catch (ServiceConfigurationError e) {
			LOG.error("Failed to load plugin: " + e.getMessage());
			System.exit(1);				
		}

		// get plugin config classes
		List<Class<?>> projectConfigClasses = new ArrayList<Class<?>>();
		projectConfigClasses.add(Project.class);
		for (ConfigExtension<? extends PluginConfig> plugin : pluginService.getExternalConfigExtensions()) {
			try {
				projectConfigClasses.add(plugin.getClass().getMethod("getConfig", new Class<?>[]{}).getReturnType());
			} catch (SecurityException e) {
				LOG.error("Failed to instantiate config for plugin '" + plugin.getClass().getCanonicalName() + "'.");
				LOG.error("Please check the following error message: " + e.getMessage());
				System.exit(1);
			} catch (NoSuchMethodException e) {
				LOG.error("Failed to instantiate config for plugin '" + plugin.getClass().getCanonicalName() + "'.");
				LOG.error("Please check the following error message: " + e.getMessage());
				System.exit(1);
			}
		}	
		
		// initialize application environment
		printInfoMessage("Initializing application environment");
		config = new Config();
		
		// initialize object registry
		ObjectRegistry registry = ObjectRegistry.getInstance();
		
		// register log controller
		registry.setLogController(Logger.getInstance());
		
		// create and register application-wide event dispatcher
		EventDispatcher eventDispatcher = new EventDispatcher();		
		registry.setEventDispatcher(eventDispatcher);
		
		// create and register plugin config controller
		PluginConfigControllerImpl pluginConfigController = new PluginConfigControllerImpl(config);
		registry.setPluginConfigController(pluginConfigController);

		// create and register database controller
		DatabaseControllerImpl databaseController = new DatabaseControllerImpl(config);
		registry.setDatabaseController(databaseController);
		
		// create and register i/o controller
		IOControllerImpl ioController = new IOControllerImpl(config);
		registry.setIOController(ioController);

		// register illegal plugin event checker with event dispatcher
		IllegalPluginEventChecker checker = IllegalPluginEventChecker.getInstance();
		eventDispatcher.addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, checker);
		eventDispatcher.addEventHandler(GlobalEvents.SWITCH_LOCALE, checker);
		
		// set internal proxy selector as default
		ProxySelector.setDefault(InternalProxySelector.getInstance(config));

		// create JAXB contexts
		try {
			// create citygml4j builder and register in object registry
			jaxbBuilder = new JAXBBuilder();
			registry.setCityGMLBuilder(jaxbBuilder);
			
			kmlContext = JAXBContext.newInstance("net.opengis.kml._2", Thread.currentThread().getContextClassLoader());
			colladaContext = JAXBContext.newInstance("org.collada._2005._11.colladaschema", Thread.currentThread().getContextClassLoader());
			projectContext = JAXBContext.newInstance(projectConfigClasses.toArray(new Class<?>[]{}));
			guiContext = JAXBContext.newInstance(Gui.class);
		} catch (JAXBException e) {
			LOG.error("Application environment could not be initialized. Please check the following stack trace.");
			LOG.error("Aborting...");
			e.printStackTrace();
			System.exit(1);
		}

		// initialize config
		printInfoMessage("Loading project settings");		
		String confPath = null;
		String projectFileName = null;

		if (configFile != null) {
			if (!configFile.exists()) {
				LOG.error("Failed to find config file '" + configFile + "'");
				LOG.error("Aborting...");
				System.exit(1);
			} else if (!configFile.canRead() || !configFile.canWrite()) {
				LOG.error("Insufficient access rights to config file '" + configFile + "'");
				LOG.error("Aborting...");
				System.exit(1);
			}

			projectFileName = configFile.getName();
			confPath = configFile.getParent();
			if (confPath == null)
				confPath = System.getProperty("user.home");
		} else {
			confPath = config.getInternal().getConfigPath();
			projectFileName = config.getInternal().getConfigProject();
		}

		config.getInternal().setConfigPath(confPath);
		config.getInternal().setConfigProject(projectFileName);
		File projectFile = new File(confPath + File.separator + projectFileName);
		Project configProject = config.getProject();

		try {
			Object object = ConfigUtil.unmarshal(projectFile, projectContext);

			if (!(object instanceof Project)) {
				String errMsg = "Failed to read project settings file '" + projectFile + '\'';
				if (shell) {
					LOG.error(errMsg);
					LOG.error("Aborting...");
					System.exit(1);
				} else
					errMsgs.add(errMsg);
			} else
				configProject = (Project)object;

		} catch (IOException fne) {
			String errMsg = "Failed to read project settings file '" + projectFile + '\'';
			if (shell) {
				LOG.error(errMsg);
				LOG.error("Aborting...");
				System.exit(1);
			} else
				errMsgs.add(errMsg);
		} catch (JAXBException jaxbE) {
			String errMsg = "Project settings '" + projectFile + "' could not be loaded: " + jaxbE.getMessage();
			if (shell) {
				LOG.error(errMsg);
				LOG.error("Aborting...");
				System.exit(1);
			} else
				errMsgs.add(errMsg);
		} finally {
			config.setProject(configProject);
		}

		if (!shell) {
			File guiFile = new File(confPath + File.separator + config.getInternal().getConfigGui());

			try {
				Gui configGui = null;
				Object object = ConfigUtil.unmarshal(guiFile, guiContext);

				if (object instanceof Gui)
					configGui = (Gui)object;

				config.setGui(configGui);
			} catch (JAXBException jaxbE) {
				//
			} catch (IOException ioE) {
				//
			}
		}

		// init logging environment
		Logging logging = config.getProject().getGlobal().getLogging();
		LOG.setDefaultConsoleLogLevel(logging.getConsole().getLogLevel());
		if (logging.getFile().isSet()) {
			LOG.setDefaultFileLogLevel(logging.getFile().getLogLevel());

			if (logging.getFile().isSetUseAlternativeLogPath() &&
					logging.getFile().getAlternativeLogPath().trim().length() == 0)
				logging.getFile().setUseAlternativeLogPath(false);

			String logPath = logging.getFile().isSetUseAlternativeLogPath() ? logging.getFile().getAlternativeLogPath() : 
				config.getInternal().getLogPath();

			boolean success = LOG.appendLogFile(logPath);
			if (!success) {
				logging.getFile().setActive(false);
				logging.getFile().setUseAlternativeLogPath(false);
				LOG.detachLogFile();
			} else {
				Calendar cal = Calendar.getInstance();
				DecimalFormat df = new DecimalFormat("00");

				int m = cal.get(Calendar.MONTH) + 1;
				int d = cal.get(Calendar.DATE);
				int y = cal.get(Calendar.YEAR);

				StringBuilder date = new StringBuilder();
				date.append(y);
				date.append('-');
				date.append(df.format(m));
				date.append('-');
				date.append(df.format(d));

				LOG.writeToFile("*** Starting new log file session on " + date.toString());
				config.getInternal().setCurrentLogPath(logPath);
			}
		}

		// printing shell command to log file
		if (logging.getFile().isSet()) {
			StringBuilder msg = new StringBuilder("*** Command line arguments: ");
			if (args.length == 0)
				msg.append("no arguments passed");
			else 
				for (String arg : args) {
					msg.append(arg);
					msg.append(' ');
				}

			LOG.writeToFile(msg.toString());
		}

		// init internationalized labels 
		LanguageType lang = config.getProject().getGlobal().getLanguage();
		if (lang == null) {
			lang = LanguageType.fromValue(System.getProperty("user.language"));
			config.getProject().getGlobal().setLanguage(lang);
		}

		Internal.I18N = ResourceBundle.getBundle("de.tub.citydb.gui.Label", new Locale(lang.value()));

		// start application
		if (!shell) {
			// create main view instance
			final ImpExpGui mainView = new ImpExpGui(config);
			registry.setViewController(mainView);

			// create database plugin
			final DatabasePlugin databasePlugin = new DatabasePlugin(config, mainView);
			databaseController.setConnectionViewHandler(databasePlugin.getConnectionViewHandler());

			// propogate config to plugins
			for (ConfigExtension<? extends PluginConfig> plugin : pluginService.getExternalConfigExtensions())
				pluginConfigController.setOrCreatePluginConfig(plugin);

			// initialize plugins
			for (Plugin plugin : pluginService.getExternalPlugins()) {
				LOG.info("Initializing plugin " + plugin.getClass().getName());
				if (useSplashScreen)
					splashScreen.setMessage("Initializing plugin " + plugin.getClass().getName());

				plugin.init(new Locale(lang.value()));
			}

			// register internal plugins
			pluginService.registerInternalPlugin(new CityGMLImportPlugin(jaxbBuilder, config, mainView));		
			pluginService.registerInternalPlugin(new CityGMLExportPlugin(jaxbBuilder, config, mainView));		
			pluginService.registerInternalPlugin(new KMLExportPlugin(kmlContext, colladaContext, config, mainView));
			pluginService.registerInternalPlugin(databasePlugin);
			pluginService.registerInternalPlugin(new PreferencesPlugin(pluginService, config, mainView));

			// initialize internal plugins
			for (Plugin plugin : pluginService.getInternalPlugins())
				plugin.init(new Locale(lang.value()));

			// initialize gui
			printInfoMessage("Starting graphical user interface");

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					mainView.invoke(projectContext,
							guiContext,
							pluginService,
							errMsgs);
				}
			});

			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
				//
			}

			if (useSplashScreen)
				splashScreen.close();

			return;
		}	

		if (validateFile != null) {
			new Thread() {
				public void run() {
					new ImpExpCmd(jaxbBuilder, config).doValidate(validateFile);
				}
			}.start();

			return;
		}

		if (importFile != null) {
			new Thread() {
				public void run() {
					new ImpExpCmd(jaxbBuilder, config).doImport(importFile);
				}
			}.start();

			return;
		}

		if (exportFile != null) {
			config.getInternal().setExportFileName(exportFile);

			new Thread() {
				public void run() {
					new ImpExpCmd(jaxbBuilder, config).doExport();
				}
			}.start();

			return;
		}

		if (kmlExportFile != null) {
			config.getInternal().setExportFileName(kmlExportFile);

			new Thread() {
				public void run() {
					new ImpExpCmd(kmlContext,
							colladaContext,
							config).doKmlExport();
				}
			}.start();

			return;
		}
	}

	private void printInfoMessage(String message) {
		LOG.info(message);
		if (useSplashScreen) {
			splashScreen.setMessage(message);
			splashScreen.nextStep();
		}
	}

	private void printUsage(CmdLineParser parser, PrintStream out) {
		out.println("Usage: java -jar 3dcitydb-impexp.jar [-options]");
		out.println("            (default: to execute gui version)");
		out.println("   or  java -jar 3dcitydb-impexp.jar -shell [-command] [-options]");
		out.println("            (to execute shell version)");
		out.println();
		out.println("where options include:");
		parser.printUsage(System.out);
		out.println();
	}

}
