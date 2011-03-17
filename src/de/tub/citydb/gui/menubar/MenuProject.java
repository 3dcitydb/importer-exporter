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
package de.tub.citydb.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.Project;
import de.tub.citydb.config.project.ProjectConfigUtil;
import de.tub.citydb.config.project.global.Logging;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.Logger;

@SuppressWarnings("serial")
public class MenuProject extends JMenu {
	private final Logger LOG = Logger.getInstance();
	private final Config config;
	private final JAXBContext ctx;
	private final ImpExpGui topFrame;

	private JMenuItem openProject;
	private JMenuItem saveProject;
	private JMenuItem saveProjectAs;
	private JMenuItem defaults;
	private JMenuItem xsdProject;
	private JMenu lastUsed;

	private String exportPath;
	private String importPath;

	public MenuProject(Config config, JAXBContext ctx, ImpExpGui topFrame) {
		this.config = config;
		this.ctx = ctx;
		this.topFrame = topFrame;		
		init();
	}

	private void init() {
		openProject = new JMenuItem();
		saveProject = new JMenuItem();
		saveProjectAs = new JMenuItem();
		defaults = new JMenuItem();
		xsdProject = new JMenuItem();
		lastUsed = new JMenu();

		openProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = loadFileDialog(Internal.I18N.getString("menu.project.open.label"));

				if (file != null) {
					openProject(file);

					addLastUsedProject(file.getAbsolutePath());
					lastUsed.setEnabled(true);
					setLastUsedList();
					lastUsed.repaint();
				}				
			}
		});

		saveProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				topFrame.setSettings();
				if (topFrame.saveProjectSettings())
					LOG.info("Settings successfully saved to config file '" + 
							new File(config.getInternal().getConfigPath()).getAbsolutePath() + File.separator + config.getInternal().getConfigProject() + "'.");

			}
		});

		saveProjectAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = saveFileDialog(Internal.I18N.getString("menu.project.saveAs.label"), true);

				if (file != null) {
					LOG.info("Saving project settings as file '" + file.toString() + "'.");
					try {
						topFrame.setSettings();
						ProjectConfigUtil.marshal(config.getProject(), file.toString(), ctx);

						addLastUsedProject(file.getAbsolutePath());
						lastUsed.setEnabled(true);
						setLastUsedList();
						lastUsed.repaint();
					} catch (JAXBException e1) {
						LOG.error("Failed to save project settings: " + e1.getMessage());
					}
				}				
			}
		});

		defaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), 
						Internal.I18N.getString("menu.project.defaults.msg"), 
						Internal.I18N.getString("menu.project.defaults.msg.title"), JOptionPane.YES_NO_OPTION);

				if (res == JOptionPane.YES_OPTION) {
					topFrame.getConsoleText().setText("");

					if (config.getInternal().isConnected()) {
						try {
							topFrame.getDBPool().close();
						} catch (SQLException e1) {
							topFrame.getDBPool().forceClose();
						}
						
						LOG.info("Disconnected from database.");
					}

					config.setProject(new Project());
					topFrame.loadSettings();
					topFrame.doTranslation();
					LOG.info("Project settings are reset to default values.");
				}
			}
		});

		xsdProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = saveFileDialog(Internal.I18N.getString("menu.project.saveXSDas.label"), false);

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

		add(openProject);
		add(saveProject);
		add(saveProjectAs);
		addSeparator();
		add(defaults);
		addSeparator();
		add (xsdProject);
		addSeparator();
		add(lastUsed);

		if (!config.getGui().getRecentlyUsedProjectFiles().isEmpty())
			setLastUsedList();			
		else
			lastUsed.setEnabled(false);
	}

	public void doTranslation() {
		openProject.setText(Internal.I18N.getString("menu.project.open.label"));
		saveProject.setText(Internal.I18N.getString("menu.project.save.label"));
		saveProjectAs.setText(Internal.I18N.getString("menu.project.saveAs.label"));
		defaults.setText(Internal.I18N.getString("menu.project.defaults.label"));
		xsdProject.setText(Internal.I18N.getString("menu.project.saveXSDas.label"));
		lastUsed.setText(Internal.I18N.getString("menu.project.lastUsed.label"));

		GuiUtil.setMnemonic(openProject, "menu.project.open.label", "menu.project.open.label.mnemonic");
		GuiUtil.setMnemonic(saveProject, "menu.project.save.label", "menu.project.save.label.mnemonic");
		GuiUtil.setMnemonic(saveProjectAs, "menu.project.saveAs.label", "menu.project.saveAs.label.mnemonic");
		GuiUtil.setMnemonic(defaults, "menu.project.defaults.label", "menu.project.defaults.label.mnemonics");
		GuiUtil.setMnemonic(xsdProject, "menu.project.saveXSDas.label", "menu.project.saveXSDas.label.mnemonic");
		GuiUtil.setMnemonic(lastUsed, "menu.project.lastUsed.label", "menu.project.lastUsed.label.mnemonic");
	}

	private boolean openProject(File file) {		
		LOG.info("Loading project settings from file '" + file.toString() + "'.");
		boolean success = false;

		try {
			Logging logging = config.getProject().getGlobal().getLogging();
			Project project = ProjectConfigUtil.unmarshal(file.toString(), ctx);

			if (project != null) {
				config.setProject(project);
				topFrame.doTranslation();
				topFrame.loadSettings();	

				// adapt logging subsystem
				project.getGlobal().setLogging(logging);
				topFrame.setLoggingSettings();

				success = true;
			} else
				LOG.error("Failed to read project settings.");
		} catch (FileNotFoundException e1) {
			LOG.error("Failed to find project settings file '" + file.toString() + "'.");
		} catch (JAXBException e1) {
			LOG.error("Failed to read project settings: " + e1.getMessage());
		}					

		return success;
	}

	private void addLastUsedProject(String fileName) {
		List<String> lastUsedList = config.getGui().getRecentlyUsedProjectFiles();
		if (lastUsedList.contains(fileName))
			lastUsedList.remove(fileName);

		lastUsedList.add(0, fileName);
		if (lastUsedList.size() > config.getGui().getMaxLastUsedEntries())
			config.getGui().setRecentlyUsedProjectFiles(lastUsedList.subList(0, config.getGui().getMaxLastUsedEntries()));
	}

	private void setLastUsedList() {
		lastUsed.removeAll();

		for (final String fileName : config.getGui().getRecentlyUsedProjectFiles()) {
			final JMenuItem item = new JMenuItem(fileName);

			File tmp = new File(fileName);
			String name = tmp.getName();
			String path = tmp.getParent();
			if (path == null)
				path = System.getProperty("user.dir");

			final File file = new File(path + File.separator + name);

			String descString = null;
			if (path.length() > 80 && path.indexOf(File.separator) != -1) {
				String first = path.substring(0, path.indexOf(File.separator));
				String last = path.substring(path.lastIndexOf(File.separator), path.length());

				descString = first + File.separator + "..." + last + File.separator + name;
			} else
				descString = file.getAbsolutePath();

			item.setText(descString);
			lastUsed.add(item);

			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean success = openProject(file);
					if (!success) {
						config.getGui().getRecentlyUsedProjectFiles().remove(fileName);
						lastUsed.remove(item);
						if (lastUsed.getItemCount() == 0)
							lastUsed.setEnabled(false);
					} else {
						importPath = file.getPath();
						addLastUsedProject(fileName);
						setLastUsedList();
					}
					
					lastUsed.repaint();
				}
			});
		}
	}

	private File saveFileDialog(String title, boolean isXml) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
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
		if (importPath == null)
			importPath = exportPath;

		return file;
	}

	private File loadFileDialog(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
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
		if (exportPath == null)
			exportPath = importPath;

		return chooser.getSelectedFile();
	}
}
