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
package org.citydb.gui.components.menubar;

import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.Project;
import org.citydb.config.project.global.Logging;
import org.citydb.event.global.ProjectChangedEvent;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.modules.preferences.PreferencesPlugin;
import org.citydb.plugin.InternalPlugin;
import org.citydb.plugin.PluginException;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.config.ConfigExtension;
import org.citydb.plugin.extension.config.PluginConfigEvent;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.CoreConstants;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("serial")
public class MenuProject extends JMenu {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final ImpExpGui mainView;
	private final PluginManager pluginManager;

	private JMenuItem openProject;
	private JMenuItem saveProject;
	private JMenuItem saveProjectAs;
	private JMenuItem defaults;
	private JMenuItem xsdProject;
	private JMenu lastUsed;

	private String exportPath;
	private String importPath;

	MenuProject(ImpExpGui mainView, Config config) {
		this.config = config;
		this.mainView = mainView;
		pluginManager = PluginManager.getInstance();

		init();
	}

	private void init() {
		openProject = new JMenuItem();
		saveProject = new JMenuItem();
		saveProjectAs = new JMenuItem();
		defaults = new JMenuItem();
		xsdProject = new JMenuItem();
		lastUsed = new JMenu();

		openProject.addActionListener(e -> {
            File file = loadDialog(Language.I18N.getString("menu.project.open.label"));

            if (file != null) {
                openProject(file);

                addLastUsedProject(file.getAbsolutePath());
                lastUsed.setEnabled(true);
                setLastUsedList();
                lastUsed.repaint();
            }
        });

		saveProject.addActionListener(e -> {
            // set settings on internal plugins
            for (InternalPlugin plugin : pluginManager.getInternalPlugins())
                plugin.setSettings();

            // fire event to external plugins
            for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class))
                plugin.handleEvent(PluginConfigEvent.PRE_SAVE_CONFIG);

            if (mainView.saveProjectSettings())
                log.info("Settings successfully saved to config file '"
                        + CoreConstants.IMPEXP_DATA_DIR
                                .resolve(ClientConstants.CONFIG_DIR)
                                .resolve(ClientConstants.PROJECT_SETTINGS_FILE) + "'.");
        });

		saveProjectAs.addActionListener(e -> {
            File file = saveDialog(Language.I18N.getString("menu.project.saveAs.label"), true);

            if (file != null) {
                log.info("Saving project settings as file '" + file.toString() + "'.");
                try {
                    // set settings on internal plugins
                    for (InternalPlugin plugin : pluginManager.getInternalPlugins())
                        plugin.setSettings();

                    // fire event to external plugins
                    for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class))
                        plugin.handleEvent(PluginConfigEvent.PRE_SAVE_CONFIG);

                    ConfigUtil.getInstance().marshal(config.getProject(), file);

                    addLastUsedProject(file.getAbsolutePath());
                    lastUsed.setEnabled(true);
                    setLastUsedList();
                    lastUsed.repaint();
                } catch (JAXBException e1) {
                    log.error("Failed to save project settings.", e1);
                }
            }
        });

		defaults.addActionListener(e -> {
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
					Language.I18N.getString("menu.project.defaults.msg"),
					Language.I18N.getString("menu.project.defaults.msg.title"), JOptionPane.YES_NO_OPTION);

			if (res == JOptionPane.YES_OPTION) {
				mainView.clearConsole();
				mainView.disconnectFromDatabase();

				config.setProject(new Project());

				// reset contents of srs combo boxes
				SrsComboBoxFactory.getInstance(config).resetAll(true);

				// reset defaults on internal plugins
				for (InternalPlugin plugin : pluginManager.getInternalPlugins())
					plugin.loadSettings();

				// update plugin configs
				for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class))
					plugin.handleEvent(PluginConfigEvent.RESET_DEFAULT_CONFIG);

				// trigger event
				ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(new ProjectChangedEvent(this));

				mainView.doTranslation();
				log.info("Project settings are reset to default values.");
			}
		});

		xsdProject.addActionListener(e -> {
            File path = saveDialog(Language.I18N.getString("menu.project.saveXSDas.label"), false);

            if (path != null) {
                log.info("Saving project XSD at location '" + path.toString() + "'.");
                try {
                    ConfigUtil.getInstance().generateSchema(path);
                } catch (JAXBException | IOException e1) {
                    log.error("Failed to save project settings.", e1);
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

		openProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveProjectAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));

		if (!config.getGui().getRecentlyUsedProjectFiles().isEmpty())
			setLastUsedList();			
		else
			lastUsed.setEnabled(false);
	}

	public void doTranslation() {
		openProject.setText(Language.I18N.getString("menu.project.open.label"));
		saveProject.setText(Language.I18N.getString("menu.project.save.label"));
		saveProjectAs.setText(Language.I18N.getString("menu.project.saveAs.label"));
		defaults.setText(Language.I18N.getString("menu.project.defaults.label"));
		xsdProject.setText(Language.I18N.getString("menu.project.saveXSDas.label"));
		lastUsed.setText(Language.I18N.getString("menu.project.lastUsed.label"));

		GuiUtil.setMnemonic(openProject, "menu.project.open.label", "menu.project.open.label.mnemonic");
		GuiUtil.setMnemonic(saveProject, "menu.project.save.label", "menu.project.save.label.mnemonic");
		GuiUtil.setMnemonic(saveProjectAs, "menu.project.saveAs.label", "menu.project.saveAs.label.mnemonic");
		GuiUtil.setMnemonic(defaults, "menu.project.defaults.label", "menu.project.defaults.label.mnemonics");
		GuiUtil.setMnemonic(xsdProject, "menu.project.saveXSDas.label", "menu.project.saveXSDas.label.mnemonic");
		GuiUtil.setMnemonic(lastUsed, "menu.project.lastUsed.label", "menu.project.lastUsed.label.mnemonic");
	}

	private boolean openProject(File file) {		
		log.info("Loading project settings from file '" + file.toString() + "'.");
		boolean success = false;

		try {
			Logging logging = config.getProject().getGlobal().getLogging();
			Object object = ConfigUtil.getInstance().unmarshal(file);
			if (!(object instanceof Project)) {
				log.error("Failed to read project settings.");
				return false;
			}

			Project project = (Project)object;
			config.setProject(project);
			mainView.doTranslation();

			// reset contents of srs combo boxes
			SrsComboBoxFactory.getInstance(config).resetAll(true);

			// load settings for internal plugins
			for (InternalPlugin plugin : pluginManager.getInternalPlugins())
				plugin.loadSettings();

			// update plugin configs
			for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
				try {
					pluginManager.propagatePluginConfig(plugin, config);
				} catch (PluginException e) {
					log.error("Failed to load configuration for plugin " + plugin.getClass().getName() + ".");
					log.warn("The plugin will most likely not work.");
				}
			}

			// adapt logging subsystem
			project.getGlobal().setLogging(logging);

			// reset logging settings
			pluginManager.getInternalPlugin(PreferencesPlugin.class).setLoggingSettings();
			
			// trigger event
			ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(new ProjectChangedEvent(this));
			success = true;
		} catch (IOException e) {
			log.error("Failed to read project settings file '" + file.toString() + "'.");
		} catch (JAXBException e) {
			log.error("Failed to read project settings: " + e.getMessage());
		}

		return success;
	}

	private void addLastUsedProject(String fileName) {
		List<String> lastUsedList = config.getGui().getRecentlyUsedProjectFiles();
		lastUsedList.remove(fileName);
		lastUsedList.add(0, fileName);

		if (lastUsedList.size() > config.getGui().getMaxLastUsedEntries())
			config.getGui().setRecentlyUsedProjectFiles(lastUsedList.subList(0, config.getGui().getMaxLastUsedEntries()));
	}

	private void setLastUsedList() {
		lastUsed.removeAll();

		for (final String fileName : config.getGui().getRecentlyUsedProjectFiles()) {
			final JMenuItem item = new JMenuItem();

			File tmp = new File(fileName);
			String name = tmp.getName();
			String path = tmp.getParent();
			if (path == null)
				path = System.getProperty("user.dir");

			final File file = new File(path + File.separator + name);

			String descString;
			if (path.length() > 80 && path.contains(File.separator)) {
				String first = path.substring(0, path.indexOf(File.separator));
				String last = path.substring(path.lastIndexOf(File.separator), path.length());

				descString = first + File.separator + "..." + last + File.separator + name;
			} else
				descString = file.getAbsolutePath();

			item.setText(descString);
			lastUsed.add(item);

			item.addActionListener(e -> {
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
            });
		}
	}

	private File saveDialog(String title, boolean isXml) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);

		if (isXml) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Project Files (*.xml)", "xml");
			chooser.addChoosableFileFilter(filter);
			chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
			chooser.setFileFilter(filter);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);			
		} else 
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (exportPath != null)
			chooser.setCurrentDirectory(new File(exportPath));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return null;

		File file = chooser.getSelectedFile();		
		if (isXml && (!file.getName().contains(".")))
			file = new File(file + ".xml");

		exportPath = chooser.getCurrentDirectory().getAbsolutePath();
		if (importPath == null)
			importPath = exportPath;

		return file;
	}

	private File loadDialog(String title) {
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
