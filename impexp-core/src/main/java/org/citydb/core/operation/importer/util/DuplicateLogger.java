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

import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.importer.DuplicateLog;
import org.citydb.core.util.CoreConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class DuplicateLogger {
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final BufferedWriter writer;
    private final Path logFile;
    private final boolean isTemporary;

    public DuplicateLogger(DuplicateLog duplicateLog, DatabaseConnection connection) throws IOException {
        if (duplicateLog == null || !duplicateLog.isSetLogDuplicates()) {
            logFile = Files.createTempFile("3dcitydb-duplicates-", ".log");
            isTemporary = true;
        } else {
            Path logFile = duplicateLog.isSetLogFile() ?
                    CoreConstants.WORKING_DIR.resolve(duplicateLog.getLogFile()) :
                    CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DELETE_LOG_DIR);

            Path defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DUPLICATE_LOG_DIR);
            if (logFile.toAbsolutePath().normalize().startsWith(defaultDir)) {
                Files.createDirectories(defaultDir);
            }

            if (Files.exists(logFile) && Files.isDirectory(logFile)) {
                logFile = logFile.resolve(getDefaultLogFileName());
            } else if (!Files.exists(logFile.getParent())) {
                Files.createDirectories(logFile.getParent());
            }

            this.logFile = logFile;
            isTemporary = false;
        }

        writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8);
        writeHeader(connection);
    }

    public Path getLogFilePath() {
        return logFile;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    private void writeHeader(DatabaseConnection connection) throws IOException {
        writer.write('#' + getClass().getPackage().getImplementationTitle() +
                ", version \"" + getClass().getPackage().getImplementationVersion() + "\"");
        writer.newLine();
        writer.write("#Database connection: ");
        writer.write(connection.toConnectString());
        writer.newLine();
        writer.write("#Timestamp: ");
        writer.write(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.newLine();
        writer.write("CITYOBJECT_ID,GMLID,FEATURE_TYPE_IN_DB,FEATURE_TYPE_IN_FILE,INPUT_FILE");
        writer.newLine();
    }

    private void writeFooter(boolean success) throws IOException {
        writer.write("#Check for duplicates " + (success ? "successfully finished." : "aborted."));
    }

    public void write(DuplicateLogEntry entry) throws IOException {
        writer.write(entry.id + "," + entry.gmlId + "," + entry.typeInDatabase + "," + entry.typeInFile + "," +
                entry.file + System.lineSeparator());
    }

    public IdList toIdList(IdColumnType columnType) {
        IdList idList = new IdList().withDefaultCommentCharacter('#');
        idList.setFiles(Collections.singletonList(logFile.toAbsolutePath().toString()));
        idList.setIdColumnType(columnType);
        idList.setIdColumnIndex(columnType == IdColumnType.DATABASE_ID ? 1 : 2);
        idList.setHasHeader(true);

        return idList;
    }

    public String getDefaultLogFileName() {
        return "duplicate-features-" + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")) + ".log";
    }

    public void close(boolean success) throws IOException {
        writeFooter(success);
        writer.close();
    }

    public static class DuplicateLogEntry {
        private final long id;
        private final String gmlId;
        private final String typeInDatabase;
        private final String typeInFile;
        private final String file;

        private DuplicateLogEntry(long id, String gmlId, String typeInDatabase, String typeInFile, String file) {
            this.id = id;
            this.gmlId = gmlId != null ? gmlId : "";
            this.typeInDatabase = typeInDatabase;
            this.typeInFile = typeInFile;
            this.file = file;
        }

        public static DuplicateLogEntry of(long id, String gmlId, String typeInDatabase, String typeInFile, String file) {
            return new DuplicateLogEntry(id, gmlId, typeInDatabase, typeInFile, file);
        }
    }
}
