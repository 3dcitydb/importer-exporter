package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.filter.feature.GmlIdFilter;
import de.tub.citydb.filter.feature.GmlNameFilter;
import de.tub.citydb.filter.statistic.FeatureCounterFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBSplitter {
	private final Logger LOG = Logger.getInstance();
	
	private final DBConnectionPool dbConnectionPool;
	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final ExportFilter exportFilter;
	private final Config config;
	private volatile boolean shouldRun = true;

	private Connection connection;
	private Long firstElement;
	private Long lastElement;
	private long elementCounter;
	private String gmlIdFilter;
	private String gmlNameFilter;

	// filter
	private FeatureClassFilter featureClassFilter;
	private FeatureCounterFilter featureCounterFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;

	public DBSplitter(DBConnectionPool dbConnectionPool, WorkerPool<DBSplittingResult> dbWorkerPool, ExportFilter exportFilter, Config config) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.dbWorkerPool = dbWorkerPool;
		this.exportFilter = exportFilter;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for connection if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.changeWorkspace(
				connection, 
				database.getWorkspace().getExportWorkspace(),
				database.getWorkspace().getExportDate());

		// init filter 
		featureClassFilter = exportFilter.getFeatureClassFilter();
		featureCounterFilter = exportFilter.getFeatureCounterFilter();
		featureGmlIdFilter = exportFilter.getGmlIdFilter();
		featureGmlNameFilter = exportFilter.getGmlNameFilter();

		List<Long> counterFilterState = featureCounterFilter.getFilterState();
		firstElement = counterFilterState.get(0);
		lastElement = counterFilterState.get(1);

		List<String> gmlIdList = featureGmlIdFilter.getFilterState();
		if (gmlIdList != null && !gmlIdList.isEmpty()) {
			String gmlIdFilterString = Util.collection2string(gmlIdList, "', '");
			gmlIdFilter = "AND GMLID in ('" + gmlIdFilterString + "') ";
		} else
			gmlIdFilter = "";

		gmlNameFilter = featureGmlNameFilter.getFilterState();
		if (gmlNameFilter != null)
			gmlNameFilter = gmlNameFilter.toUpperCase();
	}

	public void shutdown() {
		shouldRun = false;
	}

	public void startQuery() throws SQLException {
		// cityObjects
		queryCityObject();

		// wait for jobs to be done...
		try {
			dbWorkerPool.join();
		} catch (InterruptedException e) {
			//
		}

		// cityObjectGroups
		if (!featureClassFilter.filter(CityGMLClass.CITYOBJECTGROUP)) {
			queryCityObjectGroups();

			// wait for jobs to be done...
			if (shouldRun) {
				try {
					dbWorkerPool.join();
				} catch (InterruptedException e) {
					//
				}
			}
		}

		// global appearances
		if (config.getProject().getExporter().getAppearances().isSetExportAppearance())
			queryGlobalAppearance();
	}

	private void queryCityObject() throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		List<String> queryList = new ArrayList<String>();

		// build query strings...
		if (gmlNameFilter != null) {
			// gmlname filter...
			if (!featureClassFilter.filter(CityGMLClass.BUILDING)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join BUILDING j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.CITYFURNITURE)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join CITY_FURNITURE j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.LANDUSE)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join LAND_USE j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.WATERBODY)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join WATERBODY j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.PLANTCOVER)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join PLANT_COVER j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.SOLITARYVEGETATIONOBJECT)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join SOLITARY_VEGETAT_OBJECT j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.TRANSPORTATIONCOMPLEX)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join TRANSPORTATION_COMPLEX j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' " +
				" AND co.CLASS_ID=" + Util.cityObject2classId(CityGMLClass.TRANSPORTATIONCOMPLEX) + " order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.ROAD)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join TRANSPORTATION_COMPLEX j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' " +
				" AND co.CLASS_ID=" + Util.cityObject2classId(CityGMLClass.ROAD) + " order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.RAILWAY)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join TRANSPORTATION_COMPLEX j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' " +
				" AND co.CLASS_ID=" + Util.cityObject2classId(CityGMLClass.RAILWAY) + " order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.TRACK)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join TRANSPORTATION_COMPLEX j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' " +
				" AND co.CLASS_ID=" + Util.cityObject2classId(CityGMLClass.TRACK) + " order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.SQUARE)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join TRANSPORTATION_COMPLEX j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' " +
				" AND co.CLASS_ID=" + Util.cityObject2classId(CityGMLClass.SQUARE) + " order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.RELIEFFEATURE)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join RELIEF_FEATURE j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' " +
				" AND co.CLASS_ID=" + Util.cityObject2classId(CityGMLClass.RELIEFFEATURE) + " order by co.ID asc";

				queryList.add(query);
			}

			if (!featureClassFilter.filter(CityGMLClass.GENERICCITYOBJECT)) {
				String query = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
				" inner join GENERIC_CITYOBJECT j on co.ID=j.ID where upper(j.NAME) like '%" + gmlNameFilter + "%' order by co.ID asc";

				queryList.add(query);
			}

		} else {
			// feature class filter
			List<Integer> classIds = new ArrayList<Integer>();
			List<CityGMLClass> allowedFeature = featureClassFilter.getNotFilterState();
			for (CityGMLClass featureClass : allowedFeature) {
				if (featureClass == CityGMLClass.CITYOBJECTGROUP)
					continue;

				classIds.add(Util.cityObject2classId(featureClass));
			}

			if (!classIds.isEmpty()) {
				String classIdQuery = Util.collection2string(classIds, ", ");

				String query = "select ID, CLASS_ID from CITYOBJECT where CLASS_ID in (" + classIdQuery +") " + gmlIdFilter + " order by ID asc";
				queryList.add(query);
			}
		}

		if (queryList.size() == 0)
			return;

		try {

			for (String query : queryList) {
				stmt = connection.createStatement();
				rs = stmt.executeQuery(query);

				while (rs.next() && shouldRun) {
					elementCounter++;

					if (firstElement != null && elementCounter < firstElement)
						continue;

					if (lastElement != null && elementCounter > lastElement)
						break;

					long primaryKey = rs.getLong(1);
					int classId = rs.getInt(2);
					CityGMLClass cityObjectType = Util.classId2cityObject(classId);

					// set initial context...
					DBSplittingResult splitter = new DBSplittingResult(primaryKey, cityObjectType);
					dbWorkerPool.addWork(splitter);
				}

				rs.close();
			}
		} catch (SQLException sqlEx) {
			System.out.println(sqlEx);
			throw sqlEx;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				stmt = null;
			}
		}
	}

	private void queryCityObjectGroups() throws SQLException {
		if (!shouldRun)
			return;

		LOG.info("Processing CityObjectGroup features.");

		Statement stmt = null;
		ResultSet rs = null;

		FilterConfig filter = config.getProject().getExporter().getFilter();
		String query = null;
		List<Integer> classIds = new ArrayList<Integer>();

		if (filter.isSetSimple()) {
			query = "select ID from CITYOBJECT where CLASS_ID=23 " + gmlIdFilter;
		} else {
			if (featureGmlNameFilter.isActive() && gmlNameFilter != null)
				query = "select ID from CITYOBJECTGROUP where upper(NAME) like '%" + gmlNameFilter + "%'";
			else
				query = "select ID from CITYOBJECTGROUP";
			
			List<CityGMLClass> allowedFeature = featureClassFilter.getNotFilterState();
			for (CityGMLClass featureClass : allowedFeature) {
				if (featureClass == CityGMLClass.CITYOBJECTGROUP)
					continue;

				classIds.add(Util.cityObject2classId(featureClass));
			}
		}

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
			List<Long> groupIds = new ArrayList<Long>();

			while (rs.next() && shouldRun) {	
				long groupId = rs.getLong(1);
				elementCounter++;

				if (firstElement != null && elementCounter < firstElement)
					continue;

				if (lastElement != null && elementCounter > lastElement)
					break;

				groupIds.add(groupId);				
			}

			rs.close();

			Set<Long> visited = new HashSet<Long>();
			for (Long groupId : groupIds) {
				if (visited.contains(groupId))
					continue;

				visited.add(groupId);
				recursiveGroupMemberQuery(groupId, visited, classIds);				
			}			

		} catch (SQLException sqlEx) {
			LOG.error("SQL error: " + sqlEx.getMessage());
			throw sqlEx;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				stmt = null;
			}
		}
	}

	private void recursiveGroupMemberQuery(long groupId, Set<Long> visited, List<Integer> classIds) throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();

			// first: check nested groups being group members
			String innerGroupQuery = "select co.ID from GROUP_TO_CITYOBJECT gtc " +
			"inner join CITYOBJECT co on co.ID=gtc.CITYOBJECT_ID " +
			"where gtc.CITYOBJECTGROUP_ID=" + groupId + " and co.CLASS_ID=23";
			rs = stmt.executeQuery(innerGroupQuery);

			List<Long> groupIds = new ArrayList<Long>();			
			while (rs.next() && shouldRun) {
				long innerGroupId = rs.getLong(1);				
				groupIds.add(innerGroupId);
			}

			rs.close();

			for (Long innerGroupId : groupIds) {
				if (visited.contains(innerGroupId))
					continue;

				visited.add(innerGroupId);
				recursiveGroupMemberQuery(innerGroupId, visited, classIds);
			}

			// classIds filter
			String classIdsString = "";
			if (!classIds.isEmpty())
				classIdsString = "AND co.CLASS_ID in (" + Util.collection2string(classIds, ",") + ")";

			// second: work on groupMembers which are not groups
			String groupMemberQuery = "select co.ID, co.CLASS_ID from CITYOBJECT co " +
			"inner join GROUP_TO_CITYOBJECT gtc on gtc.CITYOBJECT_ID=co.ID " +
			"where gtc.CITYOBJECTGROUP_ID=" + groupId + " and not co.CLASS_ID=23 " + classIdsString;
			rs = stmt.executeQuery(groupMemberQuery);

			while (rs.next() && shouldRun) {				
				long memberId = rs.getLong(1);
				int memberClassId = rs.getInt(2);
				CityGMLClass cityObjectType = Util.classId2cityObject(memberClassId);

				// set initial context...
				DBSplittingResult splitter = new DBSplittingResult(memberId, cityObjectType);
				splitter.setCheckIfAlreadyExported(true);
				dbWorkerPool.addWork(splitter);
			}

			rs.close();

			// third: work on parents which are not groups
			String parentQuery = "select grp.PARENT_CITYOBJECT_ID, co.CLASS_ID from CITYOBJECTGROUP grp " +
			"inner join CITYOBJECT co on co.ID=grp.PARENT_CITYOBJECT_ID " +
			"where grp.ID=" + groupId + " AND not grp.PARENT_CITYOBJECT_ID is NULL and not co.CLASS_ID=23" + classIdsString;
			rs = stmt.executeQuery(parentQuery);

			while (rs.next() && shouldRun) {				
				long memberId = rs.getLong(1);
				int memberClassId = rs.getInt(2);
				CityGMLClass cityObjectType = Util.classId2cityObject(memberClassId);

				// set initial context...
				DBSplittingResult splitter = new DBSplittingResult(memberId, cityObjectType);
				splitter.setCheckIfAlreadyExported(true);
				dbWorkerPool.addWork(splitter);
			}

			rs.close();

			// wait for jobs to be done...
			try {
				dbWorkerPool.join();
			} catch (InterruptedException e) {
				//
			}			

			// finally export group itself
			DBSplittingResult splitter = new DBSplittingResult(groupId, CityGMLClass.CITYOBJECTGROUP);
			splitter.setCheckIfAlreadyExported(true);
			dbWorkerPool.addWork(splitter);

		} catch (SQLException sqlEx) {
			LOG.error("SQL error: " + sqlEx.getMessage());
			throw sqlEx;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				stmt = null;
			}
		}

	}

	private void queryGlobalAppearance() throws SQLException {
		if (!shouldRun)
			return;

		// there are two possible approaches for global appearances.
		// 1. (followed here)
		// iterate over all appearances that are not bound to a specific cityobject. if
		// a surface data member of that appearance is pointing to a geometry object
		// held in the gmlIdCache, than the appearance has to be written. this approach
		// might be slow if we have a large number of global appearances
		// 2.
		// drain all geometry entries held in the gmlIdCache to the database. on the
		// database level make a big join with the temporary cache table
		// (using dynamic_sampling for the temp table) and all relevant appearance tables.
		// the result is a list of those appearances that have to be written. this might
		// be slow if we have a big cache size but only a small number of global appearances

		LOG.info("Processing global appearance features.");

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();
			String query = "select ID from APPEARANCE where CITYOBJECT_ID is NULL";
			rs = stmt.executeQuery(query);

			while (rs.next() && shouldRun) {
				long id = rs.getLong(1);

				// set initial context
				DBSplittingResult splitter = new DBSplittingResult(id, CityGMLClass.APPEARANCE);
				dbWorkerPool.addWork(splitter);
			}

		} catch (SQLException sqlEx) {
			LOG.error("SQL error: " + sqlEx.getMessage());
			throw sqlEx;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				stmt = null;
			}
		}
	}
}
