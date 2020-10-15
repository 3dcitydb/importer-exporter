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
import org.citydb.ImpExpNew;
import org.citydb.gui.components.SplashScreen;
import org.citydb.gui.util.OSXAdapter;
import org.citydb.plugin.CLICommand;
import picocli.CommandLine;

import javax.swing.*;
import java.awt.*;

@CommandLine.Command(
        name = "gui",
        description = "Starts the graphical user interface.",
        hidden = true,
        versionProvider = ImpExpNew.class
)
public class GuiCommand extends CLICommand {

    @CommandLine.Option(names = "--no-splash", description = "Hide the splash screen during startup.")
    private boolean hideSplash;

    @CommandLine.ParentCommand
    private ImpExpNew parent;

    private SplashScreen splashScreen;

    @Override
    public Integer call() throws Exception {
        try {
            return 0;
        } finally {
            if (splashScreen != null) {
                splashScreen.close();
            }
        }
    }

    @Override
    public void preprocess() throws Exception {
        try {
            // set look & feel
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            if (OSXAdapter.IS_MAC_OS_X) {
                OSXAdapter.setDockIconImage(Toolkit.getDefaultToolkit().getImage(parent.getClass().getResource("/org/citydb/gui/images/common/logo_small.png")));
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
        } catch (Exception e) {
            throw new ImpExpException("Failed to initialize user interface.", e);
        }

        // show splash screen
        if (!hideSplash) {
            splashScreen = new SplashScreen(3, 477, Color.BLACK);
            splashScreen.setMessage("Version \"" + parent.getClass().getPackage().getImplementationVersion() + "\"");
            SwingUtilities.invokeLater(() -> splashScreen.setVisible(true));
            parent.withStartupProgressListener(splashScreen);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //
            }
        }
    }
}
