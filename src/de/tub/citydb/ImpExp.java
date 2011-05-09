/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.citygml4j.CityGMLContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import de.tub.citydb.cmd.ImpExpCmd;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.Gui;
import de.tub.citydb.config.gui.GuiConfigUtil;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.Project;
import de.tub.citydb.config.project.ProjectConfigUtil;
import de.tub.citydb.config.project.global.LanguageType;
import de.tub.citydb.config.project.global.Logging;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.SplashScreen;
import de.tub.citydb.jaxb.JAXBContextRegistry;
import de.tub.citydb.log.Logger;

public class ImpExp {
	
	// set look & feel
	static {
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
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
	private JAXBContext cityGMLContext, kmlContext, colladaContext, projectContext, guiContext;
	private DBConnectionPool dbPool;
	private Config config;
	private List<String> errMsgs = new ArrayList<String>();
	
	private SplashScreen splashScreen;
	private boolean useSplashScreen;

	public static void main(String[] args) {
		new ImpExp().doMain(args);
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
			splashScreen = new SplashScreen(4, 2, 40, Color.BLACK);
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

		// initialize JAXB and database environment
		printInfoMessage("Initializing application environment");
		config = new Config();
		
		try {
			cityGMLContext = JAXBContextRegistry.registerInstance("org.citygml", new CityGMLContext().createJAXBContext());
			kmlContext = JAXBContextRegistry.getInstance("net.opengis.kml._2");
			colladaContext = JAXBContextRegistry.getInstance("org.collada._2005._11.colladaschema");
			projectContext = JAXBContextRegistry.getInstance("de.tub.citydb.config.project");
			guiContext = JAXBContextRegistry.getInstance("de.tub.citydb.config.gui");
			dbPool = DBConnectionPool.getInstance("de.tub.citydb", config);
		} catch (JAXBException e) {
			LOG.error("Application environment could not be initialized");
			LOG.error("Aborting...");
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
		String projectFile = confPath + File.separator + projectFileName;

		try {
			Project configProject = null;
			configProject = ProjectConfigUtil.unmarshal(projectFile, projectContext);
			config.setProject(configProject);
		} catch (FileNotFoundException fne) {
			String errMsg = "Failed to find project settings file '" + projectFile + '\'';
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
		} 

		if (!shell) {
			Gui configGui = null;
			String guiFile = confPath + File.separator + config.getInternal().getConfigGui();

			try {
				configGui = GuiConfigUtil.unmarshal(guiFile, guiContext);
				config.setGui(configGui);
			} catch (JAXBException jaxbE) {
				//
			} catch (FileNotFoundException fne) {
				//
			}
		}

		// init logging environment
		Logging logging = config.getProject().getGlobal().getLogging();
		LOG.setConsoleLogLevel(logging.getConsole().getLogLevel());
		if (logging.getFile().isSet()) {
			LOG.setFileLogLevel(logging.getFile().getLogLevel());

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
			// initialize gui
			printInfoMessage("Starting graphical user interface");

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new ImpExpGui(cityGMLContext,
							kmlContext,
							colladaContext,
							projectContext,
							guiContext,
							dbPool,
							config).invoke(errMsgs);
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
			String fileList = buildFileList(validateFile);			
			if (fileList == null || fileList.length() == 0) {
				LOG.error("Invalid list of files to be validated");
				LOG.error("Aborting...");
				return;
			}

			// set import file names...
			config.getInternal().setImportFileName(fileList);

			new Thread() {
				public void run() {
					new ImpExpCmd(cityGMLContext, 
							dbPool, 
							config).doValidate();
				}
			}.start();

			return;
		}

		if (importFile != null) {
			String fileList = buildFileList(importFile);			
			if (fileList == null || fileList.length() == 0) {
				LOG.error("Invalid list of files to be imported");
				LOG.error("Aborting...");
				return;
			}

			// set import file names...
			config.getInternal().setImportFileName(fileList);

			new Thread() {
				public void run() {
					new ImpExpCmd(cityGMLContext, 
							dbPool, 
							config).doImport();
				}
			}.start();

			return;
		}

		if (exportFile != null) {
			config.getInternal().setExportFileName(exportFile);

			new Thread() {
				public void run() {
					new ImpExpCmd(cityGMLContext, 
							dbPool, 
							config).doExport();
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
							dbPool, 
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
		out.println("Usage: java -jar impexp.jar [-options]");
		out.println("            (default: to execute gui version)");
		out.println("   or  java -jar impexp.jar -shell [-command] [-options]");
		out.println("            (to execute shell version)");
		out.println();
		out.println("where options include:");
		parser.printUsage(System.out);
		out.println();
	}

	private String buildFileList(String userString) {
		StringBuilder buffer = new StringBuilder();

		for (String part : userString.split(";")) {
			if (part == null || part.trim().isEmpty())
				continue;

			File input = new File(part.trim());

			if (input.isDirectory()) {
				buffer.append(input.getAbsolutePath());
				buffer.append("\n");
				continue;
			}

			final String path = new File(input.getAbsolutePath()).getParent();
			final String fileName = replaceWildcards(input.getName().toLowerCase());

			input = new File(path);
			if (!input.exists()) {
				LOG.error("'" + input.toString() + "' does not exist");
				continue;
			}

			File[] list = input.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.toLowerCase().matches(fileName));
				}
			});

			if (list != null && list.length != 0) {
				int i = 0;
				for (File file : list) {
					if (file.isFile()) {
						String name = file.getName().toUpperCase();
						if (!name.endsWith(".GML") && 
								!name.endsWith(".XML") &&
								!name.endsWith(".CITYGML"))
							continue;
					}
					
					buffer.append(file.getAbsolutePath());
					buffer.append("\n");
					++i;
				}

				if (i == 0)
					LOG.warn("No import files found at '" + part + "'");
			} else
				LOG.warn("No import files found at '" + part + "'");
		}

		return buffer.toString();
	}

	private String replaceWildcards(String input) {
		StringBuilder buffer = new StringBuilder();
		char [] chars = input.toCharArray();

		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] == '*')
				buffer.append(".*");
			else if (chars[i] == '?')
				buffer.append(".");
			else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
				buffer.append('\\').append(chars[i]);
			else
				buffer.append(chars[i]);
		}

		return buffer.toString();
	}
}
