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
package org.citydb;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.cli.ImpExpCli;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.Gui;
import org.citydb.config.internal.Internal;
import org.citydb.config.language.Language;
import org.citydb.config.project.Project;
import org.citydb.config.project.global.LanguageType;
import org.citydb.config.project.global.Logging;
import org.citydb.config.project.plugin.PluginConfig;
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
import org.citydb.plugin.IllegalPluginEventChecker;
import org.citydb.plugin.Plugin;
import org.citydb.plugin.PluginConfigController;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.config.ConfigExtension;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.InternalProxySelector;
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

	private final Logger log = Logger.getInstance();
	private JAXBContext kmlContext, colladaContext, projectContext, guiContext;
	private Config config;

	private PluginManager pluginManager = PluginManager.getInstance();
	private ADEExtensionManager adeManager = ADEExtensionManager.getInstance();
	
	private SplashScreen splashScreen;
	private boolean useSplashScreen;	

	private List<String> errMsgs = new ArrayList<String>();

	public static void main(String[] args) {
		new ImpExp().doMain(args);
	}

	public void doMain(String[] args, Plugin... plugins) {
		if (plugins != null) {
			for (Plugin plugin : plugins)
				pluginManager.registerExternalPlugin(plugin);
		}

		doMain(args);
	}
	
	public void doMain(String[] args, ADEExtension... extensions) {
		if (extensions != null) {
			for (ADEExtension extension : extensions) {
				if (extension.getBasePath() == null)
					extension.setBasePath(Paths.get("."));
				
				adeManager.loadExtension(extension);
			}
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

		log.info("Starting " +
				this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + "\"");

		// load external plugins
		printInfoMessage("Loading plugins");
		URLClassLoader externalLoader = new URLClassLoader(ImpExp.class.getClassLoader());
		try {
			if (Files.exists(Paths.get(Internal.PLUGINS_PATH))) {
				try (Stream<Path> stream = Files.walk(Paths.get(Internal.PLUGINS_PATH))
						.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))) {
					stream.forEach(path -> externalLoader.addPath(path));
				}
			}

			pluginManager.loadPlugins(externalLoader);
		} catch (IOException e) {
			log.error("Failed to initialize plugin support: " + e.getMessage());
			System.exit(1);
		}

		// get plugin config classes
		List<Class<?>> projectConfigClasses = new ArrayList<Class<?>>();
		projectConfigClasses.add(Project.class);
		for (ConfigExtension<? extends PluginConfig> plugin : pluginManager.getExternalConfigExtensions()) {
			try {
				projectConfigClasses.add(plugin.getClass().getMethod("getConfig", new Class<?>[]{}).getReturnType());
			} catch (SecurityException | NoSuchMethodException e) {
				log.error("Failed to instantiate config for plugin " + plugin.getClass().getName());
				log.error("Please check the following error message: " + e.getMessage());
				System.exit(1);
			}
		}

		// initialize application environment
		printInfoMessage("Initializing application environment");
		config = new Config();

		// initialize object registry
		ObjectRegistry registry = ObjectRegistry.getInstance();

		// create and register application-wide event dispatcher
		EventDispatcher eventDispatcher = new EventDispatcher();		
		registry.setEventDispatcher(eventDispatcher);

		// create and register database controller
		DatabaseController databaseController = new DatabaseController(config);
		registry.setDatabaseController(databaseController);

		// register illegal plugin event checker with event dispatcher
		IllegalPluginEventChecker checker = IllegalPluginEventChecker.getInstance();
		eventDispatcher.addEventHandler(EventType.DATABASE_CONNECTION_STATE, checker);
		eventDispatcher.addEventHandler(EventType.SWITCH_LOCALE, checker);

		// set internal proxy selector as default
		ProxySelector.setDefault(InternalProxySelector.getInstance(config));

		// create JAXB contexts
		try {
			kmlContext = JAXBContext.newInstance("net.opengis.kml._2", Thread.currentThread().getContextClassLoader());
			colladaContext = JAXBContext.newInstance("org.collada._2005._11.colladaschema", Thread.currentThread().getContextClassLoader());
			projectContext = JAXBContext.newInstance(projectConfigClasses.toArray(new Class<?>[]{}));
			guiContext = JAXBContext.newInstance(Gui.class);
		} catch (JAXBException e) {
			log.error("Application environment could not be initialized. Please check the following stack trace.");
			log.error("Aborting...");
			e.printStackTrace();
			System.exit(1);
		}
		
		// read database schema mapping and register with ObjectRegistry
		printInfoMessage("Loading database schema mapping");
		SchemaMapping schemaMapping = null;
		try {
			schemaMapping = SchemaMappingUtil.getInstance().unmarshal(Internal.CITYDB_SCHEMA_MAPPING_FILE);
			registry.register(SchemaMapping.class.getName(), schemaMapping);		
		} catch (JAXBException | SchemaMappingException | SchemaMappingValidationException e) {
			log.error("Failed to process 3DCityDB schema mapping file.");
			log.error("Cause: " + e.getMessage());
			log.error("Aborting...");
			System.exit(1);
		}

		// load ADE extensions	
		printInfoMessage("Loading ADE extensions");
		try {
			if (Files.exists(Paths.get(Internal.ADE_EXTENSIONS_PATH))) {
				try (Stream<Path> stream = Files.walk(Paths.get(Internal.ADE_EXTENSIONS_PATH))
						.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jar"))) {
					stream.forEach(path -> externalLoader.addPath(path));
				}
			}

			adeManager.loadExtensions(externalLoader);
			adeManager.loadSchemaMappings(schemaMapping);
			
			for (ADEExtension extension : adeManager.getExtensions())
				log.info("Initializing ADE extension " + extension.getClass().getName());
			
			// exit shell mode if not all extensions could be loaded successfully
			if (shell && adeManager.hasExceptions()) {
				adeManager.logExceptions();
				log.error("Aborting...");
				System.exit(1);
			}
		} catch (IOException e) {
			log.error("Failed to initialize ADE extension support: " + e.getMessage());
			log.error("Aborting...");
			System.exit(1);
		}
		
		// load CityGML and ADE context
		printInfoMessage("Loading CityGML and ADE contexts");
		try {
			CityGMLContext context = CityGMLContext.getInstance();
			for (ADEContext adeContext : adeManager.getADEContexts())
				context.registerADEContext(adeContext);
			
			// create JAXB builder and register in object registry
			CityGMLBuilder cityGMLBuilder = context.createCityGMLBuilder(externalLoader);			
			registry.setCityGMLBuilder(cityGMLBuilder);
		} catch (CityGMLBuilderException | ADEException e) {
			log.error("CityGML context could not be initialized");
			log.error("Aborting...");
			e.printStackTrace();
			System.exit(1);
		}
		
		// initialize config
		printInfoMessage("Loading project settings");		
		String confPath = null;
		String projectFileName = null;

		if (configFile != null) {
			if (!configFile.exists()) {
				log.error("Failed to find config file '" + configFile + "'");
				log.error("Aborting...");
				System.exit(1);
			} else if (!configFile.canRead() || !configFile.canWrite()) {
				log.error("Insufficient access rights to config file '" + configFile + "'");
				log.error("Aborting...");
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
				log.warn("Failed to read project settings file '" + projectFile + '\'');
				log.warn("Loading settings from previous file '" + oldProjectFile + "\' instead");
				projectFile = oldProjectFile;
			}
		}

		Project configProject = config.getProject();
		try {
			Object object = ConfigUtil.unmarshal(projectFile, projectContext);
			if (!(object instanceof Project))
				throw new JAXBException("Failed to interpret project file");
			
			configProject = (Project)object;
		} catch (IOException | JAXBException e) {
			String errMsg = "Failed to read project settings file '" + projectFile + '\'';
			if (shell) {
				log.error(errMsg);
				log.error("Aborting...");
				System.exit(1);
			} else
				errMsgs.add(errMsg);
		} finally {
			config.setProject(configProject);
		}

		if (!shell) {
			File guiFile = new File(confPath, config.getInternal().getConfigGui());
			try {
				Object object = ConfigUtil.unmarshal(guiFile, guiContext);
				if (object instanceof Gui)
					config.setGui((Gui)object);
			} catch (JAXBException | IOException e) {
				//
			}
		}

		// init logging environment
		Logging logging = config.getProject().getGlobal().getLogging();
		log.setDefaultConsoleLogLevel(logging.getConsole().getLogLevel());
		if (logging.getFile().isSet()) {
			log.setDefaultFileLogLevel(logging.getFile().getLogLevel());

			if (logging.getFile().isSetUseAlternativeLogPath() &&
					logging.getFile().getAlternativeLogPath().trim().length() == 0)
				logging.getFile().setUseAlternativeLogPath(false);

			String logPath = logging.getFile().isSetUseAlternativeLogPath() ? logging.getFile().getAlternativeLogPath() : Internal.DEFAULT_LOG_PATH;

			boolean success = log.appendLogFile(logPath, true);
			if (!success) {
				logging.getFile().setActive(false);
				logging.getFile().setUseAlternativeLogPath(false);
				log.detachLogFile();
			} else {
				Calendar cal = Calendar.getInstance();
				DecimalFormat df = new DecimalFormat("00");
				StringBuilder date = new StringBuilder()
						.append(cal.get(Calendar.YEAR)).append('-').append(df.format(cal.get(Calendar.MONTH) + 1)).append('-').append(df.format(cal.get(Calendar.DATE)));

				log.writeToFile("*** Starting new log file session on " + date.toString());
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

			log.writeToFile(msg.toString());
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

			// create database plugin
			final DatabasePlugin databasePlugin = new DatabasePlugin(mainView, config);
			databaseController.setConnectionViewHandler(databasePlugin.getConnectionViewHandler());

			// propagate config to plugins
			for (ConfigExtension<? extends PluginConfig> plugin : pluginManager.getExternalConfigExtensions())
				PluginConfigController.getInstance(config).setOrCreatePluginConfig(plugin);

			// initialize plugins
			for (Plugin plugin : pluginManager.getExternalPlugins()) {
				log.info("Initializing plugin " + plugin.getClass().getName());
				if (useSplashScreen)
					splashScreen.setMessage("Initializing plugin " + plugin.getClass().getName());

				plugin.init(mainView, new Locale(lang.value()));
			}

			// register internal plugins
			pluginManager.registerInternalPlugin(new CityGMLImportPlugin(mainView, config));		
			pluginManager.registerInternalPlugin(new CityGMLExportPlugin(mainView, config));		
			pluginManager.registerInternalPlugin(new KMLExportPlugin(mainView, kmlContext, colladaContext, config));
			pluginManager.registerInternalPlugin(databasePlugin);
			pluginManager.registerInternalPlugin(new PreferencesPlugin(mainView, config));

			// initialize internal plugins
			for (Plugin plugin : pluginManager.getInternalPlugins())
				plugin.init(mainView, new Locale(lang.value()));

			// initialize gui
			printInfoMessage("Starting graphical user interface");

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					mainView.invoke(projectContext,
							guiContext,
							errMsgs);
				}
			});

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
			ImpExpCli cmd = new ImpExpCli(kmlContext, colladaContext, config);
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

	public void setLookAndFeel() {
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

	public void printInfoMessage(String message) {
		log.info(message);
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
