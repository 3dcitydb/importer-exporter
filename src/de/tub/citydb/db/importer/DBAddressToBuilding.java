package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.tub.citydb.config.internal.Internal;

public class DBAddressToBuilding implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psAddressToBuilding;
	private int batchCounter;
	
	public DBAddressToBuilding(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		psAddressToBuilding = batchConn.prepareStatement("insert into ADDRESS_TO_BUILDING (BUILDING_ID, ADDRESS_ID) values " +
			"(?, ?)");
	}
	
	public void insert(long addressId, long buildingId) throws SQLException {
		psAddressToBuilding.setLong(1, buildingId);
		psAddressToBuilding.setLong(2, addressId);

		psAddressToBuilding.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.ADDRESS_TO_BUILDING);
	}
	
	@Override
	public void executeBatch() throws SQLException {
		psAddressToBuilding.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psAddressToBuilding.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.ADDRESS_TO_BUILDING;
	}

}
