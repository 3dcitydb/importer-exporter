package de.tub.citydb.db.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OraclePreparedStatement;
import de.tub.citydb.db.temp.DBTempGTT;
import de.tub.citydb.db.temp.DBTempHeapTable;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.temp.model.DBTempTableIdCache;
import de.tub.citydb.db.temp.model.DBTempTableModelEnum;
import de.tub.citygml4j.model.citygml.CityGMLClass;

public class DBImportCache implements DBCacheModel {
	private final int partitions;

	private final ReentrantLock mainLock = new ReentrantLock(true);
	private final Condition heapCreationDone = mainLock.newCondition();

	private DBTempGTT[] backUpTables;
	private PreparedStatement[] psLookupGmlIds;
	private PreparedStatement[] psDrains;
	private ReentrantLock[] locks;

	private AtomicBoolean createHeapView = new AtomicBoolean(false);
	private volatile boolean isHeapCreated = false;

	public DBImportCache(DBTempTableManager tempTableManager, DBTempTableModelEnum backupTableType, int partitions, int batchSize) throws SQLException {
		this.partitions = partitions;

		DBTempGTT main = tempTableManager.createGTT(new DBTempTableIdCache(backupTableType));
		backUpTables = new DBTempGTT[partitions];
		psLookupGmlIds = new PreparedStatement[partitions];
		psDrains = new PreparedStatement[partitions];
		locks = new ReentrantLock[partitions];

		for (int i = 0; i < partitions; i++) {
			DBTempGTT tempTable = i == 0 ? main : main.branch();

			Connection conn = tempTable.getWriter();
			String tableName = tempTable.getTableName();

			backUpTables[i] = tempTable;
			locks[i] = new ReentrantLock(true);
			psDrains[i] = conn.prepareStatement("insert into " + tableName + " (GMLID, ID, ROOT_ID, REVERSE, MAPPING, TYPE) values (?, ?, ?, ?, ?, ?)");
			((OraclePreparedStatement)psDrains[i]).setExecuteBatch(batchSize);
		}		
	}

	public void drainToDB(ConcurrentHashMap<String, GmlIdEntry> map, int drain) throws SQLException {
		int drainCounter = 0;	

		Iterator<Map.Entry<String, GmlIdEntry>> iter = map.entrySet().iterator();
		while (drainCounter <= drain && iter.hasNext()) {
			Map.Entry<String, GmlIdEntry> entry = iter.next();
			String gmlId = entry.getKey();

			// determine bucket for gml:id
			int partition = Math.abs(gmlId.hashCode() % partitions);

			// get corresponding prepared statement
			PreparedStatement psDrain = psDrains[partition];

			psDrain.setString(1, gmlId);
			psDrain.setLong(2, entry.getValue().getId());
			psDrain.setLong(3, entry.getValue().getRootId());
			psDrain.setInt(4, entry.getValue().isReverse() ? 1 : 0);
			psDrain.setString(5, entry.getValue().getMapping());
			psDrain.setInt(6, entry.getValue().getType().ordinal());

			psDrain.executeUpdate();
			iter.remove();
			++drainCounter;
		}

		// finally send batches
		for (PreparedStatement psDrain : psDrains) 
			if (psDrain != null)
				((OraclePreparedStatement)psDrain).sendBatch();
	}

	public GmlIdEntry lookupDB(String key) throws SQLException {
		if (createHeapView.compareAndSet(false, true)) 
			createHeapView();

		// wait for heap views to be created
		if (!isHeapCreated) {
			final ReentrantLock lock = this.mainLock;
			lock.lock();

			try {
				while (!isHeapCreated)
					heapCreationDone.await();
			} catch (InterruptedException ie) {
				//
			} finally {
				lock.unlock();
			}
		}

		// determine partition for gml:id
		int partition = Math.abs(key.hashCode() % partitions);

		// lock partition
		final ReentrantLock tableLock = this.locks[partition];
		tableLock.lock();

		try {
			ResultSet rs = null;	
			try {
				psLookupGmlIds[partition].setString(1, key);
				rs = psLookupGmlIds[partition].executeQuery();

				if (rs.next()) {
					long id = rs.getLong(1);
					long rootId = rs.getLong(2);
					boolean reverse = rs.getBoolean(3);
					String mapping = rs.getString(4);
					int type = rs.getInt(5);

					return new GmlIdEntry(id, rootId, reverse, mapping, CityGMLClass.fromInt(type));
				}

				return null;
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException sqlEx) {
						//
					}

					rs = null;
				}
			}	
		} finally {
			tableLock.unlock();
		}
	}

	private void createHeapView() throws SQLException {
		for (int i = 0; i < partitions; i++) {			
			DBTempHeapTable heapView = backUpTables[i].createIndexedHeapViewOfWriter();		
			psLookupGmlIds[i] = heapView.getConnection().prepareStatement("select ID, ROOT_ID, REVERSE, MAPPING, TYPE from " + heapView.getTableName() + " where GMLID=?");
		}
		
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		
		try {
			isHeapCreated = true;
			heapCreationDone.signalAll();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public String lookupDB(long id, CityGMLClass type) throws SQLException {
		// nothing to do here 
		return null;
	}

}
