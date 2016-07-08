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
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.util.Util;

public class DBXlinkExporterTextureImage implements DBXlinkExporter {
	private final Logger LOG = Logger.getInstance();
	private final DBXlinkExporterManager xlinkExporterManager;
	private final Config config;
	private final Connection connection;

	private BlobExportAdapter textureImageExportAdapter;
	private String localPath;
	private String texturePath;
	private boolean texturePathIsLocal;
	private boolean overwriteTextureImage;
	private boolean useBuckets;
	private boolean[] buckets; 
	private CounterEvent counter;

	public DBXlinkExporterTextureImage(Connection connection, Config config, DBXlinkExporterManager xlinkExporterManager) throws SQLException {
		this.xlinkExporterManager = xlinkExporterManager;
		this.config = config;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getExportPath();
		texturePathIsLocal = config.getProject().getExporter().getAppearances().getTexturePath().isRelative();
		texturePath = config.getInternal().getExportTextureFilePath();
		overwriteTextureImage = config.getProject().getExporter().getAppearances().isSetOverwriteTextureFiles();
		counter = new CounterEvent(CounterType.TEXTURE_IMAGE, 1, this);
		useBuckets = config.getProject().getExporter().getAppearances().getTexturePath().isUseBuckets() &&
				config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets() > 0;

		if (useBuckets)
			buckets = new boolean[config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets()];

		textureImageExportAdapter = xlinkExporterManager.getDatabaseAdapter().getSQLAdapter().getBlobExportAdapter(connection, BlobType.TEXTURE_IMAGE);
	}

	public boolean export(DBXlinkTextureFile xlink) throws SQLException {
		String fileName = xlink.getFileURI();

		if (fileName == null || fileName.length() == 0) {
			LOG.error("Database error while exporting a texture file: Attribute TEX_IMAGE_URI is empty.");
			return false;
		}

		// check whether we deal with a remote image uri
		if (Util.isRemoteXlink(fileName)) {
			URL url = null;

			try {
				url = new URL(fileName);
			} catch (MalformedURLException e) {
				LOG.error("Error while exporting a texture file: " + fileName + " could not be interpreted.");
				return false;
			}

			if (url != null) {
				File file = new File(url.getFile());
				fileName = file.getName();
			}
		}

		// start export of texture to file
		// we do not overwrite an already existing file. so no need to
		// query the database in that case.
		String fileURI;
		if (texturePathIsLocal)
			fileURI = localPath + File.separator + texturePath + File.separator + fileName;
		else
			fileURI = texturePath + File.separator + fileName;

		File file = new File(fileURI);
		if (!overwriteTextureImage && file.exists())
			return false;

		if (useBuckets) {
			int bucket = Integer.valueOf(fileName.substring(0, fileName.indexOf('/'))) - 1;
			if (!buckets[bucket]) {
				file.getParentFile().mkdir();
				buckets[bucket] = true;
			}
		}

		// load image data into file
		xlinkExporterManager.propagateEvent(counter);
		return textureImageExportAdapter.getInFile(xlink.getId(), fileName, fileURI);
	}

	@Override
	public void close() throws SQLException {
		textureImageExportAdapter.close();
	}

	@Override
	public DBXlinkExporterEnum getDBXlinkExporterType() {
		return DBXlinkExporterEnum.TEXTURE_IMAGE;
	}

}
