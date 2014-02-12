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
package de.tub.citydb.modules.citygml.importer.database.uid;

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

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.modules.citygml.common.database.cache.BranchCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.CacheTableManager;
import de.tub.citydb.modules.citygml.common.database.cache.CacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCachingModel;

public class TextureImageCache implements UIDCachingModel {
	private final int partitions;
	
	private final ReentrantLock mainLock = new ReentrantLock(true);
	private final Condition indexingDone = mainLock.newCondition();

	private CacheTable[] backUpTables;
	private PreparedStatement[] psLookupIds;
	private PreparedStatement[] psDrains;
	private ReentrantLock[] locks;
	private int[] batchCounters;

	private int batchSize;
	private AtomicBoolean enableIndexes = new AtomicBoolean(false);
	private volatile boolean isIndexed = false;

	public TextureImageCache(CacheTableManager cacheTableManager, int partitions, int batchSize) throws SQLException {
		this.partitions = partitions;
		this.batchSize = batchSize;

		BranchCacheTable branchTable = cacheTableManager.createBranchCacheTable(CacheTableModelEnum.TEXTURE_FILE_ID);
		backUpTables = new CacheTable[partitions];
		psLookupIds = new PreparedStatement[partitions];
		psDrains = new PreparedStatement[partitions];
		locks = new ReentrantLock[partitions];
		batchCounters = new int[partitions];

		for (int i = 0; i < partitions; i++) {
			CacheTable tempTable = i == 0 ? branchTable.getMainTable() : branchTable.branch();

			Connection conn = tempTable.getConnection();
			String tableName = tempTable.getTableName();

			backUpTables[i] = tempTable;
			psDrains[i] = conn.prepareStatement("insert into " + tableName + " (FILE_URI, ID) values (?, ?)");
			psLookupIds[i] = conn.prepareStatement("select ID from " + tableName + " where FILE_URI=?");
			locks[i] = new ReentrantLock(true);
		}		
	}

	@Override
	public void drainToDB(ConcurrentHashMap<String, UIDCacheEntry> map, int drain) throws SQLException {
		int drainCounter = 0;	

		// firstly, try and write those entries which have been requested so far
		Iterator<Map.Entry<String, UIDCacheEntry>> iter = map.entrySet().iterator();
		while (drainCounter <= drain && iter.hasNext()) {
			Map.Entry<String, UIDCacheEntry> entry = iter.next();
			if (entry.getValue().isRequested()) {
				String fileURI = entry.getKey();

				// determine partition for gml:id
				int partition = Math.abs(fileURI.hashCode() % partitions);

				// get corresponding prepared statement
				PreparedStatement psDrain = psDrains[partition];

				psDrain.setString(1, fileURI);
				psDrain.setLong(2, entry.getValue().getId());

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
			Map.Entry<String, UIDCacheEntry> entry = iter.next();
			String fileURI = entry.getKey();

			// determine partition for gml:id
			int partition = Math.abs(fileURI.hashCode() % partitions);

			// get corresponding prepared statement
			PreparedStatement psDrain = psDrains[partition];

			psDrain.setString(1, fileURI);
			psDrain.setLong(2, entry.getValue().getId());

			psDrain.addBatch();
			if (++batchCounters[partition] == batchSize) {
				psDrain.executeBatch();
				batchCounters[partition] = 0;
			}

			iter.remove();
			++drainCounter;
		}

		// finally execute batches
		for (int i = 0; i < psDrains.length; i++)
			if (psDrains[i] != null && batchCounters[i] > 0)
				psDrains[i].executeBatch();
	}

	@Override
	public UIDCacheEntry lookupDB(String key) throws SQLException {
		if (enableIndexes.compareAndSet(false, true)) 
			enableIndexesOnCacheTables();

		// wait for tables to be indexed
		if (!isIndexed) {
			final ReentrantLock lock = this.mainLock;
			lock.lock();

			try {
				while (!isIndexed)
					indexingDone.await();
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
				psLookupIds[partition].setString(1, key);
				rs = psLookupIds[partition].executeQuery();

				if (rs.next()) {
					long id = rs.getLong(1);

					return new UIDCacheEntry(id, 0, false, null, CityGMLClass.ABSTRACT_TEXTURE);
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

		for (PreparedStatement ps : psLookupIds)
			if (ps != null)
				ps.close();
	}

	@Override
	public String getType() {
		return "texture image";
	}
	
	private void enableIndexesOnCacheTables() throws SQLException {
		// cache is indexed upon first database lookup
		for (int i = 0; i < partitions; i++)		
			backUpTables[i].createIndexes();		

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			isIndexed = true;
			indexingDone.signalAll();
		} finally {
			lock.unlock();
		}
	}

}
