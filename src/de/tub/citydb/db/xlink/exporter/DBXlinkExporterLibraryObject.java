package de.tub.citydb.db.xlink.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.Util;

public class DBXlinkExporterLibraryObject implements DBXlinkExporter {
	private final DBXlinkExporterManager xlinkExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psLibraryObject;
	private OracleResultSet rs;

	private String localPath;

	public DBXlinkExporterLibraryObject(Connection connection, Config config, DBXlinkExporterManager xlinkExporterManager) throws SQLException {
		this.xlinkExporterManager = xlinkExporterManager;
		this.config = config;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getExportPath();

		psLibraryObject = connection.prepareStatement("select LIBRARY_OBJECT from IMPLICIT_GEOMETRY where ID=?");
	}

	public boolean export(DBXlinkExternalFile xlink) throws SQLException {
		String fileName = xlink.getFileURI();
		boolean isReomte = false;

		if (fileName == null || fileName.length() == 0) {
			LogMessageEvent log = new LogMessageEvent(
					"Datenbank-Fehler beim Export eines Bibliothekobjekts: Attribut libraryObject ist leer.",
					LogMessageEnum.ERROR);
			xlinkExporterManager.propagateEvent(log);

			return false;
		}

		// check whether we deal with a remote image uri
		if (Util.isRemoteXlink(fileName)) {
			URL url = null;
			isReomte = true;

			try {
				url = new URL(fileName);
			} catch (MalformedURLException e) {
				LogMessageEvent log = new LogMessageEvent(
						"Fehler beim Export eines Bibliothekobjekts: " + fileName + " konnte nicht interpretiert werden.",
						LogMessageEnum.ERROR);
				xlinkExporterManager.propagateEvent(log);

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
		String fileURI = localPath + File.separator + fileName;
		File file = new File(fileURI);
		if (file.exists()) {
			// we could have an action depending on some user input
			// so far, we silently return
			return false;
		}

		// try and read texture image attribute from surface_data table
		psLibraryObject.setLong(1, xlink.getId());
		rs = (OracleResultSet)psLibraryObject.executeQuery();

		if (!rs.next()) {
			if (!isReomte) {
				// we could not read from database. if we deal with a remote
				// image uri, we do not really care. but if the texture image should
				// be provided by us, then this is serious...
				LogMessageEvent log = new LogMessageEvent(
						"Fehler beim Export eines Bibliothekobjekts: " + fileName + " existiert nicht in der Datenbank.",
						LogMessageEnum.ERROR);
				xlinkExporterManager.propagateEvent(log);
			}

			return false;
		}

		LogMessageEvent log = new LogMessageEvent(
				"Exportiere Bibliothekobjekt: " + fileName,
				LogMessageEnum.INFO);
		xlinkExporterManager.propagateEvent(log);

		// read oracle image data type
		BLOB blob = rs.getBLOB(1);
		if (blob == null) {
			log = new LogMessageEvent(
					"Datenbank-Fehler beim Lesen des Bibliothekobjekts: " + fileName,
					LogMessageEnum.ERROR);
			xlinkExporterManager.propagateEvent(log);

			return false;
		}

		int size = blob.getBufferSize();
		byte[] buffer = new byte[size];

		try {
			InputStream in = blob.getBinaryStream(1L);
			FileOutputStream out = new FileOutputStream(fileURI);

			int length = -1;
			while ((length = in.read(buffer)) != -1)
				out.write(buffer, 0, length);

			in.close();
			out.close();
		} catch (IOException ioEx) {
			log = new LogMessageEvent(
					"Schreibfehler bei " + fileName + ": " + ioEx,
					LogMessageEnum.ERROR);
			xlinkExporterManager.propagateEvent(log);

			return false;
		}

		return true;
	}

	@Override
	public DBXlinkExporterEnum getDBXlinkExporterType() {
		return DBXlinkExporterEnum.LIBRARY_OBJECT;
	}

}
