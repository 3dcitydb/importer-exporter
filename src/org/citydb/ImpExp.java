/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb;

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

import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.global.GlobalEvents;
import org.citydb.api.plugin.Plugin;
import org.citydb.api.plugin.extension.config.ConfigExtension;
import org.citydb.api.plugin.extension.config.PluginConfig;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.cmd.ImpExpCmd;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.controller.PluginConfigControllerImpl;
import org.citydb.config.gui.Gui;
import org.citydb.config.internal.Internal;
import org.citydb.config.language.Language;
import org.citydb.config.project.Project;
import org.citydb.config.project.global.LanguageType;
import org.citydb.config.project.global.Logging;
import org.citydb.database.DatabaseControllerImpl;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.SplashScreen;
import org.citydb.io.DirectoryScanner;
import org.citydb.io.IOControllerImpl;
import org.citydb.io.InternalProxySelector;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import org.citydb.modules.citygml.importer.CityGMLImportPlugin;
import org.citydb.modules.database.DatabasePlugin;
import org.citydb.modules.kml.KMLExportPlugin;
import org.citydb.modules.preferences.PreferencesPlugin;
import org.citydb.plugin.IllegalPluginEventChecker;
import org.citydb.plugin.PluginService;
import org.citydb.plugin.PluginServiceFactory;
import org.citydb.util.gui.OSXAdapter;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

public class ImpExp {

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

	@Option(name="-kmlExport", usage="export KML/COLLADA/glTF data to this file\n(shell version only)", metaVar="fileName")
	private String kmlExportFile;

	@Option(name="-testConnection", usage="test whether a database connection can be established")
	private boolean testConnection;

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
		CmdLineParser parser = new CmdLineParser(this, ParserProperties.defaults().withUsageWidth(80));

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
			if (testConnection)
				++commands;

			if (commands == 0) {
				System.out.println("Choose either command \"-import\", \"-export\", \"-kmlExport\", \"-validate\" or \"testConnection\" for shell version");
				printUsage(parser, System.out);
				System.exit(1);
			}

			if (commands > 1) {
				System.out.println("Commands \"-import\", \"-export\", \"-kmlExport\", \"-validate\" and \"testConnection\" may not be mixed");
				printUsage(parser, System.out);
				System.exit(1);
			}
		}

		// initialize look&feel and splash screen
		if (!shell) {
			setLookAndFeel();

			if (!noSplash) {
				useSplashScreen = true;
				splashScreen = new SplashScreen(4, 3, 480, Color.BLACK);
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
		File projectFile = new File(confPath, projectFileName);
		
		// with v3.3, the config path has been changed to not include the version number.
		// if the project file cannot be found, we thus check the old path used in v3.0 to v3.2
		if (!projectFile.exists()) {
			File oldConfPath = new File(Internal.USER_PATH + "-3.0", "config");
			File oldProjectFile = new File(oldConfPath, projectFileName);
			if (oldProjectFile.exists()) {
				LOG.warn("Failed to read project settings file '" + projectFile + '\'');
				LOG.warn("Loading settings from previous file '" + oldProjectFile + "\' instead");
				projectFile = oldProjectFile;
			}
		}
		
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
			File guiFile = new File(confPath, config.getInternal().getConfigGui());

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

			String logPath = logging.getFile().isSetUseAlternativeLogPath() ? logging.getFile().getAlternativeLogPath() : Internal.DEFAULT_LOG_PATH;

			boolean success = LOG.appendLogFile(logPath, true);
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
		if (lang == null)
			lang = LanguageType.fromValue(System.getProperty("user.language"));

		if (!Language.existsLanguagePack(new Locale(lang.value())))
			lang = LanguageType.EN;
		
		Language.I18N = ResourceBundle.getBundle("org.citydb.config.language.Label", new Locale(lang.value()));
		config.getProject().getGlobal().setLanguage(lang);

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

		else {
			ImpExpCmd cmd = new ImpExpCmd(jaxbBuilder, kmlContext, colladaContext, config);
			if (validateFile != null)
				cmd.doValidate(validateFile);
			else if (importFile != null)
				cmd.doImport(importFile);
			else if (exportFile != null) {
				config.getInternal().setExportFileName(exportFile);
				cmd.doExport();
			} else if (kmlExportFile != null) {
				config.getInternal().setExportFileName(kmlExportFile);
				cmd.doKmlExport();
			} else if (testConnection) {
				boolean success = cmd.doTestConnection();
				if (!success)
					System.exit(1);
			}
		}
	}

	private void setLookAndFeel() {
		try {
			// set look & feel
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			if (OSXAdapter.IS_MAC_OS_X) {
				OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(ImpExp.class.getResource("/resources/img/common/logo_small.png")));
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}

		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
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
		out.println("Usage: java -jar lib/3dcitydb-impexp.jar [-options]");
		out.println("            (default: to execute gui version)");
		out.println("   or  java -jar lib/3dcitydb-impexp.jar -shell [-command] [-options]");
		out.println("            (to execute shell version)");
		out.println();
		out.println("where options include:");
		parser.printUsage(out);
		out.println();
	}

}
