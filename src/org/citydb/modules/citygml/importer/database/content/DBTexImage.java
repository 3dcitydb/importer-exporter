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
package org.citydb.modules.citygml.importer.database.content;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.config.Config;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.citygml.importer.util.ConcurrentLockManager;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;

public class DBTexImage implements DBImporter {
	private final ConcurrentLockManager lockManager = ConcurrentLockManager.getInstance(DBTexImage.class);
	private final Connection connection;
	private final Config config;
	private final DBImporterManager importerManager;

	private PreparedStatement psInsertStmt;
	private MessageDigest md5;
	private String localPath;
	private boolean replacePathSeparator;
	private boolean importTextureImage;
	private int batchCounter;

	public DBTexImage(Connection connection, Config config, DBImporterManager importerManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.importerManager = importerManager;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();
		replacePathSeparator = File.separatorChar == '/';
		importTextureImage = config.getProject().getImporter().getAppearances().isSetImportTextureFiles();

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new SQLException(e);
		}

		psInsertStmt = connection.prepareStatement(new StringBuilder()
		.append("insert into TEX_IMAGE (ID, TEX_IMAGE_URI, TEX_MIME_TYPE, TEX_MIME_TYPE_CODESPACE) values ")
		.append("(?, ?, ?, ?)").toString());
	}

	public long insert(AbstractTexture abstractTexture, long surfaceDataId) throws SQLException {
		String imageURI = abstractTexture.getImageURI().trim();
		if (imageURI.isEmpty())
			return 0;
		
		long texImageId = 0;
		String md5URI = toHexString(md5.digest(imageURI.getBytes()));
		boolean insertIntoTexImage = false;

		// synchronize concurrent processing of the same texture image
		// different texture images however may be processed concurrently
		ReentrantLock lock = lockManager.putAndGetLock(md5URI);
		lock.lock();

		try {
			texImageId = importerManager.getDBId(md5URI, CityGMLClass.ABSTRACT_TEXTURE);
			if (texImageId == 0) {
				texImageId = importerManager.getDBId(DBSequencerEnum.TEX_IMAGE_ID_SEQ);
				importerManager.putUID(md5URI, texImageId, CityGMLClass.ABSTRACT_TEXTURE);
				insertIntoTexImage = true;
			}

		} finally {
			lockManager.releaseLock(md5URI);
			lock.unlock();
		}

		if (insertIntoTexImage) {
			// fill TEX_IMAGE with texture file properties
			String fileName = getFileName(imageURI);
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
			if (++batchCounter == importerManager.getDatabaseAdapter().getMaxBatchSize())
				importerManager.executeBatch(DBImporterEnum.TEX_IMAGE);

			if (importTextureImage) {
				// propagte xlink to import the texture file itself
				importerManager.propagateXlink(new DBXlinkTextureFile(
						texImageId,
						imageURI,
						false));

				// do we have a world file?!
				if (abstractTexture.getCityGMLClass() == CityGMLClass.GEOREFERENCED_TEXTURE &&
						!((GeoreferencedTexture)abstractTexture).isSetOrientation() && !((GeoreferencedTexture)abstractTexture).isSetReferencePoint()) {
					importerManager.propagateXlink(new DBXlinkTextureFile(
							surfaceDataId,
							imageURI,
							true));
				}
			}
		}

		return texImageId;
	}

	private String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
			hexString.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));

		return hexString.toString();
	}

	private String getFileName(String fileURI) {
		String name = fileURI;

		if (replacePathSeparator)
			fileURI = fileURI.replace("\\", "/");

		File imageFile = new File(fileURI);
		if (!imageFile.isAbsolute()) {
			fileURI = localPath + File.separator + imageFile.getPath();
			imageFile = new File(fileURI);
		}

		if (imageFile.exists())
			name = imageFile.getName();

		return name;
	}

	@Override
	public void executeBatch() throws SQLException {
		psInsertStmt.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psInsertStmt.close();
		lockManager.clear();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TEX_IMAGE;
	}

}
