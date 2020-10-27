/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

package org.citydb.cli;

import org.citydb.ImpExpException;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.GuiConfig;
import org.citydb.database.DatabaseController;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.SplashScreen;
import org.citydb.gui.modules.database.DatabasePlugin;
import org.citydb.gui.modules.exporter.CityGMLExportPlugin;
import org.citydb.gui.modules.importer.CityGMLImportPlugin;
import org.citydb.gui.modules.kml.KMLExportPlugin;
import org.citydb.gui.modules.preferences.PreferencesPlugin;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.InternalPlugin;
import org.citydb.plugin.Plugin;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.cli.StartupProgressListener;
import org.citydb.plugin.extension.view.ViewExtension;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.CoreConstants;
import picocli.CommandLine;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@CommandLine.Command(
        name = GuiCommand.NAME,
        description = "Starts the graphical user interface.",
        versionProvider = ImpExpCli.class
)
public class GuiCommand extends CliCommand implements StartupProgressListener {
    public static final String NAME = "gui";

    @CommandLine.Option(names = "--no-splash", description = "Hide the splash screen during startup.")
    private boolean hideSplash;

    @CommandLine.ParentCommand
    private ImpExpCli parent;

    private final PluginManager pluginManager = PluginManager.getInstance();
    private int processSteps;
    private SplashScreen splashScreen;

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        // load GUI configuration
        loadGuiConfig(config);

        // initialize predefined GUI components
        ImpExpGui impExpGui = new ImpExpGui(parent.getConfigFile());
        initializeViewComponents(impExpGui, config);

        parent.logProgress("Starting graphical user interface");
        SwingUtilities.invokeLater(impExpGui::invoke);
        if (!hideSplash) {
            splashScreen.close();
        }

        return 0;
    }

    private void loadGuiConfig(Config config) {
        Path guiConfigFile = CoreConstants.IMPEXP_DATA_DIR
                .resolve(ClientConstants.CONFIG_DIR)
                .resolve(ClientConstants.GUI_SETTINGS_FILE);

        try {
            Object object = ConfigUtil.getInstance().unmarshal(guiConfigFile.toFile());
            if (object instanceof GuiConfig) {
                config.setGuiConfig((GuiConfig) object);
            }
        } catch (JAXBException | IOException e) {
            //
        }
    }

    private void initializeViewComponents(ImpExpGui impExpGui, Config config) {
        // create database plugin
        DatabasePlugin databasePlugin = new DatabasePlugin(impExpGui, config);
        DatabaseController databaseController = ObjectRegistry.getInstance().getDatabaseController();
        databaseController.setConnectionViewHandler(databasePlugin.getConnectionViewHandler());

        // register predefined internal plugins
        pluginManager.registerInternalPlugin(new CityGMLImportPlugin(impExpGui, config));
        pluginManager.registerInternalPlugin(new CityGMLExportPlugin(impExpGui, config));
        pluginManager.registerInternalPlugin(new KMLExportPlugin(impExpGui, config));
        pluginManager.registerInternalPlugin(databasePlugin);
        pluginManager.registerInternalPlugin(new PreferencesPlugin(impExpGui, config));

        // initialize all GUI plugins
        Locale locale = new Locale(config.getGlobalConfig().getLanguage().value());
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (plugin instanceof ViewExtension) {
                ((ViewExtension) plugin).initViewExtension(impExpGui, locale);
                if (!hideSplash && !(plugin instanceof InternalPlugin)) {
                    splashScreen.setMessage("Initializing plugin " + plugin.getClass().getName());
                }
            }
        }
    }

    @Override
    public void preprocess() throws Exception {
        // set options on parent command
        parent.useDefaultConfiguration(true)
                .failOnADEExceptions(false);

        // set look & feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            if (OSXAdapter.IS_MAC_OS_X) {
                OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/citydb/gui/images/common/logo_small.png")));
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
        } catch (Exception e) {
            throw new ImpExpException("Failed to initialize user interface.", e);
        }

        // splash screen
        if (!hideSplash) {
            splashScreen = new SplashScreen(3, 477, Color.BLACK);
            splashScreen.setMessage("Version \"" + getClass().getPackage().getImplementationVersion() + "\"");
            parent.withStartupProgressListener(this);
            SwingUtilities.invokeLater(() -> splashScreen.setVisible(true));
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
