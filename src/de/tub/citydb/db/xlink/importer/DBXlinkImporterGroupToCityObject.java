package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.xlink.DBXlinkGroupToCityObject;

public class DBXlinkImporterGroupToCityObject implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;

	public DBXlinkImporterGroupToCityObject(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}
	
	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (GROUP_ID, GMLID, IS_PARENT, ROLE) values " +
			"(?, ?, ?, ?)");
	}
	
	public boolean insert(DBXlinkGroupToCityObject xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getGroupId());
		psXlink.setString(2, xlinkEntry.getGmlId());		
		psXlink.setInt(3, xlinkEntry.isParent() ? 1 : 0);		
		psXlink.setString(4, xlinkEntry.getRole());

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
		return DBXlinkImporterEnum.GROUP_TO_CITYOBJECT;
	}

}
