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
package org.citydb;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.cli.ImpExpCliOld;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.Gui;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.Project;
import org.citydb.config.project.global.LanguageType;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.global.Logging;
import org.citydb.database.DatabaseController;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SchemaMappingException;
import org.citydb.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.database.schema.util.SchemaMappingUtil;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.EventType;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.SplashScreen;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.CityGMLExportPlugin;
import org.citydb.modules.citygml.importer.CityGMLImportPlugin;
import org.citydb.modules.database.DatabasePlugin;
import org.citydb.modules.kml.KMLExportPlugin;
import org.citydb.modules.preferences.PreferencesPlugin;
import org.citydb.plugin.IllegalEventSourceChecker;
import org.citydb.plugin.InternalPlugin;
import org.citydb.plugin.Plugin;
import org.citydb.plugin.PluginException;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.config.ConfigExtension;
import org.citydb.plugin.extension.view.ViewExtension;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.CoreConstants;
import org.citydb.util.InternalProxySelector;
import org.citydb.util.PidFile;
import org.citydb.util.Util.URLClassLoader;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.citygml.ade.binding.ADEContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class ImpExpOld {

	@Option(name="-config", usage="config file containing project settings", metaVar="fileName")
	private Path configFile;

	@Option(name="-version", aliases={"-v"}, usage="print product version and exit")
	private boolean version;

	@Option(name="-help", aliases={"-h"}, usage="print this help message and exit")
	private boolean help;

	@Option(name="-shell", usage="to execute in a shell environment,\nwithout graphical user interface")
	private boolean shell;

	@Option(name="-import", usage="a ; separated list of directories and files to import,\nwildcards allowed\n(shell version only)", metaVar="fileName[s]")
	private String importFile;

	@Option(name="-validate", usage="a ; separated list of directories and files to\nvalidate, wildcards allowed\n(shell version only)", metaVar="fileName[s]")
	private String validateFile;

	@Option(name="-export", usage="export data to this file\n(shell version only)", metaVar="fileName")
	private String exportFile;
	
	@Option(name="-delete", usage="delete data from database\n(shell version only)")
	private boolean databaseDelete;

	@Option(name="-kmlExport", usage="export KML/COLLADA/glTF data to this file\n(shell version only)", metaVar="fileName")
	private String kmlExportFile;

	@Option(name="-testConnection", usage="test whether a database connection can be established")
	private boolean testConnection;

	@Option(name="-pid-file", usage="create file containing the current process ID", metaVar="fileName")
	private Path pidFile;

	@Option(name="-noSplash")
	private boolean noSplash;

	private final Logger log = Logger.getInstance();
	private JAXBContext kmlContext, colladaContext;
	private PluginManager pluginManager = PluginManager.getInstance();
	private ADEExtensionManager adeManager = ADEExtensionManager.getInstance();

	private SplashScreen splashScreen;
	private boolean useSplashScreen;
	private Map<LogLevel, String> logMessages = new HashMap<>();

	private final int maximumSteps = 7;
	private int currentStep = 1;

	public static void main(String[] args) {
		ImpExpOld impExpOld = new ImpExpOld();

		try {
			impExpOld.doMain(args);
		} catch (ImpExpException e) {
			impExpOld.logErrorAndExit(e);
		}
	}

	public void doMain(String[] args, Plugin... plugins) {
		if (plugins != null) {
			for (Plugin plugin : plugins)
				pluginManager.registerExternalPlugin(plugin);
		}

		try {
			doMain(args);
		} catch (ImpExpException e) {
			logErrorAndExit(e);
		}
	}
	
	public void doMain(String[] args, ADEExtension... extensions) {
		if (extensions != null) {
			for (ADEExtension extension : extensions) {
				if (extension.getBasePath() == null)
					extension.setBasePath(Paths.get("."));

				adeManager.loadExtension(extension);
			}
		}

		try {
			doMain(args);
		} catch (ImpExpException e) {
			logErrorAndExit(e);
		}
	}

	private void doMain(String[] args) throws ImpExpException {
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
			if (databaseDelete)
				++commands;
			if (kmlExportFile != null)
				++commands;
			if (testConnection)
				++commands;

			if (commands == 0) {
				System.out.println("Choose either command \"-import\", \"-export\", \"-delete\", \"-kmlExport\", \"-validate\" or \"-testConnection\" for shell version");
				printUsage(parser, System.out);
				System.exit(1);
			}

			if (commands > 1) {
				System.out.println("Commands \"-import\", \"-export\", \"-delete\", \"-kmlExport\", \"-validate\" and \"-testConnection\" may not be mixed");
				printUsage(parser, System.out);
				System.exit(1);
			}
		} else {
			// initialize look&feel and splash screen
			setLookAndFeel();

			if (!noSplash) {
				useSplashScreen = true;
				splashScreen = new SplashScreen(3, 477, Color.BLACK);
				splashScreen.setMessage("Version \"" + this.getClass().getPackage().getImplementationVersion() + "\"");
				SwingUtilities.invokeLater(() -> splashScreen.setVisible(true));

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//
				}
			}
		}

		log.info("Starting " +
				this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + "\"");

		// load external plugins
		printInfoMessage("Loading plugins");
		URLClassLoader externalLoader = new URLClassLoader(ImpExpOld.class.getClassLoader());
		try {
			Path pluginsDir = ClientConstants.IMPEXP_HOME.resolve(ClientConstants.PLUGINS_DIR);
			if (Files.exists(pluginsDir)) {
				try (Stream<Path> stream = Files.walk(pluginsDir)
						.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))) {
					stream.forEach(externalLoader::addPath);
				}
			}

			pluginManager.loadPlugins(externalLoader);
			for (Plugin plugin : pluginManager.getExternalPlugins())
				log.info("Initializing plugin " + plugin.getClass().getName());

		} catch (IOException e) {
			throw new ImpExpException("Failed to initialize plugin support.", e);
		}

		// get plugin config classes
		for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
			try {
				ConfigUtil.getInstance().withConfigClass(pluginManager.getConfigClass(plugin));
			} catch (PluginException e) {
				throw new ImpExpException("Failed to instantiate config for plugin " + plugin.getClass().getName() + ".", e);
			}
		}

		// initialize application environment
		printInfoMessage("Initializing application environment");
		Config config = new Config();

		// initialize object registry
		ObjectRegistry registry = ObjectRegistry.getInstance();

		// create and register application-wide event dispatcher
		EventDispatcher eventDispatcher = new EventDispatcher();		
		registry.setEventDispatcher(eventDispatcher);

		// create and register database controller
		DatabaseController databaseController = new DatabaseController(config);
		registry.setDatabaseController(databaseController);

		// register illegal plugin event checker with event dispatcher
		IllegalEventSourceChecker checker = IllegalEventSourceChecker.getInstance();
		eventDispatcher.addEventHandler(EventType.DATABASE_CONNECTION_STATE, checker);
		eventDispatcher.addEventHandler(EventType.SWITCH_LOCALE, checker);

		// set internal proxy selector as default
		ProxySelector.setDefault(InternalProxySelector.getInstance(config));

		// create JAXB contexts
		try {
			kmlContext = JAXBContext.newInstance("net.opengis.kml._2", this.getClass().getClassLoader());
			colladaContext = JAXBContext.newInstance("org.collada._2005._11.colladaschema", this.getClass().getClassLoader());
		} catch (JAXBException e) {
			throw new ImpExpException("Application environment could not be initialized.", e);
		}
		
		// read database schema mapping and register with ObjectRegistry
		printInfoMessage("Loading database schema mapping");
		SchemaMapping schemaMapping = null;
		try {
			schemaMapping = SchemaMappingUtil.getInstance().unmarshal(CoreConstants.CITYDB_SCHEMA_MAPPING_FILE);
			registry.setSchemaMapping(schemaMapping);
		} catch (JAXBException | SchemaMappingException | SchemaMappingValidationException e) {
			throw new ImpExpException("Failed to process 3DCityDB schema mapping file.", e);
		}

		// load ADE extensions	
		printInfoMessage("Loading ADE extensions");
		try {
			Path adeExtensionsDir = ClientConstants.IMPEXP_HOME.resolve(ClientConstants.ADE_EXTENSIONS_DIR);
			if (Files.exists(adeExtensionsDir)) {
				try (Stream<Path> stream = Files.walk(adeExtensionsDir)
						.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))) {
					stream.forEach(externalLoader::addPath);
				}
			}

			adeManager.loadExtensions(externalLoader);
			adeManager.loadSchemaMappings(schemaMapping);

			for (ADEExtension extension : adeManager.getExtensions())
				log.info("Initializing ADE extension " + extension.getClass().getName());
			
			// exit shell mode if not all extensions could be loaded successfully
			if (shell && adeManager.hasExceptions()) {
				adeManager.logExceptions();
				throw new ImpExpException("Failed to load ADE extensions.");
			}
		} catch (IOException e) {
			throw new ImpExpException("Failed to initialize ADE extension support.", e);
		}
		
		// load CityGML and ADE context
		printInfoMessage("Loading CityGML and ADE contexts");
		try {
			CityGMLContext context = CityGMLContext.getInstance();
			for (ADEContext adeContext : adeManager.getADEContexts())
				context.registerADEContext(adeContext);
			
			// create CityGML builder and register with object registry
			CityGMLBuilder cityGMLBuilder = context.createCityGMLBuilder(externalLoader);			
			registry.setCityGMLBuilder(cityGMLBuilder);
		} catch (CityGMLBuilderException | ADEException e) {
			throw new ImpExpException("CityGML context could not be initialized.", e);
		}
		
		// initialize config
		printInfoMessage("Loading project settings");		
		if (configFile != null) {
			if (!configFile.isAbsolute())
				configFile = ClientConstants.WORKING_DIR.resolve(configFile);

			if (!Files.exists(configFile)) {
				throw new ImpExpException("Failed to find config file '" + configFile + "'.");
			} else if (!Files.isReadable(configFile) || !Files.isWritable(configFile)) {
				throw new ImpExpException("Insufficient access rights to config file '" + configFile + "'.");
			}
		} else
			configFile = CoreConstants.IMPEXP_DATA_DIR
					.resolve(ClientConstants.CONFIG_DIR).resolve(ClientConstants.PROJECT_SETTINGS_FILE);

		// with v3.3, the config path has been changed to not include the version number.
		// if the project file cannot be found, we thus check the old path used in v3.0 to v3.2
		if (!Files.exists(configFile)) {
			Path legacyConfigFile = Paths.get(CoreConstants.IMPEXP_DATA_DIR + "-3.0",
					ClientConstants.CONFIG_DIR, ClientConstants.PROJECT_SETTINGS_FILE);

			if (Files.exists(legacyConfigFile)) {
				log.warn("Failed to read project settings file '" + configFile + "'");
				log.warn("Loading settings from previous file '" + legacyConfigFile + "' instead");
				configFile = legacyConfigFile;
			}
		}

		Project project = config.getProject();
		try {
			Object object = ConfigUtil.getInstance().unmarshal(configFile.toFile());
			if (!(object instanceof Project))
				throw new JAXBException("Failed to interpret project file.");
			
			project = (Project)object;
		} catch (IOException | JAXBException e) {
			String errMsg = "Failed to read project settings file '" + configFile + "\'.";
			if (shell) {
				throw new ImpExpException(errMsg);
			} else {
				logMessages.put(LogLevel.ERROR, errMsg);
				logMessages.put(LogLevel.INFO, "Project settings initialized using default values.");
			}
		} finally {
			config.setProject(project);
		}

		if (!shell) {
			Path guiFile = CoreConstants.IMPEXP_DATA_DIR
					.resolve(ClientConstants.CONFIG_DIR).resolve(ClientConstants.GUI_SETTINGS_FILE);
			try {
				Object object = ConfigUtil.getInstance().unmarshal(guiFile.toFile());
				if (object instanceof Gui)
					config.setGui((Gui)object);
			} catch (JAXBException | IOException e) {
				//
			}
		}

		// load plugin configs to plugins
		for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
			try {
				pluginManager.propagatePluginConfig(plugin, config);
			} catch (PluginException e) {
				throw new ImpExpException("Failed to load config for plugin " + plugin.getClass().getName() + ".", e);
			}
		}

		// init logging environment
		Logging logging = config.getProject().getGlobal().getLogging();
		log.setConsoleLogLevel(logging.getConsole().getLogLevel());
		if (logging.getFile().isActive()) {
			log.setFileLogLevel(logging.getFile().getLogLevel());

			Path logFile = logging.getFile().isUseAlternativeLogFile() ?
					Paths.get(logging.getFile().getAlternativeLogFile()) :
					CoreConstants.IMPEXP_DATA_DIR.resolve(ClientConstants.LOG_DIR).resolve(log.getDefaultLogFileName());

			boolean success = log.appendLogFile(logFile, logging.getFile().getLogFileMode());
			if (!success) {
				logging.getFile().setActive(false);
				logging.getFile().setUseAlternativeLogFile(false);
				log.detachLogFile();
			} else {
				log.logToFile("*** Command line arguments: " +  (args.length == 0 ? "no arguments passed" : String.join(" ", args)));
			}
		}

		// create pid file
		if (pidFile != null) {
			try {
				log.debug("Creating PID file '" + pidFile.normalize().toAbsolutePath() + "'");
				PidFile.create(pidFile, true);
			} catch (IOException e) {
				throw new ImpExpException("Failed to create PID file.", e);
			}
		}

		// init internationalized labels 
		LanguageType lang = config.getProject().getGlobal().getLanguage();
		if (lang == null)
			lang = LanguageType.fromValue(System.getProperty("user.language"));

		if (!Language.existsLanguagePack(new Locale(lang.value())))
			lang = LanguageType.EN;

		Language.I18N = ResourceBundle.getBundle("org.citydb.config.i18n.language", new Locale(lang.value()));
		config.getProject().getGlobal().setLanguage(lang);

		// start application
		if (!shell) {
			// create main view instance
			final ImpExpGui mainView = new ImpExpGui(config);

			// create database plugin
			final DatabasePlugin databasePlugin = new DatabasePlugin(mainView, config);
			databaseController.setConnectionViewHandler(databasePlugin.getConnectionViewHandler());

			// register internal plugins
			pluginManager.registerInternalPlugin(new CityGMLImportPlugin(mainView, config));		
			pluginManager.registerInternalPlugin(new CityGMLExportPlugin(mainView, config));
			pluginManager.registerInternalPlugin(new KMLExportPlugin(mainView, kmlContext, colladaContext, config));
			pluginManager.registerInternalPlugin(databasePlugin);
			pluginManager.registerInternalPlugin(new PreferencesPlugin(mainView, config));

			// initialize plugins
			for (Plugin plugin : pluginManager.getPlugins()) {
				if (plugin instanceof ViewExtension) {
					((ViewExtension) plugin).initViewExtension(mainView, new Locale(lang.value()));
					if (useSplashScreen && !(plugin instanceof InternalPlugin))
						splashScreen.setMessage("Initializing plugin " + plugin.getClass().getName());
				}
			}

			// initialize gui
			printInfoMessage("Starting graphical user interface");
			SwingUtilities.invokeLater(() -> mainView.invoke(logMessages));

			try {
				// clean up heap space
				System.gc();
				Thread.sleep(700);
			} catch (InterruptedException e) {
				//
			}

			if (useSplashScreen)
				splashScreen.close();
		}	

		else {
			ImpExpCliOld cmd = new ImpExpCliOld(kmlContext, colladaContext, config);
			boolean success = false;

			if (validateFile != null)
				success = cmd.doValidate(validateFile);
			else if (importFile != null)
				success = cmd.doImport(importFile);
			else if (exportFile != null)
				success = cmd.doExport(exportFile);
			else if (databaseDelete)
				success = cmd.doDelete();
			else if (kmlExportFile != null)
				success= cmd.doKmlExport(kmlExportFile);
			else if (testConnection)
				success = cmd.doTestConnection();

			if (!success)
				System.exit(1);
		}
	}

	private void setLookAndFeel() throws ImpExpException {
		try {
			// set look & feel
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			if (OSXAdapter.IS_MAC_OS_X) {
				OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(ImpExpOld.class.getResource("/org/citydb/gui/images/common/logo_small.png")));
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
		} catch (Exception e) {
			throw new ImpExpException("Failed to initialize user interface.", e);
		}
	}

	private void logErrorAndExit(ImpExpException e) {
		log.logStackTrace(e);
		log.error("Aborting...");
		System.exit(1);
	}

	public void printInfoMessage(String message) {
		log.info(message);
		if (useSplashScreen) {
			splashScreen.setMessage(message);
			splashScreen.nextStep(currentStep++, maximumSteps);
		}
	}

	private void printUsage(CmdLineParser parser, PrintStream out) {
		out.println("Usage: impexp [-options]");
		out.println("            (default: to execute gui version)");
		out.println("   or  impexp -shell [-command] [-options]");
		out.println("            (to execute cli version)");
		out.println();
		out.println("where options include:");
		parser.printUsage(out);
		out.println();
	}

}
