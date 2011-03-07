package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.db.temp.DBTempGTT;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;

public class DBXlinkImporterExternalFile implements DBXlinkImporter {
	private final DBTempGTT tempTable;
	private PreparedStatement psXlink;

	public DBXlinkImporterExternalFile(DBTempGTT tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getWriter().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, FILE_URI, TYPE) values " +
			"(?, ?, ?)");
	}

	public boolean insert(DBXlinkExternalFile xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setString(2, xlinkEntry.getFileURI());
		psXlink.setInt(3, xlinkEntry.getType().ordinal());

		psXlink.addBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psXlink.executeBatch();
	}

	@Override
	public DBXlinkImporterEnum getDBXlinkImporterType() {
		return DBXlinkImporterEnum.EXTERNAL_FILE;
	}

}
