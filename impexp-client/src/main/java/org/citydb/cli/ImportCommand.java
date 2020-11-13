package org.citydb.cli;

import org.citydb.ImpExpException;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.controller.Importer;
import org.citydb.cli.options.importer.FilterOption;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.importer.ImportConfig;
import org.citydb.database.DatabaseController;
import org.citydb.log.Logger;
import org.citydb.plugin.CliCommand;
import org.citydb.plugin.cli.CliOptionBuilder;
import org.citydb.plugin.cli.DatabaseOption;
import org.citydb.plugin.cli.ThreadPoolOption;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
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

    @CommandLine.Option(names = "--import-log", paramLabel = "<file>",
            description = "Record imported top-level features to this file.")
    private Path importLogFile;

    @CommandLine.ArgGroup
    private ThreadPoolOption threadPoolOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Import filter options:%n")
    private FilterOption filterOption;

    @CommandLine.ArgGroup(exclusive = false, heading = "Database connection options:%n")
    private DatabaseOption databaseOption;

    private final Logger log = Logger.getInstance();

    @Override
    public Integer call() throws Exception {
        Config config = ObjectRegistry.getInstance().getConfig();

        List<Path> inputFiles;
        try {
            log.debug("Parsing and resolving input file parameters.");
            inputFiles = CliOptionBuilder.inputFiles(files, ClientConstants.WORKING_DIR);

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

        if (filterOption != null) {
            config.getImportConfig().setFilter(filterOption.toImportFilter());
        }

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
        if (importLogFile != null) {
            importConfig.getImportLog().setLogFile(importLogFile.toAbsolutePath().toString());
            importConfig.getImportLog().setLogImportedFeatures(true);
        }

        if (threadPoolOption != null) {
            importConfig.getResources().getThreadPool().setDefaultPool(threadPoolOption.toThreadPool());
        }
    }
}
