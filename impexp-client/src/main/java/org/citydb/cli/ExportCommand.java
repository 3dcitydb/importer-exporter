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

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.controller.Exporter;
import org.citydb.cli.options.export.QueryOption;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.database.DatabaseController;
import org.citydb.log.Logger;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.cli.DatabaseOption;
import org.citydb.registry.ObjectRegistry;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(
        name = "export",
        description = "Exports data in CityGML format.",
        versionProvider = ImpExpCli.class
)
public class ExportCommand extends CliCommand {
    @CommandLine.Option(names = {"-o", "--output"}, required = true,
            description = "Name of the output file.")
    private Path file;

    @CommandLine.Option(names = "--output-encoding", defaultValue = "UTF-8",
            description = "Encoding used for the output file (default: ${DEFAULT-VALUE}).")
    private String encoding;

    @CommandLine.ArgGroup(exclusive = false, heading = "Query and filter options:%n")
    private QueryOption queryOption;

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
            log.warn("Database export aborted.");
            return 1;
        }

        // set file encoding
        config.getExportConfig().getCityGMLOptions().setFileEncoding(encoding);

        // set user-defined query options
        if (queryOption != null) {
            config.getExportConfig().setQuery(queryOption.toQueryConfig());
        }

        try {
            new Exporter().doExport(file);
            log.info("Database export successfully finished.");
        } catch (CityGMLExportException e) {
            log.error(e.getMessage(), e.getCause());
            log.warn("Database export aborted.");
            return 1;
        } finally {
            database.disconnect(true);
        }

        return 0;
    }
}
