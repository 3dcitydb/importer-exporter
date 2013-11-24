/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.database.xlink;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.config.Config;
import de.tub.citydb.database.adapter.BlobExportAdapter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import de.tub.citydb.util.Util;

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

		blobExportAdapter = xlinkExporterManager.getDatabaseAdapter().getSQLAdapter().getBlobExportAdapter(connection);
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
