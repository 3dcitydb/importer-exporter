package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;

public class DBWaterBodToWaterBndSrf implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psWaterBodToWaterBndSrf;
	private int batchCounter;

	public DBWaterBodToWaterBndSrf(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psWaterBodToWaterBndSrf = batchConn.prepareStatement("insert into WATERBOD_TO_WATERBND_SRF (WATERBOUNDARY_SURFACE_ID, WATERBODY_ID) values " +
			"(?, ?)");
	}

	public void insert(long waterSurfaceId, long waterBodyId) throws SQLException {
		psWaterBodToWaterBndSrf.setLong(1, waterSurfaceId);
		psWaterBodToWaterBndSrf.setLong(2, waterBodyId);

		psWaterBodToWaterBndSrf.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.WATERBOD_TO_WATERBND_SRF);
	}

	@Override
	public void executeBatch() throws SQLException {
		psWaterBodToWaterBndSrf.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psWaterBodToWaterBndSrf.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.WATERBOD_TO_WATERBND_SRF;
	}
}
