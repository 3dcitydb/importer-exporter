package de.tub.citydb.db.xlink.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;

public class DBXlinkImporterTextureParam implements DBXlinkImporter {
	private final TemporaryCacheTable tempTable;
	private PreparedStatement psXlink;

	public DBXlinkImporterTextureParam(TemporaryCacheTable tempTable) throws SQLException {
		this.tempTable = tempTable;

		init();
	}

	private void init() throws SQLException {
		psXlink = tempTable.getConnection().prepareStatement("insert into " + tempTable.getTableName() + 
			" (ID, GMLID, TYPE, IS_TEXTURE_PARAMETERIZATION, TEXPARAM_GMLID, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, TARGET_URI, TEXCOORDLIST_ID) values " +
			"(?, ?, ?, ?, ?, ?, ?, ?, ?)");
	}

	public boolean insert(DBXlinkTextureParam xlinkEntry) throws SQLException {
		psXlink.setLong(1, xlinkEntry.getId());
		psXlink.setString(2, xlinkEntry.getGmlId());
		psXlink.setInt(3, xlinkEntry.getType().ordinal());
		psXlink.setInt(4, xlinkEntry.isTextureParameterization() ? 1 : 0);

		if (xlinkEntry.getTexParamGmlId() != null && xlinkEntry.getTexParamGmlId().length() != 0)
			psXlink.setString(5, xlinkEntry.getTexParamGmlId());
		else
			psXlink.setNull(5, Types.VARCHAR);

		if (xlinkEntry.getWorldToTexture() != null && xlinkEntry.getWorldToTexture().length() != 0)
			psXlink.setString(6, xlinkEntry.getWorldToTexture());
		else
			psXlink.setNull(6, Types.VARCHAR);

		if (xlinkEntry.getTextureCoord() != null && xlinkEntry.getTextureCoord().length() != 0)
			psXlink.setString(7, xlinkEntry.getTextureCoord());
		else
			psXlink.setNull(7, Types.VARCHAR);

		if (xlinkEntry.getTargetURI() != null && xlinkEntry.getTargetURI().length() != 0)
			psXlink.setString(8, xlinkEntry.getTargetURI());
		else
			psXlink.setNull(8, Types.VARCHAR);

		if (xlinkEntry.getTexCoordListId() != null && xlinkEntry.getTexCoordListId().length() != 0)
			psXlink.setString(9, xlinkEntry.getTexCoordListId());
		else
			psXlink.setNull(9, Types.VARCHAR);

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
		return DBXlinkImporterEnum.XLINK_TEXTUREPARAM;
	}

}
