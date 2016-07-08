/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.exporter.database.xlink;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.config.Config;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.util.Util;

public class DBXlinkExporterLibraryObject implements DBXlinkExporter {
	private final Logger LOG = Logger.getInstance();

	private final DBXlinkExporterManager xlinkExporterManager;
	private final Config config;
	private final Connection connection;

	private BlobExportAdapter blobExportAdapter;
	private String localPath;

	public DBXlinkExporterLibraryObject(Connection connection, Config config, DBXlinkExporterManager xlinkExporterManager) throws SQLException {
		this.config = config;
		this.connection = connection;
		this.xlinkExporterManager = xlinkExporterManager;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getExportPath();

		blobExportAdapter = xlinkExporterManager.getDatabaseAdapter().getSQLAdapter().getBlobExportAdapter(connection, BlobType.LIBRARY_OBJECT);
	}

	public boolean export(DBXlinkLibraryObject xlink) throws SQLException {
		String fileName = xlink.getFileURI();

		if (fileName == null || fileName.length() == 0) {
			LOG.error("Database error while exporting a library object: Attribute REFERENCE_TO_LIBRARY is empty.");
			return false;
		}

		// check whether we deal with a remote object uri
		if (Util.isRemoteXlink(fileName)) {
			URL url = null;

			try {
				url = new URL(fileName);
			} catch (MalformedURLException e) {
				LOG.error("Error while exporting a library object: " + fileName + " could not be interpreted.");
				return false;
			}

			if (url != null) {
				File file = new File(url.getFile());
				fileName = file.getName();
			}
		}

		// start export of library object to file
		// we do not overwrite an already existing file. so no need to
		// query the database in that case.
		String fileURI = localPath + File.separator + fileName;
		File file = new File(fileURI);
		if (file.exists()) {
			// we could have an action depending on some user input
			// so far, we silently return
			return false;
		}

		// read blob into file
		return blobExportAdapter.getInFile(xlink.getId(), fileName, fileURI);
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
