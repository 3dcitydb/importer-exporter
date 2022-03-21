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

package org.citydb.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.citydb.cli.ImpExpCli;
import org.citydb.cli.ImpExpException;
import org.citydb.cli.option.StartupProgressListener;
import org.citydb.cli.util.CliConstants;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.GuiConfig;
import org.citydb.config.gui.style.Theme;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.plugin.CliCommand;
import org.citydb.core.plugin.Plugin;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.gui.components.SplashScreen;
import org.citydb.gui.operation.database.DatabasePlugin;
import org.citydb.gui.operation.exporter.CityGMLExportPlugin;
import org.citydb.gui.operation.importer.CityGMLImportPlugin;
import org.citydb.gui.operation.preferences.PreferencesPlugin;
import org.citydb.gui.operation.preferences.plugin.PluginsOverviewPlugin;
import org.citydb.gui.operation.visExporter.VisExportPlugin;
import org.citydb.gui.plugin.GuiExtension;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.util.log.Logger;
import picocli.CommandLine;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@CommandLine.Command(
        name = GuiCommand.NAME,
        description = "Starts the graphical user interface."
)
public class GuiCommand extends CliCommand implements StartupProgressListener {
    public static final String NAME = "gui";

    @CommandLine.Option(names = "--no-splash", description = "Hide the splash screen during startup.")
    private boolean hideSplash;

    @CommandLine.ParentCommand
    private ImpExpCli parent;

    private final PluginManager pluginManager = PluginManager.getInstance();
    private final Logger log = Logger.getInstance();

    private GuiConfig guiConfig;
    private int processSteps;
    private SplashScreen splashScreen;

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        // set GUI configuration
        config.setGuiConfig(guiConfig);

        // initialize predefined GUI components
        ImpExpGui impExpGui = new ImpExpGui(parent.getConfigFile());

        try {
            parent.logProgress("Starting graphical user interface");
            initializeViewComponents(impExpGui, config);
            SwingUtilities.invokeLater(impExpGui::invoke);
        } catch (Throwable e) {
            throw new ImpExpException("Failed to initialize the graphical user interface.", e);
        } finally {
            if (!hideSplash) {
                splashScreen.close();
            }
        }

        return 0;
    }

    private GuiConfig loadGuiConfig() {
        Path guiConfigFile = CoreConstants.IMPEXP_DATA_DIR
                .resolve(CliConstants.CONFIG_DIR)
                .resolve(CliConstants.GUI_SETTINGS_FILE);

        Object object = null;
        try {
            object = ConfigUtil.getInstance().unmarshal(guiConfigFile.toFile());
        } catch (JAXBException | IOException e) {
            //
        }

        return object instanceof GuiConfig ? (GuiConfig) object : new GuiConfig();
    }

    private void initializeViewComponents(ImpExpGui impExpGui, Config config) {
        // register predefined internal plugins
        pluginManager.registerInternalPlugin(new CityGMLImportPlugin());
        pluginManager.registerInternalPlugin(new CityGMLExportPlugin());
        pluginManager.registerInternalPlugin(new VisExportPlugin());

        // create and register database plugin
        DatabasePlugin databasePlugin = new DatabasePlugin();
        pluginManager.registerInternalPlugin(databasePlugin);

        // only register plugins settings if external plugins are installed
        if (!pluginManager.getExternalPlugins().isEmpty()) {
            pluginManager.registerInternalPlugin(new PluginsOverviewPlugin());
        }

        // initialize all GUI plugins
        Locale locale = new Locale(config.getGlobalConfig().getLanguage().value());
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin instanceof GuiExtension) {
                ((GuiExtension) plugin).initGuiExtension(impExpGui, locale);
            }
        }

        // register preferences plugin
        PreferencesPlugin preferencesPlugin = new PreferencesPlugin(impExpGui);
        preferencesPlugin.initGuiExtension(impExpGui, locale);
        pluginManager.registerInternalPlugin(preferencesPlugin);

        // set view handler for database controller
        DatabaseController databaseController = ObjectRegistry.getInstance().getDatabaseController();
        databaseController.setConnectionViewHandler(databasePlugin.getConnectionViewHandler());
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        // set options on parent command
        parent.useDefaultConfiguration(true)
                .failOnADEExceptions(false)
                .failOnPluginExceptions(false);

        // load GUI configuration
        guiConfig = loadGuiConfig();
        String laf = GuiUtil.getLaf(guiConfig.getAppearance().getTheme());

        try {
            // set look and feel
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            log.error("Failed to install look and feel theme '" + laf + "'.", e);
            guiConfig.getAppearance().setTheme(Theme.LIGHT);
            FlatLightLaf.setup();
        }

        if (OSXAdapter.IS_MAC_OS) {
            OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/citydb/gui/logos/logo_small.png")));
            if (System.getProperty("apple.laf.useScreenMenuBar") == null) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
        }

        // set UI defaults
        int offset = Math.max(16 - UIManager.getIcon("CheckBox.icon").getIconWidth(), 0);
        int iconTextGap = UIManager.getInt("CheckBox.iconTextGap") + offset;
        UIManager.put("CheckBox.iconTextGap", iconTextGap);
        UIManager.put("RadioButton.iconTextGap", iconTextGap);
        UIManager.put("TitlePane.centerTitleIfMenuBarEmbedded", false);

        // enable window decorations on Linux
        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        // splash screen
        if (!hideSplash) {
            splashScreen = new SplashScreen(5, 487, Color.BLACK);
            splashScreen.setMessage("Version \"" + getClass().getPackage().getImplementationVersion() + "\"");
            parent.withStartupProgressListener(this);
            splashScreen.setVisible(true);
        }
    }

    @Override
    public void setProcessSteps(int processSteps) {
        this.processSteps = processSteps + 1;
    }

    @Override
    public void nextStep(String message, int step) {
        splashScreen.setMessage(message);
        splashScreen.nextStep(step, processSteps);
    }
}
