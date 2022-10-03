/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class DuplicateLogger {
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final BufferedWriter writer;
    private Path logFile;
    private boolean isTemporary;

    public DuplicateLogger(DatabaseConnection connection) throws IOException {
        logFile = Files.createTempFile("3dcitydb-duplicates-", ".log");

        boolean truncate = true;
        isTemporary = true;

        writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        writeHeader(connection, truncate);
    }

    public Path getLogFilePath() {
        return logFile;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    private void writeHeader(DatabaseConnection connection, boolean writeHeaderLine) throws IOException {
        writer.write('#' + getClass().getPackage().getImplementationTitle() +
                ", version \"" + getClass().getPackage().getImplementationVersion() + "\"\n");
        writer.write("#Database connection: ");
        writer.write(connection.toConnectString() + "\n");
        writer.write("#Timestamp: ");
        writer.write(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");

        if (writeHeaderLine) {
            writer.write("CITYOBJECT_ID,GMLID,FEATURE_TYPE_IN_DB,FEATURE_TYPE_IN_FILE,INPUT_FILE\n");
        }
    }

    private void writeFooter(boolean success) throws IOException {
        writer.write("#Checking for duplicates " + (success ? "successfully finished." : "aborted.") + "\n");
    }

    public void write(DuplicateLogEntry entry) throws IOException {
        writer.write(entry.id + "," + entry.gmlId + "," + entry.typeInDatabase + "," + entry.typeInFile + "," +
                entry.file + "\n");
    }

    public IdList toIdList() {
        IdList idList = new IdList().withDefaultCommentCharacter('#');
        idList.setFiles(Collections.singletonList(logFile.toAbsolutePath().toString()));
        idList.setIdColumnType(IdColumnType.DATABASE_ID);
        idList.setIdColumnIndex(1);
        idList.setHasHeader(true);

        return idList;
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
