/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.gmlid;

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

import oracle.jdbc.OraclePreparedStatement;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.modules.citygml.common.database.cache.BranchTemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.cache.HeapCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBCacheModel;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;

public class DBImportCache implements DBCacheModel {
	private final int partitions;
	private final CacheTableModelEnum cacheTableModel;

	private final ReentrantLock mainLock = new ReentrantLock(true);
	private final Condition heapCreationDone = mainLock.newCondition();

	private TemporaryCacheTable[] backUpTables;
	private PreparedStatement[] psLookupGmlIds;
	private PreparedStatement[] psDrains;
	private ReentrantLock[] locks;

	private AtomicBoolean createHeapView = new AtomicBoolean(false);
	private volatile boolean isHeapCreated = false;

	public DBImportCache(CacheManager cacheManager, CacheTableModelEnum cacheTableModel, int partitions, int batchSize) throws SQLException {
		this.partitions = partitions;
		this.cacheTableModel = cacheTableModel;

		BranchTemporaryCacheTable branchTable = cacheManager.createBranchTemporaryCacheTable(cacheTableModel);
		backUpTables = new TemporaryCacheTable[partitions];
		psLookupGmlIds = new PreparedStatement[partitions];
		psDrains = new PreparedStatement[partitions];
		locks = new ReentrantLock[partitions];

		for (int i = 0; i < partitions; i++) {
			TemporaryCacheTable tempTable = i == 0 ? branchTable.getMainTable() : branchTable.branch();

			Connection conn = tempTable.getConnection();
			String tableName = tempTable.getTableName();

			backUpTables[i] = tempTable;
			psDrains[i] = conn.prepareStatement("insert into " + tableName + " (GMLID, ID, ROOT_ID, REVERSE, MAPPING, TYPE) values (?, ?, ?, ?, ?, ?)");
			locks[i] = new ReentrantLock(true);
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
			deriveHeapCacheTables();

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

	private void deriveHeapCacheTables() throws SQLException {
		for (int i = 0; i < partitions; i++) {			
			HeapCacheTable heapView = backUpTables[i].deriveHeapCacheTableWithIndexes();		
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

	@Override
	public void close() throws SQLException {
		for (PreparedStatement ps : psDrains)
			if (ps != null)
				ps.close();
		
		for (PreparedStatement ps : psLookupGmlIds)
			if (ps != null)
				ps.close();
	}
	
	@Override
	public String getType() {
		switch (cacheTableModel) {
		case GMLID_GEOMETRY:
			return "geometry";
		case GMLID_FEATURE:
			return "feature";
		default:
			return "";
		}
	}

}
