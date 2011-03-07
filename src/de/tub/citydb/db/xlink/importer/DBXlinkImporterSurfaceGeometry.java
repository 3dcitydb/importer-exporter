package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.xlink.DBXlinkSurfaceGeometry;

public class DBXlinkImporterSurfaceGeometry implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;

	public DBXlinkImporterSurfaceGeometry(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, PARENT_ID, ROOT_ID, REVERSE, GMLID) values " +
			"(?, ?, ?, ?, ?)");
	}

	public boolean insert(DBXlinkSurfaceGeometry xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setLong(2, xlinkEntry.getParentId());
		psXlink.setLong(3, xlinkEntry.getRootId());
		psXlink.setInt(4, xlinkEntry.isReverse() ? 1 : 0);
		psXlink.setString(5, xlinkEntry.getGmlId());

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
		return DBXlinkImporterEnum.SURFACE_GEOMETRY;
	}

}
