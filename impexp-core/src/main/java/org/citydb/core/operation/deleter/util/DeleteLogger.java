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

package org.citydb.core.operation.deleter.util;

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.core.util.CoreConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeleteLogger {
    private final LocalDateTime date = LocalDateTime.now();
    private final Path logFile;
    private final BufferedWriter writer;
    private final DeleteMode mode;

    public DeleteLogger(Path logFile, DeleteMode mode, boolean autoCommit, DatabaseConnection connection) throws IOException {
        Path defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DELETE_LOG_DIR);
        if (logFile.toAbsolutePath().normalize().startsWith(defaultDir)) {
            Files.createDirectories(defaultDir);
        }

        if (Files.exists(logFile) && Files.isDirectory(logFile)) {
            logFile = logFile.resolve(getDefaultLogFileName());
        } else if (!Files.exists(logFile.getParent())) {
            Files.createDirectories(logFile.getParent());
        }

        this.logFile = logFile;
        this.mode = mode;
        writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8);
        writeHeader(autoCommit, connection);
    }

    public Path getLogFilePath() {
        return logFile;
    }

    private void writeHeader(boolean autoCommit, DatabaseConnection connection) throws IOException {
        writer.write('#' + getClass().getPackage().getImplementationTitle() +
                ", version \"" + getClass().getPackage().getImplementationVersion() + "\"");
        writer.newLine();
        writer.write("#Delete mode: " + (mode == DeleteMode.TERMINATE ? "Terminate" : "Delete"));
        writer.newLine();
        writer.write("#Database connection: ");
        writer.write(connection.toConnectString());
        writer.newLine();
        writer.write("#Timestamp: ");
        writer.write(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.newLine();
        writer.write("#Auto-commit: " + autoCommit);
        writer.newLine();
        writer.write("FEATURE_TYPE,CITYOBJECT_ID,GMLID");
        writer.newLine();
    }

    private void writeFooter(boolean success) throws IOException {
        writer.write("#" + (mode == DeleteMode.TERMINATE ? "Terminate" : "Delete"));
        writer.write(success ? " successfully finished." : " aborted.");
    }

    public void write(String type, long id, String gmlId) throws IOException {
        writer.write(type + "," + id + "," + (gmlId != null ? gmlId : "") + System.lineSeparator());
    }

    public String getDefaultLogFileName() {
        return "deleted-features-" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")) + ".log";
    }

    public void close(boolean success) throws IOException {
        writeFooter(success);
        writer.close();
    }
}