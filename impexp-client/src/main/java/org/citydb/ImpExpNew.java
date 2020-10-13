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

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.cli.ExportCommand;
import org.citydb.config.project.global.LogLevel;
import org.citydb.log.Logger;
import org.citydb.plugin.CLICommand;
import org.citydb.plugin.Plugin;
import org.citydb.plugin.PluginManager;
import org.citydb.util.ClientConstants;
import org.citydb.util.Util;
import picocli.CommandLine;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

@CommandLine.Command(
        name = ClientConstants.CLI_NAME,
        description = "Command-line interface for the 3D City Database.",
        synopsisSubcommandLabel = "COMMAND",
        versionProvider = ImpExpNew.class
)
public class ImpExpNew extends CLICommand implements CommandLine.IVersionProvider {
    @CommandLine.Option(names = {"-c", "--config"}, scope = CommandLine.ScopeType.INHERIT, paramLabel = "<file>",
            description = "Use configuration from this file.")
    private Path configFile;

    @CommandLine.Option(names = "--log-level", scope = CommandLine.ScopeType.INHERIT, paramLabel = "<level>",
            description = "Log level: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private LogLevel logLevel = LogLevel.INFO;

    @CommandLine.Option(names = "--log-file", scope = CommandLine.ScopeType.INHERIT, paramLabel = "<file>",
            description = "Write log messages to the specified file.")
    private Path logFile;

    @CommandLine.Option(names = "--pid-file", scope = CommandLine.ScopeType.INHERIT, paramLabel = "<file>",
            description = "Create a file containing the current process ID.")
    private Path pidFile;

    private final Logger log = Logger.getInstance();
    private final PluginManager pluginManager = PluginManager.getInstance();
    private final ADEExtensionManager adeManager = ADEExtensionManager.getInstance();
    private final Util.URLClassLoader classLoader = new Util.URLClassLoader(Thread.currentThread().getContextClassLoader());
    private String commandLineString;
    private String subCommandName;

    public static void main(String[] args) {
        ImpExpNew impExp = new ImpExpNew();
        try {
            System.exit(impExp.doMain(args));
        } catch (Exception e) {
            impExp.logError(e);
            System.exit(1);
        }
    }

    public ImpExpNew withCLICommand(CLICommand command) {
        pluginManager.registerCLICommand(command);
        return this;
    }

    public ImpExpNew withPlugin(Plugin plugin) {
        pluginManager.registerExternalPlugin(plugin);
        return this;
    }

    public ImpExpNew withADEExtension(ADEExtension extension) {
        if (extension.getBasePath() == null)
            extension.setBasePath(Paths.get(""));

        adeManager.loadExtension(extension);
        return this;
    }

    public int doMain(String[] args) throws Exception {
        CommandLine cmd = new CommandLine(this);

        // add predefined commands
        cmd.addSubcommand(new CommandLine.HelpCommand());
        cmd.addSubcommand(new ExportCommand());

        try {
            // load CLI commands from plugins
            loadClasses(ClientConstants.IMPEXP_HOME.resolve(ClientConstants.PLUGINS_DIR), classLoader);
            pluginManager.loadCLICommands(classLoader);
            for (CLICommand command : pluginManager.getCLICommands()) {
                cmd.addSubcommand(command);
            }
        } catch (IOException e) {
            throw new ImpExpException("Failed to initialize CLI commands from plugins.", e);
        }

        cmd.setCaseInsensitiveEnumValuesAllowed(true)
                .setAbbreviatedOptionsAllowed(true)
                .setAbbreviatedSubcommandsAllowed(true)
                .setExecutionStrategy(new CommandLine.RunAll());

        try {
            CommandLine.ParseResult parseResult = cmd.parseArgs(args);
            List<CommandLine> commandLines = parseResult.asCommandLineList();

            // check for help options
            for (CommandLine commandLine : commandLines) {
                if (commandLine.isUsageHelpRequested() || commandLine.isVersionHelpRequested()) {
                    return CommandLine.executeHelpRequest(parseResult);
                }
            }

            // check for subcommand
            if (!parseResult.hasSubcommand()) {
                throw new CommandLine.ParameterException(cmd, "Missing required subcommand.");
            }

            for (CommandLine commandLine : commandLines) {
                // read password from keyboard
                CommandLine.Model.OptionSpec passwordOption = commandLine.getParseResult().matchedOption("-p");
                if (passwordOption != null && passwordOption.getValue().equals("")) {
                    passwordOption.setValue(readPassword(commandLine.getParseResult()));
                }

                // preprocess commands
                Object command = commandLine.getCommand();
                if (command instanceof CLICommand) {
                    ((CLICommand) command).preprocess();
                }
            }

            commandLineString = ClientConstants.CLI_NAME + " " + String.join(" ", args);
            subCommandName = commandLines.get(1).getCommandName();

            // execute command
            int exitCode = cmd.getExecutionStrategy().execute(parseResult);

            return exitCode;
        } catch (CommandLine.ParameterException e) {
            cmd.getParameterExceptionHandler().handleParseException(e, args);
            return cmd.getCommandSpec().exitCodeOnInvalidInput();
        }
    }

    @Override
    public Integer call() throws Exception {
        log.info("Starting " + getClass().getPackage().getImplementationTitle() +
                ", version " + this.getClass().getPackage().getImplementationVersion());

        return 0;
    }

    private void loadClasses(Path path, Util.URLClassLoader classLoader) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> stream = Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().toLowerCase().endsWith(".jar"))) {
                stream.forEach(classLoader::addPath);
            }
        }
    }

    private void logError(Exception e) {
        log.error("Aborting due to a fatal " + e.getClass().getName() + " exception.");
        log.logStackTrace(e);
    }

    private String readPassword(CommandLine.ParseResult parseResult) {
        String prompt = "Enter password for " + parseResult.matchedOptionValue("-u", "") +": ";
        Console console = System.console();
        if (console != null) {
            char[] input = console.readPassword(prompt);
            return input != null ? new String(input) : null;
        } else {
            System.out.print(prompt);
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        }
    }

    @Override
    public String[] getVersion() {
        return new String[]{
                getClass().getPackage().getImplementationTitle() +
                        ", version " + this.getClass().getPackage().getImplementationVersion(),
                "(C) 2013-" + LocalDate.now().getYear() + " " + getClass().getPackage().getImplementationVendor()
        };
    }
}
