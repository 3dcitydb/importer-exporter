package org.citydb.util;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoreConstants {
    public static final Path IMPEXP_DATA_DIR = Paths.get(System.getProperty("user.home"), "3dcitydb", "importer-exporter");
    public static final String IMPORT_LOG_DIR = "imported-features";
    public static final URL CITYDB_SCHEMA_MAPPING_FILE = CoreConstants.class.getResource("/org/citydb/database/schema/3dcitydb-schema.xml");

    public static final String DEFAULT_DELIMITER = "--/\\--";
    public static final String OBJECT_ORIGINAL_GMLID = "origGMLId";
    public static final String GEOMETRY_XLINK = "isXlink";
    public static final String GEOMETRY_ORIGINAL = "origGeom";
    public static final String GEOMETRY_INVALID = "geomInvalid";
    public static final String TEXTURE_IMAGE_XLINK = "textureXlink";
    public static final String FOREIGN_KEYS_SET = "foreignKeys";
    public static final String EXPORT_STUB = "exportStub";
    public static final String EXPORT_AS_ADDITIONAL_OBJECT = "additionalObject";
    public static final String UNIQUE_TEXTURE_FILENAME_PREFIX = "tex_";
}
