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

import com.formdev.flatlaf.FlatLightLaf;
import org.citydb.ImpExpException;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.gui.GuiConfig;
import org.citydb.config.gui.style.Theme;
import org.citydb.database.DatabaseController;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.components.SplashScreen;
import org.citydb.gui.modules.database.DatabasePlugin;
import org.citydb.gui.modules.exporter.CityGMLExportPlugin;
import org.citydb.gui.modules.importer.CityGMLImportPlugin;
import org.citydb.gui.modules.kml.KMLExportPlugin;
import org.citydb.gui.modules.preferences.PreferencesPlugin;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.log.Logger;
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
                .resolve(ClientConstants.CONFIG_DIR)
                .resolve(ClientConstants.GUI_SETTINGS_FILE);

        Object object = null;
        try {
            object = ConfigUtil.getInstance().unmarshal(guiConfigFile.toFile());
        } catch (JAXBException | IOException e) {
            //
        }

        return object instanceof GuiConfig ? (GuiConfig) object : new GuiConfig();
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
    public void preprocess(CommandLine commandLine) throws Exception {
        // set options on parent command
        parent.useDefaultConfiguration(true)
                .failOnADEExceptions(false);

        // load GUI configuration
        guiConfig = loadGuiConfig();
        String laf = GuiUtil.getLaf(guiConfig.getAppearance().getTheme());

        try {
            // set look and feel
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            log.error("Failed to install look and feel theme '" + laf + "'.", e);
            guiConfig.getAppearance().setTheme(Theme.LIGHT);
            FlatLightLaf.install();
        }

        if (OSXAdapter.IS_MAC_OS) {
            OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/citydb/gui/logos/logo_small.png")));
            if (System.getProperty("apple.laf.useScreenMenuBar") == null) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
        }

        // enable window decorations
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // set UI defaults
        int offset = Math.max(16 - UIManager.getIcon("CheckBox.icon").getIconWidth(), 0);
        int iconTextGap = UIManager.getInt("CheckBox.iconTextGap") + offset;
        UIManager.put("CheckBox.iconTextGap", iconTextGap);
        UIManager.put("RadioButton.iconTextGap", iconTextGap);

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
