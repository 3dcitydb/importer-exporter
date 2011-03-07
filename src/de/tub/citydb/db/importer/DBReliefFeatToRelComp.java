package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;

public class DBReliefFeatToRelComp implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psReliefFeatToRelComp;
	private int batchCounter;

	public DBReliefFeatToRelComp(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psReliefFeatToRelComp = batchConn.prepareStatement("insert into RELIEF_FEAT_TO_REL_COMP (RELIEF_COMPONENT_ID, RELIEF_FEATURE_ID) values " +
			"(?, ?)");
	}
	
	public void insert(long reliefComponentId, long reliefFeatureId) throws SQLException {
		psReliefFeatToRelComp.setLong(1, reliefComponentId);
		psReliefFeatToRelComp.setLong(2, reliefFeatureId);

		psReliefFeatToRelComp.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.RELIEF_FEAT_TO_REL_COMP);
	}
	
	@Override
	public void executeBatch() throws SQLException {
		psReliefFeatToRelComp.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psReliefFeatToRelComp.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.RELIEF_FEAT_TO_REL_COMP;
	}

}
