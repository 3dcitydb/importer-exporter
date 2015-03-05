/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;

public class XlinkTextureImage implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection externalFileConn;
	private final Config config;
	private final DBXlinkResolverManager resolverManager;

	private BlobImportAdapter textureImportAdapter;	
	private String localPath;
	private CounterEvent counter;
	private boolean replacePathSeparator;

	public XlinkTextureImage(Connection externalFileConn, Config config, DBXlinkResolverManager resolverManager) throws SQLException {
		this.externalFileConn = externalFileConn;
		this.config = config;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();
		counter = new CounterEvent(CounterType.TEXTURE_IMAGE, 1, this);
		replacePathSeparator = File.separatorChar == '/';

		textureImportAdapter = resolverManager.getDatabaseAdapter().getSQLAdapter().getBlobImportAdapter(externalFileConn, BlobType.TEXTURE_IMAGE);
	}

	public boolean insert(DBXlinkTextureFile xlink) throws SQLException {
		String imageFileURL = xlink.getFileURI();
		InputStream imageStream = null;
		boolean success = true;

		try {
			try {
				URL tmp = new URL(imageFileURL);
				imageFileURL = tmp.toString();
				imageStream = tmp.openStream();
			} catch (MalformedURLException malURL) {
				if (replacePathSeparator)
					imageFileURL = imageFileURL.replace("\\", "/");

				File imageFile = new File(imageFileURL);
				if (!imageFile.isAbsolute()) {
					imageFileURL = localPath + File.separator + imageFile.getPath();
					imageFile = new File(imageFileURL);
				}

				// check minimum requirements for local texture files
				if (!imageFile.exists() || !imageFile.isFile() || !imageFile.canRead()) {
					LOG.error("Failed to read texture file '" + imageFileURL + "'.");
					return false;
				} else if (imageFile.length() == 0) {
					LOG.error("Skipping 0 byte texture file '" + imageFileURL + "'.");
					return false;
				}

				imageStream = new FileInputStream(imageFileURL);
			}

			if (imageStream != null) {
				LOG.debug("Importing texture file: " + imageFileURL);
				success = textureImportAdapter.insert(xlink.getId(), imageStream, imageFileURL);
				resolverManager.propagateEvent(counter);
			}

			if (success) {
				//
			}

			return success;
		} catch (FileNotFoundException e) {
			LOG.error("Failed to find texture file '" + imageFileURL + "'.");
			return false;
		} catch (IOException e) {
			LOG.error("Failed to read texture file '" + imageFileURL + "': " + e.getMessage());
			return false;
		} finally {
			if (imageStream != null) {
				try {
					imageStream.close();
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
		textureImportAdapter.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXTURE_IMAGE;
	}

}
