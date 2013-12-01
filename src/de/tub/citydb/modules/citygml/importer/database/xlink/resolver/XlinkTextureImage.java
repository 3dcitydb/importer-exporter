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
import de.tub.citydb.database.adapter.TextureImageImportAdapter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;

public class XlinkTextureImage implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection externalFileConn;
	private final Config config;
	private final DBXlinkResolverManager resolverManager;

	private TextureImageImportAdapter textureImportAdapter;	
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

		textureImportAdapter = resolverManager.getDatabaseAdapter().getSQLAdapter().getTextureImageImportAdapter(externalFileConn);
	}

	public boolean insert(DBXlinkTextureFile xlink) throws SQLException {
		String imageFileName = xlink.getFileURI();
		InputStream imageStream = null;

		try {
			try {
				URL imageURL = new URL(imageFileName);
				imageFileName = imageURL.toString();
				imageStream = imageURL.openStream();
			} catch (MalformedURLException malURL) {
				if (replacePathSeparator)
					imageFileName = imageFileName.replace("\\", "/");

				File imageFile = new File(imageFileName);
				if (!imageFile.isAbsolute()) {
					imageFileName = localPath + File.separator + imageFile.getPath();
					imageFile = new File(imageFileName);
				}

				// check minimum requirements for local texture files
				if (!imageFile.exists() || !imageFile.isFile() || !imageFile.canRead()) {
					LOG.error("Failed to read texture file '" + imageFileName + "'.");
					return false;
				} else if (imageFile.length() == 0) {
					LOG.error("Skipping 0 byte texture file '" + imageFileName + "'.");
					return false;
				}

				imageStream = new FileInputStream(imageFileName);
			}

			boolean success = false;
			if (imageStream != null) {
				LOG.debug("Importing texture file: " + imageFileName);
				success = textureImportAdapter.insert(xlink.getId(), imageStream, imageFileName);
				resolverManager.propagateEvent(counter);
			}

			return success;
		} catch (FileNotFoundException e) {
			LOG.error("Failed to find texture file '" + imageFileName + "'.");
			return false;
		} catch (IOException e) {
			LOG.error("Failed to read texture file '" + imageFileName + "': " + e.getMessage());
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
