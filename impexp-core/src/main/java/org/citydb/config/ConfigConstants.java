package org.citydb.config;

import org.citydb.config.internal.Internal;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigConstants {
    public static final Path IMPEXP_DATA_DIR = Paths.get(System.getProperty("user.home"), "3dcitydb", "importer-exporter");
    public static final String LOG_DIR = "log";
    public static final String IMPORT_LOG_DIR = LOG_DIR + File.separator + "imported-features";
    public static final URL CITYDB_SCHEMA_MAPPING_FILE = Internal.class.getResource("/database/schema/3dcitydb-schema.xml");
}
