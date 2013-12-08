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
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.config.Config;
import de.tub.citydb.database.adapter.BlobImportAdapter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;

public class XlinkLibraryObject implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection externalFileConn;
	private final Config config;
	private final DBXlinkResolverManager resolverManager;

	private BlobImportAdapter blobImportAdapter;
	private String localPath;
	private boolean replacePathSeparator;

	public XlinkLibraryObject(Connection textureImageConn, Config config, DBXlinkResolverManager resolverManager) throws SQLException {
		this.externalFileConn = textureImageConn;
		this.config = config;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();
		replacePathSeparator = File.separatorChar == '/';

		blobImportAdapter = resolverManager.getDatabaseAdapter().getSQLAdapter().getBlobImportAdapter(externalFileConn);
	}

	public boolean insert(DBXlinkLibraryObject xlink) throws SQLException {
		String objectFileName = xlink.getFileURI();
		InputStream objectStream = null;

		try {
			try {
				URL objectURL = new URL(objectFileName);
				objectFileName = objectURL.toString();
				objectStream = objectURL.openStream();
			} catch (MalformedURLException malURL) {				
				if (replacePathSeparator)
					objectFileName = objectFileName.replace("\\", "/");

				File objectFile = new File(objectFileName);
				if (!objectFile.isAbsolute()) {
					objectFileName = localPath + File.separator + objectFile.getPath();
					objectFile = new File(objectFileName);
				}

				// check minimum requirements for local library object file
				if (!objectFile.exists() || !objectFile.isFile() || !objectFile.canRead()) {
					LOG.error("Failed to read library object file '" + objectFileName + "'.");
					return false;
				} else if (objectFile.length() == 0) {
					LOG.error("Skipping 0 byte library object file '" + objectFileName + "'.");
					return false;
				}

				objectStream = new FileInputStream(objectFileName);
			}

			boolean success = false;
			if (objectStream != null) {
				LOG.debug("Importing library object: " + objectFileName);
				success = blobImportAdapter.insert(xlink.getId(), objectStream, objectFileName);
			}
			
			return success;
		} catch (FileNotFoundException e) {
			LOG.error("Failed to find library object file '" + objectFileName + "'.");
			return false;
		} catch (IOException e) {
			LOG.error("Failed to read library object file '" + objectFileName + "': " + e.getMessage());
			return false;
		} finally {
			if (objectStream != null) {
				try {
					objectStream.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		// we do not have any action here
	}

	@Override
	public void close() throws SQLException {
		blobImportAdapter.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.LIBRARY_OBJECT;
	}

}
