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

package org.citydb;

import org.citydb.config.project.global.LogLevel;
import org.citydb.log.Logger;
import org.citydb.util.ClientConstants;
import picocli.CommandLine;

import java.time.LocalDate;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = ClientConstants.CLI_NAME,
        description = "Command-line interface for the 3D City Database.",
        mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "COMMAND",
        showAtFileInUsageHelp = true,
        versionProvider = ImpExpNew.class
)
public class ImpExpNew implements Callable<Integer>, CommandLine.IVersionProvider {
    @CommandLine.Option(names = "--log-level", scope = CommandLine.ScopeType.INHERIT, paramLabel = "<level>",
            defaultValue = "info", description = "Log level: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private LogLevel logLevel;

    private final Logger log = Logger.getInstance();

    public static void main(String[] args) throws Exception {
        System.exit(new ImpExpNew().doMain(args));
    }

    public int doMain(String[] args) throws Exception {
        CommandLine commandLine = new CommandLine(this)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionStrategy(new CommandLine.RunAll())
                .setAbbreviatedOptionsAllowed(true)
                .setAbbreviatedSubcommandsAllowed(true);

        try {
            CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
            if (commandLine.isUsageHelpRequested() || commandLine.isVersionHelpRequested()) {
                return CommandLine.executeHelpRequest(parseResult);
            }

            return 0;
        } catch (CommandLine.ParameterException e) {
            commandLine.getParameterExceptionHandler().handleParseException(e, args);
            return commandLine.getCommandSpec().exitCodeOnInvalidInput();
        } catch (Throwable e) {
            // exception occurred in business logic
            log.error("The following unexpected error occurred during execution.");
            log.logStackTrace(e);
            return commandLine.getCommandSpec().exitCodeOnExecutionException();
        }
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }

    @Override
    public String[] getVersion() {
        return new String[]{
                getClass().getPackage().getImplementationTitle() +
                        ", version " + this.getClass().getPackage().getImplementationVersion(),
                "(c) 2013-" + LocalDate.now().getYear() + " " + getClass().getPackage().getImplementationVendor()
        };
    }
}
