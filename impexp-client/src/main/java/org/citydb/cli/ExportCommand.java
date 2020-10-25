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
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.database.DatabaseController;
import org.citydb.log.Logger;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.cli.DatabaseOptions;
import org.citydb.plugin.cli.FileOutputOptions;
import org.citydb.plugin.cli.QueryOptions;
import org.citydb.registry.ObjectRegistry;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Exports data in CityGML format.",
        versionProvider = ImpExpCli.class
)
public class ExportCommand extends CliCommand {
    @CommandLine.ArgGroup(exclusive = false)
    private DatabaseOptions databaseOptions;

    @CommandLine.Mixin
    private FileOutputOptions outputOptions;

    @CommandLine.Option(names = "--output-encoding", defaultValue = "UTF-8",
            description = "Encoding used for the output file (default: ${DEFAULT-VALUE}).")
    private String encoding;

    @CommandLine.Mixin
    private QueryOptions queryOptions;

    private final Logger log = Logger.getInstance();

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        DatabaseController database = ObjectRegistry.getInstance().getDatabaseController();
        DatabaseConnection connection = databaseOptions != null && databaseOptions.isValid() ?
                databaseOptions.toDatabaseConnection() :
                config.getDatabaseConfig().getActiveConnection();

        if (!database.connect(connection)) {
            log.warn("Database export aborted.");
            return 1;
        }

        config.getExportConfig().getCityGMLOptions().setFileEncoding(encoding);

        try {
            new Exporter().doExport(outputOptions.getFile());
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

    @Override
    public void preprocess() throws Exception {
        if (queryOptions != null) {
            queryOptions.validate();
        }
    }
}
