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
package org.citydb.core.operation.importer.database.content;

import org.citydb.config.Config;
import org.citydb.core.database.schema.SequenceEnum;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.common.xlink.DBXlinkTextureFile;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.util.ConcurrentLockManager;
import org.citydb.core.operation.importer.util.ExternalFileChecker;
import org.citydb.util.log.Logger;
import org.citygml4j.model.citygml.appearance.AbstractTexture;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBTexImage implements DBImporter {
	private final ConcurrentLockManager lockManager = ConcurrentLockManager.getInstance(DBTexImage.class);
	private final Logger log = Logger.getInstance();
	private final CityGMLImportManager importer;
	private PreparedStatement psInsertStmt;	

	private ExternalFileChecker externalFileChecker;
	private MessageDigest md5;
	private boolean importTextureImage;
	private int batchCounter;

	public DBTexImage(Connection connection, Config config, CityGMLImportManager importer) throws SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		importTextureImage = config.getImportConfig().getAppearances().isSetImportTextureFiles();
		externalFileChecker = importer.getExternalFileChecker();

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new SQLException(e);
		}

		String stmt = "insert into " + schema + ".tex_image (id, tex_image_uri, tex_mime_type, tex_mime_type_codespace) values " +
				"(?, ?, ?, ?)";
		psInsertStmt = connection.prepareStatement(stmt);
	}

	public long doImport(AbstractTexture abstractTexture, long surfaceDataId) throws CityGMLImportException, SQLException {
		String imageURI = abstractTexture.getImageURI().trim();
		if (imageURI.isEmpty())
			return 0;

		long texImageId = 0;
		String md5URI = toHexString(md5.digest(imageURI.getBytes()));

		Map.Entry<String, String> fileInfo = null;
		boolean insertIntoTexImage = false;

		// synchronize concurrent processing of the same texture image
		// different texture images however may be processed concurrently
		ReentrantLock lock = lockManager.getLock(md5URI);
		lock.lock();
		try {
			texImageId = importer.getTextureImageId(md5URI);
			if (texImageId == -1) {
				try {
					fileInfo = externalFileChecker.getFileInfo(imageURI);
					texImageId = importer.getNextSequenceValue(SequenceEnum.TEX_IMAGE_ID_SEQ.getName());
					insertIntoTexImage = true;
				} catch (IOException e) {
					importer.logOrThrowErrorMessage("Failed to read image file at '" + imageURI + "'.", e);
					texImageId = 0;
				}

				importer.putTextureImageId(md5URI, texImageId);
			}
		} finally {
			lockManager.releaseLock(md5URI);
			lock.unlock();
		}

		if (insertIntoTexImage) {
			// fill TEX_IMAGE with texture file properties
			String fileName = fileInfo.getValue();
			String mimeType = null;
			String codeSpace = null;

			if (abstractTexture.isSetMimeType()) {
				mimeType = abstractTexture.getMimeType().getValue();
				codeSpace = abstractTexture.getMimeType().getCodeSpace();
			}

			psInsertStmt.setLong(1, texImageId);
			psInsertStmt.setString(2, fileName);
			psInsertStmt.setString(3, mimeType);
			psInsertStmt.setString(4, codeSpace);

			psInsertStmt.addBatch();
			if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
				importer.executeBatch(TableEnum.TEX_IMAGE);

			if (importTextureImage) {
				// propagte xlink to import the texture file itself
				importer.propagateXlink(new DBXlinkTextureFile(
						texImageId,
						fileInfo.getKey()));
			}
		}

		return texImageId;
	}

	private String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes)
			hexString.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

		return hexString.toString();
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psInsertStmt.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psInsertStmt.close();
	}

}
