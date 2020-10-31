/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.util;

import org.citydb.config.project.database.DBConnection;
import org.citydb.util.CoreConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImportLogger {
	private final LocalDateTime date = LocalDateTime.now();
	private final Path logFile;
	private final BufferedWriter writer;

	public ImportLogger(Path logFile, Path importFile, DBConnection connection) throws IOException {
		if (logFile.toAbsolutePath().normalize().startsWith(CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR))) {
			Files.createDirectories(CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR));
		}

		if (Files.exists(logFile) && Files.isDirectory(logFile)) {
			logFile = logFile.resolve(getDefaultLogFileName());
		} else if (!Files.exists(logFile.getParent())) {
			Files.createDirectories(logFile.getParent());
		}

		this.logFile = logFile;
		writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8);
		writeHeader(importFile, connection);
	}

	public Path getLogFilePath() {
		return logFile;
	}

	private void writeHeader(Path fileName, DBConnection connection) throws IOException {
		writer.write('#' + getClass().getPackage().getImplementationTitle() +
				", version \"" + getClass().getPackage().getImplementationVersion() + "\"");
		writer.newLine();		
		writer.write("#Imported top-level features from file: ");
		writer.write(fileName.toAbsolutePath().toString());
		writer.newLine();
		writer.write("#Database connection string: ");
		writer.write(connection.toConnectString());
		writer.newLine();
		writer.write("#Timestamp: ");
		writer.write(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		writer.newLine();
		writer.write("FEATURE_TYPE,CITYOBJECT_ID,GMLID_IN_FILE");
		writer.newLine();
	}

	private void writeFooter(boolean success) throws IOException {
		if (success)
			writer.write("#Import successfully finished.");
		else
			writer.write("#Import aborted.");
	}
	
	public void write(ImportLogEntry entry) throws IOException {
		writer.write(entry.type + "," + entry.id + "," + entry.gmlId + System.lineSeparator());
	}

	public String getDefaultLogFileName() {
		return "imported_features-" +
				date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")) +
				".log";
	}

	public void close(boolean success) throws IOException {
		writeFooter(success);
		writer.close();
	}

	public static class ImportLogEntry {
		private final String type;
		private final long id;
		private final String gmlId;

		public ImportLogEntry(String type, long id, String gmlId) {
			this.type = type;
			this.id = id;
			this.gmlId = gmlId != null && !gmlId.isEmpty() ? gmlId : "";
		}
	}
}
