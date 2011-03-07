package de.tub.citydb.db.gmlId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OraclePreparedStatement;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.db.cache.BranchTemporaryCacheTable;
import de.tub.citydb.db.cache.CacheManager;
import de.tub.citydb.db.cache.TemporaryCacheTable;
import de.tub.citydb.db.cache.model.CacheTableModelEnum;

public class DBExportCache implements DBCacheModel {
	private final int partitions;
	private final CacheTableModelEnum cacheTableModel;

	private TemporaryCacheTable[] backUpTables;
	private PreparedStatement[] psLookupDbIds;
	private PreparedStatement[] psLookupGmlIds;
	private PreparedStatement[] psDrains;
	private ReentrantLock[] locks;

	public DBExportCache(CacheManager cacheManager, CacheTableModelEnum cacheTableModel, int partitions, int batchSize) throws SQLException {
		this.partitions = partitions;
		this.cacheTableModel = cacheTableModel;

		BranchTemporaryCacheTable branchTable = cacheManager.createBranchTemporaryCacheTableWithIndexes(cacheTableModel);
		backUpTables = new TemporaryCacheTable[partitions];
		psLookupDbIds = new PreparedStatement[partitions];
		psLookupGmlIds = new PreparedStatement[partitions];
		psDrains = new PreparedStatement[partitions];
		locks = new ReentrantLock[partitions];

		for (int i = 0; i < partitions; i++) {
			TemporaryCacheTable tempTable = i == 0 ? branchTable.getMainTable() : branchTable.branchWithIndexes();

			Connection conn = tempTable.getConnection();
			String tableName = tempTable.getTableName();

			backUpTables[i] = tempTable;
			locks[i] = new ReentrantLock(true);
			psLookupDbIds[i] = conn.prepareStatement("select GMLID, TYPE from " + tableName + " where ID=?");
			psLookupGmlIds[i] = conn.prepareStatement("select ID, ROOT_ID, REVERSE, MAPPING, TYPE from " + tableName + " where GMLID=?");
			psDrains[i] = conn.prepareStatement("insert into " + tableName + " (GMLID, ID, ROOT_ID, REVERSE, MAPPING, TYPE) values (?, ?, ?, ?, ?, ?)");
			((OraclePreparedStatement)psDrains[i]).setExecuteBatch(batchSize);
		}
	}

	@Override
	public void drainToDB(ConcurrentHashMap<String, GmlIdEntry> map, int drain) throws SQLException {
		int drainCounter = 0;			

		// firstly, try and write those entries which have already been requested
		Iterator<Map.Entry<String, GmlIdEntry>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, GmlIdEntry> entry = iter.next();
			if (entry.getValue().isRequested()) { 
				String gmlId = entry.getKey();

				// determine partition for gml:id
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
		}

		// secondly, drain remaining entries until drain limit
		iter = map.entrySet().iterator();
		while (drainCounter <= drain && iter.hasNext()) {
			Map.Entry<String, GmlIdEntry> entry = iter.next();				
			String gmlId = entry.getKey();

			// determine partition for gml:id
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

	@Override
	public GmlIdEntry lookupDB(String key) throws SQLException { 
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

	@Override
	public String lookupDB(long id, CityGMLClass type) throws SQLException {
		// since we cannot determine the partition by id we have to check all of them. 
		// this is definitely a drawback of this partitions approach  			
		for (int i = 0; i < partitions; i++) {
			final ReentrantLock tableLock = locks[i];
			tableLock.lock();

			try {
				ResultSet rs = null;
				try {
					psLookupDbIds[i].setLong(1, id);
					rs = psLookupDbIds[i].executeQuery();

					while (rs.next()) {
						CityGMLClass dbType = CityGMLClass.fromInt(rs.getInt(2));
						if (!dbType.childOrSelf(type))
							continue;

						return rs.getString(1);
					}		
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

		return null;
	}

	@Override
	public void close() throws SQLException {
		for (PreparedStatement ps : psDrains)
			if (ps != null)
				ps.close();
		
		for (PreparedStatement ps : psLookupDbIds)
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
