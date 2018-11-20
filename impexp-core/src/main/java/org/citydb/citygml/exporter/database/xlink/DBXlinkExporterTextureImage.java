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

import org.citydb.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.config.Config;
import org.citydb.config.internal.OutputFile;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class DBXlinkExporterTextureImage implements DBXlinkExporter {
	private final Logger log = Logger.getInstance();
	private final DBXlinkExporterManager xlinkExporterManager;

	private BlobExportAdapter textureImageExportAdapter;
	private OutputFile outputFile;
	private String texturePath;
	private boolean isRelativePath;
	private String separator;
	private boolean overwriteTextureImage;
	private boolean useBuckets;
	private boolean[] buckets; 
	private CounterEvent counter;

	public DBXlinkExporterTextureImage(Connection connection, Config config, DBXlinkExporterManager xlinkExporterManager) throws SQLException {
		this.xlinkExporterManager = xlinkExporterManager;

		outputFile = config.getInternal().getCurrentExportFile();
		isRelativePath = config.getProject().getExporter().getAppearances().getTexturePath().isRelative();
		texturePath = config.getInternal().getExportTexturePath();
		separator = config.getProject().getExporter().getAppearances().getTexturePath().isAbsolute() ? File.separator : "/";
		overwriteTextureImage = config.getProject().getExporter().getAppearances().isSetOverwriteTextureFiles();
		counter = new CounterEvent(CounterType.TEXTURE_IMAGE, 1, this);
		useBuckets = config.getProject().getExporter().getAppearances().getTexturePath().isUseBuckets() &&
				config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets() > 0;

		if (useBuckets)
			buckets = new boolean[config.getProject().getExporter().getAppearances().getTexturePath().getNoOfBuckets()];

		textureImageExportAdapter = xlinkExporterManager.getDatabaseAdapter().getSQLAdapter().getBlobExportAdapter(connection, BlobType.TEXTURE_IMAGE);
	}

	public boolean export(DBXlinkTextureFile xlink) throws SQLException {
		String fileURI = xlink.getFileURI();

		if (fileURI == null || fileURI.isEmpty()) {
			log.error("Database error while exporting a texture file: Attribute TEX_IMAGE_URI is empty.");
			return false;
		}

		Path file;
		try {
			file = isRelativePath ?
					outputFile.resolve(texturePath).resolve(fileURI) :
					Paths.get(texturePath, fileURI);
		} catch (InvalidPathException e) {
			log.error("Failed to export a texture file: '" + fileURI + "' is invalid.");
			return false;
		}

		if (!overwriteTextureImage && Files.exists(file))
			return false;

		if (useBuckets) {
			try {
				int bucket = Integer.valueOf(fileURI.substring(0, fileURI.indexOf(separator))) - 1;
				if (!buckets[bucket]) {
					Files.createDirectories(file.getParent());
					buckets[bucket] = true;
				}
			} catch (IOException | NumberFormatException e) {
				log.error("Failed to create texture bucket for '" + fileURI + "'.");
				return false;
			}
		}

		// load image data into file
		try {
			xlinkExporterManager.propagateEvent(counter);
			return textureImageExportAdapter.writeToStream(xlink.getId(), fileURI, Files.newOutputStream(file));
		} catch (IOException e) {
			log.error("Failed to export texture file " + fileURI + ": " + e.getMessage());
			return false;
		}
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
