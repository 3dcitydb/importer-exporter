package org.citydb;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImpExpConstants {
    public static final Path IMPEXP_HOME;
    public static final Path WORKING_DIR;

    public static final String SRS_TEMPLATES_DIR = "templates" + File.separator + "CoordinateReferenceSystems";
    public static final String PLUGINS_DIR = "plugins";
    public static final String ADE_EXTENSIONS_DIR = "ade-extensions";
    public static final String CONFIG_DIR = "config";
    public static final String PROJECT_SETTINGS_FILE = "project.xml";
    public static final String GUI_SETTINGS_FILE = "gui.xml";

    static {
        String impexpHomeEnv = System.getenv("APP_HOME");
        if (impexpHomeEnv == null)
            impexpHomeEnv = ".";

        String workingDirEnv = System.getenv("WORKING_DIR");
        if (workingDirEnv == null)
            workingDirEnv = ".";

        IMPEXP_HOME = Paths.get(impexpHomeEnv).normalize().toAbsolutePath();
        WORKING_DIR = Paths.get(workingDirEnv).normalize().toAbsolutePath();
    }
}