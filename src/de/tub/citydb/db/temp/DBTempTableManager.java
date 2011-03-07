package de.tub.citydb.db.temp;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.temp.model.DBTempTableModel;
import de.tub.citydb.db.temp.model.DBTempTableModelEnum;

public class DBTempTableManager {
	private final DBConnectionPool dbPool;	
	private ConcurrentHashMap<DBTempTableModelEnum, DBTempGTT> gttMap;
	
	public DBTempTableManager(DBConnectionPool dbPool, int concurrencyLevel) {
		this.dbPool = dbPool;
		
		gttMap = new ConcurrentHashMap<DBTempTableModelEnum, DBTempGTT>(
				DBTempTableModelEnum.values().length,
				0.75f,
				concurrencyLevel
		);
	}

	public DBTempGTT createGTT(DBTempTableModel model) throws SQLException {
		DBTempGTT tempTable = getOrCreateGTT(model);
		if (!tempTable.isCreated())
			tempTable.create();
		
		return tempTable;
	}
	
	public DBTempGTT createIndexedGTT(DBTempTableModel model) throws SQLException {
		DBTempGTT tempTable = getOrCreateGTT(model);
		if (!tempTable.isCreated())
			tempTable.createIndexed();
		
		return tempTable;
	}
	
	public DBTempGTT createDecoupledGTT(DBTempTableModel model) throws SQLException {
		DBTempGTT tempTable = createGTT(model);
		if (!tempTable.isDecoupled())
			tempTable.decoupleWriter();
		
		return tempTable;
	}

	public DBTempGTT getGTT(DBTempTableModelEnum type) {
		return gttMap.get(type);
	}
	
	public DBTempHeapTable getGTTHeapView(DBTempTableModelEnum type) {
		if (gttMap.containsKey(type))
			return gttMap.get(type).getHeapView();
		
		return null;
	}
	
	public boolean existsGTT(DBTempTableModelEnum type) {
		return gttMap.containsKey(type) && gttMap.get(type).isCreated();
	}
	
	private DBTempGTT getOrCreateGTT(DBTempTableModel model) {
		DBTempGTT tempTable = gttMap.get(model.getType());
		
		if (tempTable == null) {
			DBTempGTT newTempTable = new DBTempGTT(model, dbPool);
			tempTable = gttMap.putIfAbsent(model.getType(), newTempTable);
			if (tempTable == null)
				tempTable = newTempTable;
		}
			
		return tempTable;
	}
		
	public void dropAll() throws SQLException {
		for (DBTempTable tempTable : gttMap.values())
			tempTable.drop();
	}
}
