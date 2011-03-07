package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.xlink.DBXlinkLinearRing;

public class DBXlinkImporterLinearRing implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psLinearRing;

	public DBXlinkImporterLinearRing(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psLinearRing = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (GMLID, PARENT_GMLID, RING_NO) values " +
			"(?, ?, ?)");
	}

	public boolean insert(DBXlinkLinearRing xlinkEntry) throws SQLException {
		psLinearRing.setString(1, xlinkEntry.getGmlId());
		psLinearRing.setString(2, xlinkEntry.getParentGmlId());
		psLinearRing.setInt(3, xlinkEntry.getRingId());

		psLinearRing.addBatch();

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psLinearRing.executeBatch();
	}

	@Override
	public void close() throws SQLException {
		psLinearRing.close();
	}

	@Override
	public DBXlinkImporterEnum getDBXlinkImporterType() {
		return DBXlinkImporterEnum.LINEAR_RING;
	}

}
