package de.tub.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.Project;
import de.tub.citydb.config.project.ProjectConfigUtil;
import de.tub.citydb.config.project.global.Logging;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.log.Logger;

public class MenuProject extends JMenu {
	private final Logger LOG = Logger.getInstance();
	private final Config config;
	private final JAXBContext ctx;
	
	private JMenuItem importProject;
	private JMenuItem exportProject;
	private JMenuItem xsdProject;
	
	private String exportPath;
	private String importPath;
	
	public MenuProject(String name, Config config, JAXBContext ctx) {
		super(name);
		this.config = config;
		this.ctx = ctx;
		init();
	}
	
	private void init() {
		importProject = new JMenuItem("");
		exportProject = new JMenuItem("");
		xsdProject = new JMenuItem("");
		
		importProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = importFileDialog();
				
				if (file != null) {
					LOG.info("Loading project settings from file '" + file.toString() + "'.");
					
					try {
						Logging logging = config.getProject().getGlobal().getLogging();
						Project project = ProjectConfigUtil.unmarshal(file.toString(), ctx);
						
						if (project != null) {
							config.setProject(project);
							((ImpExpGui)getTopLevelAncestor()).doTranslation();
							((ImpExpGui)getTopLevelAncestor()).loadSettings();	
							
							// adapt logging subsystem
							project.getGlobal().setLogging(logging);
							((ImpExpGui)getTopLevelAncestor()).setLoggingSettings();
						} else
							LOG.error("Failed to read project settings.");
					} catch (FileNotFoundException e1) {
						LOG.error("Failed to find project settings file '" + file.toString() + "'.");
					} catch (JAXBException e1) {
						LOG.error("Failed to read project settings: " + e1.getMessage());
					}					
				}				
			}
		});
		
		exportProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = exportFileDialog(true);
				
				if (file != null) {
					LOG.info("Saving project settings as file '" + file.toString() + "'.");
					try {
						((ImpExpGui)getTopLevelAncestor()).setSettings();
						ProjectConfigUtil.marshal(config.getProject(), file.toString(), ctx);
					} catch (JAXBException e1) {
						LOG.error("Failed to save project settings: " + e1.getMessage());
					}
				}				
			}
		});
		
		xsdProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = exportFileDialog(false);
				
				if (file != null) {
					LOG.info("Saving project XSD as file '" + file.toString() + "'.");
					try {
						ProjectConfigUtil.generateSchema(ctx, file);
					} catch (FileNotFoundException e1) {
						LOG.error("Failed to find project settings file '" + file.toString() + "'.");
					} catch (IOException e1) {
						LOG.error("Failed to save project settings: " + e1.getMessage());
					}
				}				
			}
		});
		
		add(importProject);
		add(exportProject);
		addSeparator();
		add (xsdProject);
	}
	
	public void doTranslation() {
		importProject.setText(ImpExpGui.labels.getString("menu.project.import.label"));
		exportProject.setText(ImpExpGui.labels.getString("menu.project.saveAs.label"));
		xsdProject.setText(ImpExpGui.labels.getString("menu.project.saveXSDas.label"));
	}
	
	private File exportFileDialog(boolean isXml) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter;
		
		if (isXml)
			filter = new FileNameExtensionFilter("Project Files (*.xml)", "xml");
		else 
			filter = new FileNameExtensionFilter("Project XSD Files (*.xsd)", "xsd");
		
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);
		
		if (exportPath != null) {
			chooser.setCurrentDirectory(new File(exportPath));
		} 
		
		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return null;
		
		File file = chooser.getSelectedFile();		
		if ((!file.getName().contains("."))) {
			if (isXml)
				file = new File(file + ".xml");
			else
				file = new File(file + ".xsd");
		}
		
		exportPath = chooser.getCurrentDirectory().getAbsolutePath();		
		return file;
	}

	private File importFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Project Files (*.xml)", "xml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);
		
		if (importPath != null) {
			chooser.setCurrentDirectory(new File(importPath));
		} 
		
		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return null;
		
		importPath = chooser.getCurrentDirectory().getAbsolutePath();		
		return chooser.getSelectedFile();
	}
}
