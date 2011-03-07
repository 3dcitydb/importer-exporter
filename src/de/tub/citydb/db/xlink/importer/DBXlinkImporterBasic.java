package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.xlink.DBXlinkBasic;

public class DBXlinkImporterBasic implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;

	public DBXlinkImporterBasic(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, FROM_TABLE, GMLID, TO_TABLE, ATTRNAME) values " +
			"(?, ?, ?, ?, ?)");
	}

	public boolean insert(DBXlinkBasic xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setInt(2, xlinkEntry.getFromTable().ordinal());
		psXlink.setString(3, xlinkEntry.getGmlId());
		psXlink.setInt(4, xlinkEntry.getToTable().ordinal());

		if (xlinkEntry.getAttrName() != null)
			psXlink.setString(5, xlinkEntry.getAttrName());
		else
			psXlink.setNull(5, Types.VARCHAR);

		psXlink.addBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psXlink.executeBatch();
	}

	@Override
	public void close() throws SQLException {
		psXlink.close();
	}

	@Override
	public DBXlinkImporterEnum getDBXlinkImporterType() {
		return DBXlinkImporterEnum.XLINK_BASIC;
	}

}
