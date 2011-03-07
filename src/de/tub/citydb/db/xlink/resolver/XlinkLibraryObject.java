package de.tub.citydb.db.xlink.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;

public class XlinkLibraryObject implements DBXlinkResolver {
	private final Connection externalFileConn;
	private final Config config;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psPrepare;
	PreparedStatement psSelect;
	OraclePreparedStatement psInsert;
	private String localPath;

	public XlinkLibraryObject(Connection textureImageConn, Config config, DBXlinkResolverManager resolverManager) throws SQLException {
		this.externalFileConn = textureImageConn;
		this.config = config;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getImportPath();

		psPrepare = externalFileConn.prepareStatement("update IMPLICIT_GEOMETRY set LIBRARY_OBJECT=empty_blob() where ID=?");
		psSelect = externalFileConn.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=? for update");
	}

	public boolean insert(DBXlinkExternalFile xlink) throws SQLException {
		String imageFileName = xlink.getFileURI();
		boolean isRemote = true;
		URL imageURL = null;
		try {
			// first step: prepare BLOB
			psPrepare.setLong(1, xlink.getId());
			psPrepare.executeUpdate();

			// second step: get prepared BLOB to fill it with contents
			psSelect.setLong(1, xlink.getId());
			OracleResultSet rs = (OracleResultSet)psSelect.executeQuery();
			if (!rs.next()) {
				LogMessageEvent log = new LogMessageEvent(
						"Datenbank-Fehler beim Import eines Bibliothekobjektes aufgetreten.",
						LogMessageEnum.ERROR);
				resolverManager.propagateEvent(log);

				externalFileConn.rollback();
				return false;
			}

			BLOB blob = rs.getBLOB(1);

			// third step: try and upload image data
			try {
				imageURL = new URL(imageFileName);
				imageFileName = imageURL.toString();

			} catch (MalformedURLException malURL) {
				isRemote = false;
				File imageFile = new File(imageFileName);
				imageFileName = localPath + File.separator + imageFile.getPath();
			}

			LogMessageEvent log = new LogMessageEvent(
					"Importiere Bibliothekobjekt: " + imageFileName,
					LogMessageEnum.DEBUG);
			resolverManager.propagateEvent(log);

			InputStream in = null;

			if (isRemote) {
				in = imageURL.openStream();
			} else {
				in = new FileInputStream(imageFileName);
			}

			if (in == null) {
				log = new LogMessageEvent(
						"Datenbank-Fehler beim Import des Bibliothekobjektes " + imageFileName + " aufgetreten.",
						LogMessageEnum.ERROR);
				resolverManager.propagateEvent(log);

				externalFileConn.rollback();
				return false;
			}

			OutputStream out = blob.setBinaryStream(1L);

			int size = blob.getBufferSize();
			byte[] buffer = new byte[size];
			int length = -1;

			while ((length = in.read(buffer)) != -1)
				out.write(buffer, 0, length);

			in.close();
			out.close();
			externalFileConn.commit();

		} catch (IOException ioEx) {
			LogMessageEvent log = new LogMessageEvent(
					"Lesefehler bei " + imageFileName + ": " + ioEx,
					LogMessageEnum.ERROR);
			resolverManager.propagateEvent(log);

			externalFileConn.rollback();
			return false;
		} catch (SQLException sqlEx) {
			LogMessageEvent log = new LogMessageEvent(
					"SQL-Fehler: " + sqlEx,
					LogMessageEnum.ERROR);
			resolverManager.propagateEvent(log);

			externalFileConn.rollback();
			return false;
		}

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		// we do not have any action here, since we are heavily committing and roll-backing
		// within the insert-method. that's also the reason why we need a separated connection instance.
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.LIBRARY_OBJECT;
	}

}
