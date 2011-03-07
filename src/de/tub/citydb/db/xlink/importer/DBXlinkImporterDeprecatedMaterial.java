package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.xlink.DBXlinkDeprecatedMaterial;

public class DBXlinkImporterDeprecatedMaterial implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;

	public DBXlinkImporterDeprecatedMaterial(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, GMLID, SURFACE_GEOMETRY_ID) values " +
			"(?, ?, ?)");
	}

	public boolean insert(DBXlinkDeprecatedMaterial xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setString(2, xlinkEntry.getGmlId());
		psXlink.setLong(3, xlinkEntry.getSurfaceGeometryId());

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
		return DBXlinkImporterEnum.XLINK_DEPRECATED_MATERIAL;
	}

}
