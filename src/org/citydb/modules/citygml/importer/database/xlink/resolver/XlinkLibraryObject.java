/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.config.Config;
import org.citydb.database.adapter.BlobImportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;

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

		blobImportAdapter = resolverManager.getDatabaseAdapter().getSQLAdapter().getBlobImportAdapter(externalFileConn, BlobType.LIBRARY_OBJECT);
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
