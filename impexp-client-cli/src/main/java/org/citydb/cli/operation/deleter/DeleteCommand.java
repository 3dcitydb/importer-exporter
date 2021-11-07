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

package org.citydb.cli.operation.deleter;

import org.citydb.cli.option.DatabaseOption;
import org.citydb.config.Config;
import org.citydb.config.project.common.IdList;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.deleter.DeleteConfig;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.operation.common.csv.IdListPreviewer;
import org.citydb.core.operation.deleter.DeleteException;
import org.citydb.core.operation.deleter.controller.Deleter;
import org.citydb.core.plugin.CliCommand;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.log.Logger;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
        name = "delete",
        description = "Deletes top-level city objects from the database."
)
public class DeleteCommand extends CliCommand {
    enum Mode {delete, terminate};

    @CommandLine.Option(names = {"-m", "--delete-mode"}, paramLabel = "<mode>", defaultValue = "delete",
            description = "Delete mode: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode mode;

    @CommandLine.Option(names = {"-v", "--preview"},
            description = "Run in preview mode. Affected city objects will be neither deleted nor terminated.")
    private boolean preview;

    @CommandLine.Option(names = "--delete-log", paramLabel = "<file>",
            description = "Record deleted top-level features to this file.")
    private Path deleteLogFile;

    @CommandLine.ArgGroup(exclusive = false)
    private CleanupOption cleanupOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Metadata options:%n")
    private MetadataOption metadataOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Query and filter options:%n")
    private QueryOption queryOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Delete list options:%n")
    private DeleteListOption deleteListOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Database connection options:%n")
    private final DatabaseOption databaseOption = new DatabaseOption();

    private final Logger log = Logger.getInstance();

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        // set delete list options
        config.getDeleteConfig().setUseDeleteList(deleteListOption != null);
        if (deleteListOption != null) {
            IdList deleteList = deleteListOption.toDeleteList();
            config.getDeleteConfig().setDeleteList(deleteList);

            if (deleteListOption.isPreview()) {
                try {
                    IdListPreviewer.of(deleteList)
                            .withNumberOfRecords(20)
                            .printToConsole();
                    log.info("Delete list preview successfully finished.");
                    return 0;
                } catch (Exception e) {
                    log.error("Failed to create a preview of the delete list.", e);
                    return 1;
                }
            }
        }

        // connect to database
        DatabaseController database = ObjectRegistry.getInstance().getDatabaseController();
        DatabaseConnection connection = databaseOption.hasUserInput() ?
                databaseOption.toDatabaseConnection() :
                config.getDatabaseConfig().getActiveConnection();

        if (!database.connect(connection)) {
            log.warn("Database " + mode.name() + " aborted.");
            return 1;
        }

        setDeleteOptions(config.getDeleteConfig());

        // set user-defined query options
        if (queryOption != null) {
            config.getDeleteConfig().setUseSimpleQuery(false);
            config.getDeleteConfig().setQuery(queryOption.toQueryConfig());
        }

        try {
            new Deleter().doDelete(preview);
            log.info("Database " + mode.name() + " successfully finished.");
        } catch (DeleteException e) {
            log.error(e.getMessage(), e.getCause());
            log.warn("Database " + mode.name() + " aborted.");
            return 1;
        } finally {
            database.disconnect(true);
        }

        return 0;
    }

    private void setDeleteOptions(DeleteConfig deleteConfig) {
        if (mode != null) {
            deleteConfig.setMode(mode == Mode.terminate ?
                    DeleteMode.TERMINATE :
                    DeleteMode.DELETE);
        }

        if (deleteLogFile != null) {
            deleteConfig.getDeleteLog().setLogFile(deleteLogFile.toAbsolutePath().toString());
            deleteConfig.getDeleteLog().setLogDeletedFeatures(true);
        }

        if (cleanupOption != null) {
            deleteConfig.setCleanupGlobalAppearances(cleanupOption.isCleanupGlobalAppearances());
        }

        if (metadataOption != null) {
            deleteConfig.setContinuation(metadataOption.toContinuation());
        }
    }
}
