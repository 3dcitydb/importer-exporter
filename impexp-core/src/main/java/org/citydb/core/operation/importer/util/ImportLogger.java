/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
package org.citydb.core.operation.importer.util;

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.importer.ImportLog;
import org.citydb.config.project.importer.ImportLogFileMode;
import org.citydb.core.util.CoreConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImportLogger {
    private final LocalDateTime date = LocalDateTime.now();
    private final BufferedWriter writer;
    private Path logFile;
    private String inputFile = "";

    public ImportLogger(ImportLog importLog, DatabaseConnection connection) throws IOException {
        Path defaultLogDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR);
        if (importLog.isSetLogFile()) {
            logFile = Paths.get(importLog.getLogFile());
            if (logFile.equals(defaultLogDir)) {
                logFile = logFile.resolve("imported-features.log");
            }
        } else {
            logFile = defaultLogDir.resolve("imported-features.log");
        }

        if (importLog.getLogFileMode() == ImportLogFileMode.UNIQUE) {
            logFile = logFile.resolveSibling(getUniqueFileName(logFile.getFileName().toString()));
        }

        boolean writeHeaderLine = true;
        if (!Files.exists(logFile.getParent())) {
            Files.createDirectories(logFile.getParent());
        } else if (importLog.getLogFileMode() == ImportLogFileMode.APPEND) {
            writeHeaderLine = !Files.exists(logFile);
        }

        writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                importLog.getLogFileMode() == ImportLogFileMode.TRUNCATE ?
                        StandardOpenOption.TRUNCATE_EXISTING :
                        StandardOpenOption.APPEND);

        writeHeader(connection, writeHeaderLine);
    }

    public Path getLogFilePath() {
        return logFile;
    }

    public void setInputFile(Path inputFile) {
        this.inputFile = inputFile != null ? inputFile.toAbsolutePath().toString() : "";
    }

    private void writeHeader(DatabaseConnection connection, boolean writeHeaderLine) throws IOException {
        writer.write('#' + getClass().getPackage().getImplementationTitle() +
                ", version \"" + getClass().getPackage().getImplementationVersion() + "\"");
        writer.newLine();
        writer.write("#Database connection: ");
        writer.write(connection.toConnectString());
        writer.newLine();
        writer.write("#Timestamp: ");
        writer.write(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.newLine();

        if (writeHeaderLine) {
            writer.write("FEATURE_TYPE,CITYOBJECT_ID,GMLID_IN_FILE,INPUT_FILE");
            writer.newLine();
        }
    }

    private void writeFooter(boolean success) throws IOException {
        writer.write(success ? "#Import successfully finished." : "#Import aborted.");
        writer.newLine();
    }

    public void write(ImportLogEntry entry) throws IOException {
        writer.write(entry.type + "," + entry.id + "," + entry.gmlId + "," + inputFile + System.lineSeparator());
    }

    private String getUniqueFileName(String fileName) {
        String suffix = date.format(DateTimeFormatter.ofPattern("-yyyy-MM-dd_HH-mm-ss-SSS"));
        int index = fileName.lastIndexOf('.');
        return index != -1 ?
                fileName.substring(0, index) + suffix + fileName.substring(index) :
                fileName + suffix;
    }

    public void close(boolean success) throws IOException {
        writeFooter(success);
        writer.close();
    }

    public static class ImportLogEntry {
        private final String type;
        private final long id;
        private final String gmlId;

        private ImportLogEntry(String type, long id, String gmlId) {
            this.type = type;
            this.id = id;
            this.gmlId = gmlId != null ? gmlId : "";
        }

        public static ImportLogEntry of(String type, long id, String gmlId) {
            return new ImportLogEntry(type, id, gmlId);
        }
    }
}
