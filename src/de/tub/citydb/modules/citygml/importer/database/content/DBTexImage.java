package de.tub.citydb.modules.citygml.importer.database.content;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;

import de.tub.citydb.config.Config;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;

public class DBTexImage implements DBImporter {
	private final static ReentrantLock mainLock = new ReentrantLock();

	private final Connection connection;
	private final Config config;
	private final DBImporterManager importerManager;

	private PreparedStatement psInsertStmt;
	private MessageDigest md5;
	private String localPath;
	private boolean replacePathSeparator;
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
		long texImageId = 0;
		String imageURI = abstractTexture.getImageURI().trim();
		String md5URI = toHexString(md5.digest(imageURI.getBytes()));
		boolean insertTexImage = false;

		// check whether the texture image is referenced from another surface data
		// this check has to be synchronized though
		final ReentrantLock lock = mainLock;
		lock.lock();

		try {
			texImageId = importerManager.getDBId(md5URI, CityGMLClass.ABSTRACT_TEXTURE);
			if (texImageId == 0) {
				texImageId = importerManager.getDBId(DBSequencerEnum.TEX_IMAGE_ID_SEQ);
				importerManager.putUID(md5URI, texImageId, CityGMLClass.ABSTRACT_TEXTURE);
				insertTexImage = true;
			}
		} finally {
			lock.unlock();
		}

		if (insertTexImage) {
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
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TEX_IMAGE;
	}

}
