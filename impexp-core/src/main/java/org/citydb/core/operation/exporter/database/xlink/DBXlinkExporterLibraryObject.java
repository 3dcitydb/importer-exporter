/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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
package org.citydb.core.operation.exporter.database.xlink;

import org.citydb.core.database.adapter.BlobExportAdapter;
import org.citydb.core.database.adapter.BlobType;
import org.citydb.core.file.FileType;
import org.citydb.core.file.OutputFile;
import org.citydb.core.operation.common.xlink.DBXlinkLibraryObject;
import org.citydb.core.util.CoreConstants;
import org.citydb.util.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class DBXlinkExporterLibraryObject implements DBXlinkExporter {
	private final Logger log = Logger.getInstance();
	private final OutputFile outputFile;
	private final BlobExportAdapter blobExporter;

	private boolean isFolderCreated;

	public DBXlinkExporterLibraryObject(Connection connection, DBXlinkExporterManager exporterManager) {
		outputFile = exporterManager.getInternalConfig().getOutputFile();
		blobExporter = exporterManager.getDatabaseAdapter().getSQLAdapter()
				.getBlobExportAdapter(connection, BlobType.LIBRARY_OBJECT)
				.withBatchSize(exporterManager.getBlobBatchSize());
	}

	public boolean export(DBXlinkLibraryObject xlink) throws SQLException {
		String fileURI = xlink.getFileURI();

		if (fileURI == null || fileURI.isEmpty()) {
			log.error("Database error while exporting a library object: Attribute REFERENCE_TO_LIBRARY is empty.");
			return false;
		}

		Path file ;
		try {
			if (outputFile.getType() != FileType.ARCHIVE)
				file = Paths.get(outputFile.resolve(CoreConstants.LIBRARY_OBJECTS_DIR, fileURI));
			else
				file = null;
		} catch (InvalidPathException e) {
			log.error("Failed to export a library object: '" + fileURI + "' is invalid.");
			return false;
		}

		if (!isFolderCreated) {
			try {
				isFolderCreated = true;
				if (file != null)
					Files.createDirectories(file.getParent());
				else
					outputFile.createDirectories(CoreConstants.LIBRARY_OBJECTS_DIR);
			} catch (IOException e) {
				throw new SQLException("Failed to create folder for library objects.");
			}
		}

		try {
			blobExporter.addBatch(xlink.getId(), new BlobExportAdapter.BatchEntry(
					() -> file != null ?
							Files.newOutputStream(file) :
							outputFile.newOutputStream(outputFile.resolve(CoreConstants.LIBRARY_OBJECTS_DIR, fileURI)),
					() -> file == null || !Files.exists(file)));

			return true;
		} catch (IOException e) {
			log.error("Failed to batch export library objects.", e);
			return false;
		}
	}

	@Override
	public void close() throws SQLException {
		try {
			blobExporter.executeBatch();
		} catch (IOException e) {
			log.error("Failed to batch export library objects.", e);
		}

		blobExporter.close();
	}

	@Override
	public DBXlinkExporterEnum getDBXlinkExporterType() {
		return DBXlinkExporterEnum.LIBRARY_OBJECT;
	}

}
