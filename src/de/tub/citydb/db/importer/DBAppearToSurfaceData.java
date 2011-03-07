package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;

public class DBAppearToSurfaceData implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAppearToSurfaceData;
	private int batchCounter;

	public DBAppearToSurfaceData(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psAppearToSurfaceData = batchConn.prepareStatement("insert into APPEAR_TO_SURFACE_DATA (SURFACE_DATA_ID, APPEARANCE_ID) values " +
			"(?, ?)");
	}

	public void insert(long surfaceDataId, long appearanceId) throws SQLException {
		psAppearToSurfaceData.setLong(1, surfaceDataId);
		psAppearToSurfaceData.setLong(2, appearanceId);

		psAppearToSurfaceData.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.APPEAR_TO_SURFACE_DATA);
	}

	@Override
	public void executeBatch() throws SQLException {
		psAppearToSurfaceData.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psAppearToSurfaceData.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.APPEAR_TO_SURFACE_DATA;
	}

}
