package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;

public class DBOpeningToThemSurface implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psOpeningToThemSurface;
	private int batchCounter;

	public DBOpeningToThemSurface(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psOpeningToThemSurface = batchConn.prepareStatement("insert into OPENING_TO_THEM_SURFACE (OPENING_ID, THEMATIC_SURFACE_ID) values " +
			"(?, ?)");
	}

	public void insert(long openingId, long thematicSurfaceId) throws SQLException {
        psOpeningToThemSurface.setLong(1, openingId);
        psOpeningToThemSurface.setLong(2, thematicSurfaceId);

        psOpeningToThemSurface.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.OPENING_TO_THEM_SURFACE);
	}

	@Override
	public void executeBatch() throws SQLException {
		psOpeningToThemSurface.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psOpeningToThemSurface.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.OPENING_TO_THEM_SURFACE;
	}

}
