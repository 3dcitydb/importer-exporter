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

import org.citydb.citygml.deleter.CityGMLDeleteException;
import org.citydb.citygml.deleter.controller.Deleter;
import org.citydb.cli.options.deleter.DeleteListOption;
import org.citydb.cli.options.deleter.QueryOption;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.database.DatabaseController;
import org.citydb.log.Logger;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.cli.DatabaseOption;
import org.citydb.registry.ObjectRegistry;
import picocli.CommandLine;

@CommandLine.Command(
        name = "delete",
        description = "Deletes top-level city objects from the database.",
        versionProvider = ImpExpCli.class
)
public class DeleteCommand extends CliCommand {
    enum Mode {delete, terminate};

    @CommandLine.Option(names = {"-m", "--delete-mode"}, paramLabel = "<mode>", defaultValue = "delete",
            description = "Delete mode: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode mode;

    @CommandLine.ArgGroup(exclusive = false, heading = "Query and filter options:%n")
    private QueryOption queryOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Delete list options:%n")
    private DeleteListOption deleteListOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Database connection options:%n")
    private DatabaseOption databaseOption;

    private final Logger log = Logger.getInstance();

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        // connect to database
        DatabaseController database = ObjectRegistry.getInstance().getDatabaseController();
        DatabaseConnection connection = databaseOption != null ?
                databaseOption.toDatabaseConnection() :
                config.getDatabaseConfig().getActiveConnection();

        if (!database.connect(connection)) {
            log.warn("Database delete aborted.");
            return 1;
        }

        // set delete mode
        config.getDeleteConfig().setMode(mode == Mode.terminate ?
                DeleteMode.TERMINATE :
                DeleteMode.DELETE);

        try {
            Deleter deleter = new Deleter();
            if (deleteListOption != null) {
                deleter.doDelete(deleteListOption.toDeleteListParser());
            } else {
                // set user-defined query options
                if (queryOption != null) {
                    config.getDeleteConfig().setQuery(queryOption.toQueryConfig());
                }

                deleter.doDelete();
            }

            log.info("Database delete successfully finished.");
        } catch (CityGMLDeleteException e) {
            log.error(e.getMessage(), e.getCause());
            log.warn("Database delete aborted.");
            return 1;
        } finally {
            database.disconnect(true);
        }

        return 0;
    }

    @Override
    public void preprocess(CommandLine commandLine) {
        if (queryOption != null && deleteListOption != null) {
            throw new CommandLine.ParameterException(commandLine,
                    "Error: Query options and delete file options are mutually exclusive (specify only one)");
        }
    }
}
