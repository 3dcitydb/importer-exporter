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

package org.citydb;

import org.citydb.ade.ADEExtension;
import org.citydb.cli.ImpExpCli;
import org.citydb.gui.GuiCommand;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.Plugin;

public class ImpExpLauncher {
    private final ImpExpCli impExpCli = new ImpExpCli();
    private String[] args = new String[0];

    public static void main(String[] args) {
        new ImpExpLauncher().withArgs(args).start();
    }

    public ImpExpLauncher withCliCommand(CliCommand command) {
        impExpCli.withCliCommand(command);
        return this;
    }

    public ImpExpLauncher withPlugin(Plugin plugin) {
        impExpCli.withPlugin(plugin);
        return this;
    }

    public ImpExpLauncher withADEExtension(ADEExtension extension) {
        impExpCli.withADEExtension(extension);
        return this;
    }

    public ImpExpLauncher withArgs(String[] args) {
        if (args != null) {
            this.args = args;
        }

        return this;
    }

    public void start() {
        int exitCode = impExpCli
                .withDefaultCommand(GuiCommand.NAME)
                .start(args);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
