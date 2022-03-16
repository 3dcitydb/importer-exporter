/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.menu;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.ProjectConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.Logging;
import org.citydb.core.plugin.PluginException;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.plugin.extension.config.ConfigExtension;
import org.citydb.core.plugin.extension.config.PluginConfigEvent;
import org.citydb.core.plugin.internal.InternalPlugin;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.srs.SrsComboBoxFactory;
import org.citydb.gui.operation.preferences.PreferencesPlugin;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.global.ConfigChangedEvent;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MenuFile extends JMenu {
    private final Logger log = Logger.getInstance();
    private final Config config;
    private final ImpExpGui mainView;
    private final PluginManager pluginManager;

    private JMenuItem openConfig;
    private JMenuItem saveConfig;
    private JMenuItem saveConfigAs;
    private JMenuItem defaults;
    private JMenuItem xsdConfig;
    private JMenu lastUsed;
    private JMenuItem exit;

    private String exportPath;
    private String importPath;

    MenuFile(ImpExpGui mainView, Config config) {
        this.config = config;
        this.mainView = mainView;
        pluginManager = PluginManager.getInstance();

        init();
    }

    private void init() {
        openConfig = new JMenuItem(new FlatSVGIcon("org/citydb/gui/icons/folder_open.svg"));
        saveConfig = new JMenuItem(new FlatSVGIcon("org/citydb/gui/icons/save.svg"));
        saveConfigAs = new JMenuItem(new FlatSVGIcon("org/citydb/gui/icons/save_alt.svg"));
        defaults = new JMenuItem(new FlatSVGIcon("org/citydb/gui/icons/refresh.svg"));
        xsdConfig = new JMenuItem();
        lastUsed = new JMenu();
        exit = new JMenuItem();

        lastUsed.setIcon(new FlatSVGIcon("org/citydb/gui/icons/recently_used.svg"));

        openConfig.addActionListener(e -> {
            File file = loadDialog(Language.I18N.getString("menu.file.open.label"));
            if (file != null) {
                openConfig(file);
                addLastUsedConfig(file.getAbsolutePath());
                lastUsed.setEnabled(true);
                setLastUsedList();
                lastUsed.repaint();
            }
        });

        saveConfig.addActionListener(e -> {
            // set settings on internal plugins
            for (InternalPlugin plugin : pluginManager.getInternalPlugins()) {
                plugin.setSettings();
            }

            // fire event to external plugins
            for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
                plugin.handleEvent(PluginConfigEvent.PRE_SAVE_CONFIG);
            }

            if (mainView.saveSettings()) {
                log.info("Settings successfully saved to file '" + mainView.getConfigFile() + "'.");
            }
        });

        saveConfigAs.addActionListener(event -> {
            File file = saveDialog(Language.I18N.getString("menu.file.saveAs.label"), true);

            if (file != null) {
                log.info("Saving settings to file '" + file + "'.");
                try {
                    // set settings on internal plugins
                    for (InternalPlugin plugin : pluginManager.getInternalPlugins()) {
                        plugin.setSettings();
                    }

                    // fire event to external plugins
                    for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
                        plugin.handleEvent(PluginConfigEvent.PRE_SAVE_CONFIG);
                    }

                    ConfigUtil.getInstance().marshal(config.getProjectConfig(), file);

                    addLastUsedConfig(file.getAbsolutePath());
                    lastUsed.setEnabled(true);
                    setLastUsedList();
                    lastUsed.repaint();
                } catch (JAXBException e) {
                    log.error("Failed to save settings.", e);
                }
            }
        });

        defaults.addActionListener(e -> {
            int option = mainView.showOptionDialog(Language.I18N.getString("menu.file.defaults.msg.title"),
                    Language.I18N.getString("menu.file.defaults.msg"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                mainView.clearConsole();
                mainView.disconnectFromDatabase();

                config.setProjectConfig(new ProjectConfig());

                // reset contents of srs combo boxes
                SrsComboBoxFactory.getInstance().resetAll(true);

                // reset defaults on internal plugins
                for (InternalPlugin plugin : pluginManager.getInternalPlugins()) {
                    plugin.loadSettings();
                }

                // update plugin configs
                for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
                    plugin.handleEvent(PluginConfigEvent.RESET_DEFAULT_CONFIG);
                }

                // trigger event
                ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(new ConfigChangedEvent(this));

                mainView.switchLocale();
                log.info("Settings are reset to default values.");
            }
        });

        xsdConfig.addActionListener(event -> {
            File path = saveDialog(Language.I18N.getString("menu.file.saveXSDas.label"), false);

            if (path != null) {
                log.info("Saving settings XSD at location '" + path + "'.");
                try {
                    ConfigUtil.getInstance().generateSchema(path);
                } catch (JAXBException | IOException e) {
                    log.error("Failed to save settings XSD.", e);
                }
            }
        });

        exit.addActionListener(e -> ((ImpExpGui) getTopLevelAncestor()).dispose());

        add(openConfig);
        add(saveConfig);
        add(saveConfigAs);
        addSeparator();
        add(defaults);
        addSeparator();
        add(xsdConfig);
        addSeparator();
        add(lastUsed);
        addSeparator();
        add(exit);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        openConfig.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, toolkit.getMenuShortcutKeyMask()));
        saveConfig.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, toolkit.getMenuShortcutKeyMask()));
        saveConfigAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, toolkit.getMenuShortcutKeyMask() | InputEvent.SHIFT_DOWN_MASK));

        if (!config.getGuiConfig().getRecentlyUsedConfigFiles().isEmpty())
            setLastUsedList();
        else
            lastUsed.setEnabled(false);
    }

    public void switchLocale() {
        openConfig.setText(Language.I18N.getString("menu.file.open.label"));
        saveConfig.setText(Language.I18N.getString("menu.file.save.label"));
        saveConfigAs.setText(Language.I18N.getString("menu.file.saveAs.label"));
        defaults.setText(Language.I18N.getString("menu.file.defaults.label"));
        xsdConfig.setText(Language.I18N.getString("menu.file.saveXSDas.label"));
        lastUsed.setText(Language.I18N.getString("menu.file.lastUsed.label"));
        exit.setText(Language.I18N.getString("menu.file.exit.label"));

        GuiUtil.setMnemonic(openConfig, "menu.file.open.label", "menu.file.open.label.mnemonic");
        GuiUtil.setMnemonic(saveConfig, "menu.file.save.label", "menu.file.save.label.mnemonic");
        GuiUtil.setMnemonic(saveConfigAs, "menu.file.saveAs.label", "menu.file.saveAs.label.mnemonic");
        GuiUtil.setMnemonic(defaults, "menu.file.defaults.label", "menu.file.defaults.label.mnemonics");
        GuiUtil.setMnemonic(xsdConfig, "menu.file.saveXSDas.label", "menu.file.saveXSDas.label.mnemonic");
        GuiUtil.setMnemonic(lastUsed, "menu.file.lastUsed.label", "menu.file.lastUsed.label.mnemonic");
        GuiUtil.setMnemonic(exit, "menu.file.exit.label", "menu.file.exit.label.mnemonic");
    }

    private boolean openConfig(File file) {
        log.info("Loading settings from file '" + file.toString() + "'.");
        boolean success = false;

        try {
            Logging logging = config.getGlobalConfig().getLogging();
            Object object = ConfigUtil.getInstance().unmarshal(file);
            if (!(object instanceof ProjectConfig)) {
                log.error("Failed to load settings.");
                return false;
            }

            ProjectConfig projectConfig = (ProjectConfig) object;
            config.setProjectConfig(projectConfig);
            mainView.switchLocale();

            // reset contents of srs combo boxes
            SrsComboBoxFactory.getInstance().resetAll(true);

            // load settings for internal plugins
            for (InternalPlugin plugin : pluginManager.getInternalPlugins()) {
                plugin.loadSettings();
            }

            // update plugin configs
            for (ConfigExtension<?> plugin : pluginManager.getExternalPlugins(ConfigExtension.class)) {
                try {
                    pluginManager.propagatePluginConfig(plugin, config);
                } catch (PluginException e) {
                    log.error("Failed to load settings for plugin " + plugin.getClass().getName() + ".", e);
                    log.warn("The plugin will most likely not work.");
                }
            }

            // adapt logging subsystem
            config.getGlobalConfig().setLogging(logging);

            // reset logging settings
            pluginManager.getInternalPlugin(PreferencesPlugin.class).setLoggingSettings();

            // trigger event
            ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(new ConfigChangedEvent(this));
            success = true;
        } catch (JAXBException | IOException e) {
            log.error("Failed to load settings file '" + file + "'.", e);
        }

        return success;
    }

    private void addLastUsedConfig(String fileName) {
        List<String> lastUsedList = config.getGuiConfig().getRecentlyUsedConfigFiles();
        lastUsedList.remove(fileName);
        lastUsedList.add(0, fileName);

        if (lastUsedList.size() > config.getGuiConfig().getMaxLastUsedEntries()) {
            config.getGuiConfig().setRecentlyUsedConfigFiles(lastUsedList.subList(0, config.getGuiConfig().getMaxLastUsedEntries()));
        }
    }

    private void setLastUsedList() {
        lastUsed.removeAll();

        for (final String fileName : config.getGuiConfig().getRecentlyUsedConfigFiles()) {
            final JMenuItem item = new JMenuItem();

            File tmp = new File(fileName);
            String name = tmp.getName();
            String path = tmp.getParent();
            if (path == null) {
                path = System.getProperty("user.dir");
            }

            final File file = new File(path + File.separator + name);

            String descString;
            if (path.length() > 80 && path.contains(File.separator)) {
                String first = path.substring(0, path.indexOf(File.separator));
                String last = path.substring(path.lastIndexOf(File.separator));

                descString = first + File.separator + "..." + last + File.separator + name;
            } else {
                descString = file.getAbsolutePath();
            }

            item.setText(descString);
            lastUsed.add(item);

            item.addActionListener(e -> {
                boolean success = openConfig(file);
                if (!success) {
                    config.getGuiConfig().getRecentlyUsedConfigFiles().remove(fileName);
                    lastUsed.remove(item);
                    if (lastUsed.getItemCount() == 0) {
                        lastUsed.setEnabled(false);
                    }
                } else {
                    importPath = file.getPath();
                    addLastUsedConfig(fileName);
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
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Settings Files (*.xml)", "xml");
            chooser.addChoosableFileFilter(filter);
            chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
            chooser.setFileFilter(filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        } else {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        if (exportPath != null) {
            chooser.setCurrentDirectory(new File(exportPath));
        }

        int result = chooser.showSaveDialog(getTopLevelAncestor());
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        }

        File file = chooser.getSelectedFile();
        if (isXml && (!file.getName().contains("."))) {
            file = new File(file + ".xml");
        }

        exportPath = chooser.getCurrentDirectory().getAbsolutePath();
        if (importPath == null) {
            importPath = exportPath;
        }

        return file;
    }

    private File loadDialog(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Settings Files (*.xml)", "xml");
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileFilter(filter);

        if (importPath != null) {
            chooser.setCurrentDirectory(new File(importPath));
        }

        int result = chooser.showOpenDialog(getTopLevelAncestor());
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        }

        importPath = chooser.getCurrentDirectory().getAbsolutePath();
        if (exportPath == null) {
            exportPath = importPath;
        }

        return chooser.getSelectedFile();
    }
}
