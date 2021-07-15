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

package org.citydb.cli.operation.importer;

import org.citydb.cli.ImpExpCli;
import org.citydb.cli.ImpExpException;
import org.citydb.cli.option.CliOptionBuilder;
import org.citydb.cli.option.DatabaseOption;
import org.citydb.cli.option.ThreadPoolOption;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.importer.ImportConfig;
import org.citydb.config.project.importer.ImportList;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.operation.common.csv.IdListPreviewer;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.controller.Importer;
import org.citydb.core.plugin.CliCommand;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.util.log.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@CommandLine.Command(
        name = "import",
        description = "Imports data in CityGML or CityJSON format.",
        versionProvider = ImpExpCli.class
)
public class ImportCommand extends CliCommand {
    @CommandLine.Parameters(paramLabel = "<file>", arity = "1",
            description = "Files or directories to import (glob patterns allowed).")
    private String[] files;

    @CommandLine.Option(names = "--input-encoding",
            description = "Encoding of the input file(s).")
    private String encoding;

    @CommandLine.Option(names = "--import-log", paramLabel = "<file>",
            description = "Record imported top-level features to this file.")
    private Path importLogFile;

    @CommandLine.ArgGroup
    private ThreadPoolOption threadPoolOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Metadata options:%n")
    private MetadataOption metadataOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Import filter options:%n")
    private FilterOption filterOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Import list options:%n")
    private ImportListOption importListOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Database connection options:%n")
    private DatabaseOption databaseOption;

    private final Logger log = Logger.getInstance();

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        // get import list and preview it if required
        ImportList importList = null;
        if (importListOption != null) {
            importList = importListOption.toImportList();

            if (importListOption.isPreview()) {
                try {
                    IdListPreviewer.of(importList)
                            .withNumberOfRecords(20)
                            .printToConsole();
                    log.info("Import list preview successfully finished.");
                    return 0;
                } catch (Exception e) {
                    log.error("Failed to create a preview of the import list.", e);
                    return 1;
                }
            }
        }

        List<Path> inputFiles;
        try {
            log.debug("Parsing and resolving input file parameters.");
            inputFiles = CliOptionBuilder.inputFiles(files, CoreConstants.WORKING_DIR);

            if (inputFiles.isEmpty()) {
                log.error("Failed to find input files for the provided parameters: " + String.join(", ", files));
                log.warn("Database import aborted.");
                return 1;
            }
        } catch (IOException e) {
            throw new ImpExpException("Failed to parse input file parameters.", e);
        }

        // connect to database
        DatabaseController database = ObjectRegistry.getInstance().getDatabaseController();
        DatabaseConnection connection = databaseOption != null ?
                databaseOption.toDatabaseConnection() :
                config.getDatabaseConfig().getActiveConnection();

        if (!database.connect(connection)) {
            log.warn("Database import aborted.");
            return 1;
        }

        // set general import options
        setImportOptions(config.getImportConfig());

        // set filter options
        if (filterOption != null) {
            config.getImportConfig().setFilter(filterOption.toImportFilter());
        }

        // set import list options
        config.getImportConfig().getFilter().setUseImportListFilter(importList != null);
        config.getImportConfig().getFilter().setImportList(importList);

        try {
            new Importer().doImport(inputFiles);
            log.info("Database import successfully finished.");
        } catch (CityGMLImportException e) {
            log.error(e.getMessage(), e.getCause());
            log.warn("Database import aborted.");
            return 1;
        } finally {
            database.disconnect(true);
        }

        return 0;
    }

    private void setImportOptions(ImportConfig importConfig) {
        importConfig.getGeneralOptions().setFileEncoding(encoding);

        if (importLogFile != null) {
            importConfig.getImportLog().setLogFile(importLogFile.toAbsolutePath().toString());
            importConfig.getImportLog().setLogImportedFeatures(true);
        }

        if (metadataOption != null) {
            importConfig.setContinuation(metadataOption.toContinuation());
        }

        if (filterOption != null) {
            importConfig.getAppearances().setImportAppearances(filterOption.isImportAppearances());
        }

        if (threadPoolOption != null) {
            importConfig.getResources().setThreadPool(threadPoolOption.toThreadPool());
        }
    }
}
