package de.tub.citydb.db.xlink.exporter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.event.statistic.TextureImageCountEvent;
import de.tub.citydb.util.Util;

public class DBXlinkExporterTextureImage implements DBXlinkExporter {
	private final DBXlinkExporterManager xlinkExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psTextureImage;
	private OracleResultSet rs;

	private String localPath;
	private String texturePath;
	private boolean texturePathIsLocal;
	private boolean overwriteTextureImage;
	private TextureImageCountEvent counter;

	public DBXlinkExporterTextureImage(Connection connection, Config config, DBXlinkExporterManager xlinkExporterManager) throws SQLException {
		this.xlinkExporterManager = xlinkExporterManager;
		this.config = config;
		this.connection = connection;

		init();
	}

	private void init() throws SQLException {
		localPath = config.getInternal().getExportPath();
		texturePathIsLocal = config.getProject().getExporter().getAppearances().isTexturePathRealtive();
		texturePath = config.getInternal().getExportTextureFilePath();
		overwriteTextureImage = config.getProject().getExporter().getAppearances().isSetOverwriteTextureFiles();
		counter = new TextureImageCountEvent(1);
		
		psTextureImage = connection.prepareStatement("select TEX_IMAGE from SURFACE_DATA where ID=?");
	}

	public boolean export(DBXlinkExternalFile xlink) throws SQLException {
		String fileName = xlink.getFileURI();
		boolean isRemote = false;

		if (fileName == null || fileName.length() == 0) {
			LogMessageEvent log = new LogMessageEvent(
					"Datenbank-Fehler beim Export einer Tetxurdatei: Attribut imageURI ist leer.",
					LogMessageEnum.ERROR);
			xlinkExporterManager.propagateEvent(log);

			return false;
		}

		// check whether we deal with a remote image uri
		if (Util.isRemoteXlink(fileName)) {
			URL url = null;
			isRemote = true;

			try {
				url = new URL(fileName);
			} catch (MalformedURLException e) {
				LogMessageEvent log = new LogMessageEvent(
						"Fehler beim Export einer Tetxurdatei: " + fileName + " konnte nicht interpretiert werden.",
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
		String fileURI;
		if (texturePathIsLocal)
			fileURI = localPath + File.separator + texturePath + File.separator + fileName;
		else
			fileURI = texturePath + File.separator + fileName;

		File file = new File(fileURI);
		if (file.exists() && !overwriteTextureImage)
			return false;

		// try and read texture image attribute from surface_data table
		psTextureImage.setLong(1, xlink.getId());
		rs = (OracleResultSet)psTextureImage.executeQuery();

		if (!rs.next()) {
			if (!isRemote) {
				// we could not read from database. if we deal with a remote
				// image uri, we do not really care. but if the texture image should
				// be provided by us, then this is serious...
				LogMessageEvent log = new LogMessageEvent(
						"Fehler beim Export einer Tetxurdatei: " + fileName + " existiert nicht in der Datenbank.",
						LogMessageEnum.ERROR);
				xlinkExporterManager.propagateEvent(log);
			}

			return false;
		}

		LogMessageEvent log = new LogMessageEvent(
				"Exportiere Tetxurdatei: " + fileName,
				LogMessageEnum.DEBUG);
		xlinkExporterManager.propagateEvent(log);

		xlinkExporterManager.propagateEvent(counter);
		
		// read oracle image data type
		OrdImage imgProxy = (OrdImage)rs.getORAData(1, OrdImage.getORADataFactory());
		if (imgProxy == null) {
			log = new LogMessageEvent(
					"Datenbank-Fehler beim Lesen der Tetxurdatei: " + fileName,
					LogMessageEnum.ERROR);
			xlinkExporterManager.propagateEvent(log);

			return false;
		}

		try {
			imgProxy.getDataInFile(fileURI);
		} catch (IOException ioEx) {
			log = new LogMessageEvent(
					"Schreibfehler bei " + fileName + ": " + ioEx,
					LogMessageEnum.ERROR);
			xlinkExporterManager.propagateEvent(log);

			return false;
		}

		imgProxy = null;
		return true;
	}

	@Override
	public DBXlinkExporterEnum getDBXlinkExporterType() {
		return DBXlinkExporterEnum.TEXTURE_IMAGE;
	}

}
