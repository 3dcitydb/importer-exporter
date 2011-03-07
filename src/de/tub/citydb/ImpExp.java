package de.tub.citydb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import de.tub.citydb.cmd.ImpExpCmd;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.gui.Gui;
import de.tub.citydb.config.gui.GuiConfigUtil;
import de.tub.citydb.config.project.Project;
import de.tub.citydb.config.project.ProjectConfigUtil;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.jaxb.JAXBContextRegistry;
import de.tub.citydb.log.Logger;
import de.tub.citygml4j.CityGMLContext;

public class ImpExp {

	@Option(name="-config", usage="config file containing project settings", metaVar="fileName")
	private File configFile;

	@Option(name="-version", usage="print product version and exit")
	private boolean version;

	@Option(name="-h", aliases={"-help"}, usage="print this help message and exit")
	private boolean help;

	@Option(name="-shell", usage="to execute in a shell environment,\nwithout graphical user interface")
	private boolean shell;

	@Option(name="-import", usage="a ; list of directories and files to import,\nwildcards allowed\n(shell version only)", metaVar="fileName[s]")
	private String importFile;

	@Option(name="-export", usage="export data to this file\n(shell version only)", metaVar="fileName")
	private String exportFile;
	
	private Logger LOG = Logger.getInstance();
	private JAXBContext cityGMLContext, projectContext, guiContext;
	private DBConnectionPool dbPool;
	private Config config;
	private List<String> errMsgs = new ArrayList<String>();

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

		if (shell && importFile == null && exportFile == null) {
			System.out.println("Choose either option \"-import\" or \"-export\" for shell version");
			printUsage(parser, System.out);
			System.exit(1);
		}
		
		if (shell && importFile != null && exportFile != null) {
			System.out.println("Options \"-import\" and \"-export\" may not be mixed");
			printUsage(parser, System.out);
			System.exit(1);
		}
		
		// we do not need the parser instance any more
		LOG.info("Starting " +
				this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + "\"");

		// initialize JAXB and database environment
		LOG.info("Initializing application environment");

		try {
			cityGMLContext = JAXBContextRegistry.registerInstance("org.citygml", new CityGMLContext().toJAXBContext());
			projectContext = JAXBContextRegistry.getInstance("de.tub.citydb.config.project");
			guiContext = JAXBContextRegistry.getInstance("de.tub.citydb.config.gui");
			dbPool = DBConnectionPool.getInstance("de.tub.citydb");
		} catch (JAXBException e) {
			LOG.error("Application environment could not be initialized");
			LOG.error("Aborting...");
			System.exit(1);
		}

		// initialize config
		LOG.info("Loading project settings");
		config = new Config();
		String confDir = System.getProperty("user.dir") + File.separator + config.getInternal().getConfigPath();
		String projectFile = configFile == null ? confDir + File.separator + config.getInternal().getConfigProject() : configFile.toString();

		try {
			Project configProject = null;
			configProject = ProjectConfigUtil.unmarshal(projectFile, projectContext);
			config.setProject(configProject);
		} catch (JAXBException jaxbE) {
			String errMsg = "Project settings '" + projectFile + "' could not be loaded: " + jaxbE.getMessage();
			if (shell) {
				LOG.error(errMsg);
				LOG.error("Aborting...");
				System.exit(1);
			} else
				errMsgs.add(errMsg);
		} catch (FileNotFoundException fne) {
			String errMsg = "Project settings '" + projectFile + "' could not be loaded: " + fne.getMessage();
			if (shell) {
				LOG.error(errMsg);
				LOG.error("Aborting...");
				System.exit(1);
			} else
				errMsgs.add(errMsg);
		}

		if (!shell) {
			Gui configGui = null;
			String guiFile = confDir + File.separator + config.getInternal().getConfigGui();

			try {
				configGui = GuiConfigUtil.unmarshal(guiFile, guiContext);
				config.setGui(configGui);
			} catch (JAXBException jaxbE) {
				//
			} catch (FileNotFoundException fne) {
				//
			}
		}

		// start application
		if (!shell) {
			// initialize gui
			LOG.info("Starting graphical user interface");

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new ImpExpGui(cityGMLContext,
							projectContext,
							guiContext,
							dbPool,
							config).invoke(errMsgs);
				}
			});
			
			return;
		}	
		
		if (importFile != null) {
			StringBuilder buffer = new StringBuilder();
			
			for (String part : importFile.split(";")) {
				File input = new File(part.trim());
				final String path, fileName;
				
				if (input.isDirectory()) {
					path = input.getAbsolutePath();
					fileName = ".*";
				} else {
					path = new File(input.getAbsolutePath()).getParent();
					fileName = replaceWildcards(input.getName().toLowerCase());
				}
			
				input = new File(path);
				if (!input.exists()) {
					LOG.error("'" + input.toString() + "' existiert nicht.");
					continue;
				}
					
				File[] list = input.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return (name.toLowerCase().matches(fileName));
					}
				});
				
				if (list != null) {
					for (File file : list)
						if (file.isFile()) {
							buffer.append(file);
							buffer.append("\n");
						}
				} else
					LOG.error("No import files found at '" + part + "'");
			}
			
			// set import file names...
			config.getInternal().setImportFileName(buffer.toString());
			
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
	}

	private void printUsage(CmdLineParser parser, PrintStream out) {
		out.println("Usage: java -jar impexp.jar [-options]");
		out.println("            (default: to execute gui version)");
		out.println("   or  java -jar impexp.jar -shell [-import | -export] [-options]");
		out.println("            (to execute shell version)");
		out.println();
		out.println("where options include:");
		parser.printUsage(System.out);
		out.println();
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
