/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.citygml.exporter.database.xlink;

import org.citydb.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.config.Config;
import org.citydb.config.internal.OutputFile;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class DBXlinkExporterLibraryObject implements DBXlinkExporter {
	private final Logger log = Logger.getInstance();

	private BlobExportAdapter blobExportAdapter;
	private OutputFile outputFile;

	public DBXlinkExporterLibraryObject(Connection connection, Config config, DBXlinkExporterManager xlinkExporterManager) throws SQLException {
		outputFile = config.getInternal().getCurrentExportFile();
		blobExportAdapter = xlinkExporterManager.getDatabaseAdapter().getSQLAdapter().getBlobExportAdapter(connection, BlobType.LIBRARY_OBJECT);
	}

	public boolean export(DBXlinkLibraryObject xlink) throws SQLException {
		String fileURI = xlink.getFileURI();

		if (fileURI == null || fileURI.isEmpty()) {
			log.error("Database error while exporting a library object: Attribute REFERENCE_TO_LIBRARY is empty.");
			return false;
		}

		Path file;
		try {
			file = outputFile.resolve(fileURI);
		} catch (InvalidPathException e) {
			log.error("Failed to export a library object: '" + fileURI + "' is invalid.");
			return false;
		}

		if (Files.exists(file)) {
			// we could have an action depending on some user input
			// so far, we silently return
			return false;
		}

		// read blob into file
		try {
			return blobExportAdapter.writeToStream(xlink.getId(), fileURI, Files.newOutputStream(file));
		} catch (IOException e) {
			log.error("Failed to export library object " + fileURI + ": " + e.getMessage());
			return false;
		}
	}

	@Override
	public void close() throws SQLException {
		blobExportAdapter.close();
	}

	@Override
	public DBXlinkExporterEnum getDBXlinkExporterType() {
		return DBXlinkExporterEnum.LIBRARY_OBJECT;
	}

}
