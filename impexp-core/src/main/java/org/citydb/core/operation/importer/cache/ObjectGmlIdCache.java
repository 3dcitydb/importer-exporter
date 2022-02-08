/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.operation.importer.cache;

import org.citydb.core.operation.common.cache.*;
import org.citydb.core.operation.common.cache.model.CacheTableModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectGmlIdCache implements IdCachingModel {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final int partitions;
	private final CacheTableModel cacheTableModel;
	private final CacheTableManager cacheTableManager;

	private final CacheTable[] backUpTables;
	private final PreparedStatement[] psLookupIds;
	private final PreparedStatement[] psDrains;
	private final ReentrantLock[] locks;
	private final boolean[] isIndexed;
	private final int[] batchCounters;
	private final int batchSize;

	private BranchCacheTable branchTable;

	public ObjectGmlIdCache(CacheTableManager cacheTableManager, int partitions, int batchSize) throws SQLException {
		this.cacheTableManager = cacheTableManager;
		this.partitions = partitions;
		this.batchSize = batchSize;

		cacheTableModel = CacheTableModel.OBJECT_GMLID;
		backUpTables = new CacheTable[partitions];
		psLookupIds = new PreparedStatement[partitions];
		psDrains = new PreparedStatement[partitions];
		locks = new ReentrantLock[partitions];
		isIndexed = new boolean[partitions];
		batchCounters = new int[partitions];

		for (int i = 0; i < partitions; i++) {
			locks[i] = new ReentrantLock();
		}
	}

	@Override
	public void drainToDB(ConcurrentHashMap<String, IdCacheEntry> map, int drain) throws SQLException {
		int drainCounter = 0;	

		// firstly, try and write those entries which have not been requested so far
		Iterator<Map.Entry<String, IdCacheEntry>> iter = map.entrySet().iterator();
		while (drainCounter <= drain && iter.hasNext()) {
			Map.Entry<String, IdCacheEntry> entry = iter.next();
			if (!entry.getValue().isRequested()) {
				String gmlId = entry.getKey();

				// determine partition for gml:id
				int partition = Math.abs(gmlId.hashCode() % partitions);
				initializePartition(partition);

				// get corresponding prepared statement
				PreparedStatement psDrain = psDrains[partition];

				psDrain.setString(1, gmlId);
				psDrain.setLong(2, entry.getValue().getId());
				psDrain.setString(3, entry.getValue().getMapping());
				psDrain.setInt(4, entry.getValue().getObjectClassId());

				psDrain.addBatch();
				if (++batchCounters[partition] == batchSize) {
					psDrain.executeBatch();
					batchCounters[partition] = 0;
				}

				iter.remove();
				++drainCounter;
			}
		}

		// secondly, drain remaining entries until drain limit
		iter = map.entrySet().iterator();
		while (drainCounter <= drain && iter.hasNext()) {
			Map.Entry<String, IdCacheEntry> entry = iter.next();
			String gmlId = entry.getKey();

			// determine partition for gml:id
			int partition = Math.abs(gmlId.hashCode() % partitions);
			initializePartition(partition);

			// get corresponding prepared statement
			PreparedStatement psDrain = psDrains[partition];

			psDrain.setString(1, gmlId);
			psDrain.setLong(2, entry.getValue().getId());
			psDrain.setString(3, entry.getValue().getMapping());
			psDrain.setInt(4, entry.getValue().getObjectClassId());

			psDrain.addBatch();
			if (++batchCounters[partition] == batchSize) {
				psDrain.executeBatch();
				batchCounters[partition] = 0;
			}

			iter.remove();
			++drainCounter;
		}

		// finally execute batches
		for (int i = 0; i < psDrains.length; i++) {
			if (psDrains[i] != null && batchCounters[i] > 0) {
				psDrains[i].executeBatch();
			}
		}
	}

	@Override
	public IdCacheEntry lookupDB(String key) throws SQLException {
		// determine partition for gml:id
		int partition = Math.abs(key.hashCode() % partitions);
		initializePartition(partition);

		// enable indexes upon first lookup
		if (!isIndexed[partition]) {
			enableIndexesOnCacheTable(partition);
		}

		// lock partition
		final ReentrantLock tableLock = this.locks[partition];
		tableLock.lock();

		try {
			psLookupIds[partition].setString(1, key);
			try (ResultSet rs = psLookupIds[partition].executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					String mapping = rs.getString(2);
					int objectClassId = rs.getInt(3);

					return new IdCacheEntry(id, 0, false, mapping, objectClassId);
				}

				return null;
			}
		} finally {
			tableLock.unlock();
		}
	}

	@Override
	public void close() throws SQLException {
		for (PreparedStatement ps : psDrains) {
			if (ps != null) {
				ps.close();
			}
		}

		for (PreparedStatement ps : psLookupIds) {
			if (ps != null) {
				ps.close();
			}
		}
	}

	@Override
	public String getType() {
		return "object";
	}

	private void enableIndexesOnCacheTable(int partition) throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isIndexed[partition]) {
				backUpTables[partition].createIndexes();
				isIndexed[partition] = true;
			}
		} finally {
			lock.unlock();
		}
	}

	private void initializePartition(int partition) throws SQLException {
		if (branchTable == null) {
			mainLock.lock();

			try {
				if (branchTable == null) {
					branchTable = cacheTableManager.createBranchCacheTable(cacheTableModel);
				}
			} finally {
				mainLock.unlock();
			}
		}

		if (backUpTables[partition] == null) {
			final ReentrantLock tableLock = locks[partition];
			tableLock.lock();

			try {
				if (backUpTables[partition] == null) {
					CacheTable tempTable = partition == 0 ? branchTable.getMainTable() : branchTable.branch();

					Connection conn = tempTable.getConnection();
					String tableName = tempTable.getTableName();

					backUpTables[partition] = tempTable;
					psLookupIds[partition] = conn.prepareStatement("select ID, MAPPING, OBJECTCLASS_ID from " + backUpTables[partition].getTableName() + " where GMLID=?");
					psDrains[partition] = conn.prepareStatement("insert into " + tableName + " (GMLID, ID, MAPPING, OBJECTCLASS_ID) values (?, ?, ?, ?)");
				}
			} finally {
				tableLock.unlock();
			}
		}
	}
	
}
