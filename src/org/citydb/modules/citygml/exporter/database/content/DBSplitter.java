/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.ExportFilterConfig;
import org.citydb.config.project.filter.TiledBoundingBox;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citydb.modules.citygml.common.database.uid.UIDCache;
import org.citydb.modules.common.event.StatusDialogMessage;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.BoundingBoxFilter;
import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.modules.common.filter.feature.GmlNameFilter;
import org.citydb.modules.common.filter.statistic.FeatureCounterFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

public class DBSplitter {
	private final Logger LOG = Logger.getInstance();

	private final DatabaseConnectionPool dbConnectionPool;
	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final UIDCache featureGmlIdCache;
	private final CacheTableManager cacheTableManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private volatile boolean shouldRun = true;

	private Connection connection;
	private long elementCounter;

	private Long firstElement;
	private Long lastElement;
	private String gmlIdFilter;
	private String gmlNameFilter;
	private String bboxFilter;
	private String optimizerHint;

	private FeatureClassFilter featureClassFilter;
	private FeatureCounterFilter featureCounterFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;

	private ExportFilterConfig expFilterConfig;

	public DBSplitter(DatabaseConnectionPool dbConnectionPool, 
			WorkerPool<DBSplittingResult> dbWorkerPool, 
			ExportFilter exportFilter, 
			UIDCache featureGmlIdCache,
			CacheTableManager cacheTableManager,
			EventDispatcher eventDispatcher, 
			Config config) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.dbWorkerPool = dbWorkerPool;
		this.featureGmlIdCache = featureGmlIdCache;
		this.cacheTableManager = cacheTableManager;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		init(exportFilter, cacheTableManager);
	}

	private void init(ExportFilter exportFilter, CacheTableManager cacheTableManager) throws SQLException {
		connection = dbConnectionPool.getConnection();

		// try and change workspace for connection
		if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(
					connection, 
					config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
		}

		// create temporary table for global appearances if needed
		if (config.getInternal().isExportGlobalAppearances()) {
			CacheTable temp = cacheTableManager.createCacheTableInDatabase(CacheTableModelEnum.GLOBAL_APPEARANCE);

			// try and change workspace for temporary table
			if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
				dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(
						temp.getConnection(), 
						config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
			}
		}

		// set filter instances 
		featureClassFilter = exportFilter.getFeatureClassFilter();
		featureCounterFilter = exportFilter.getFeatureCounterFilter();
		featureGmlIdFilter = exportFilter.getGmlIdFilter();
		featureGmlNameFilter = exportFilter.getGmlNameFilter();
		boundingBoxFilter = exportFilter.getBoundingBoxFilter();

		expFilterConfig = config.getProject().getExporter().getFilter();
	}

	private void initFilter() throws SQLException {
		// do not use any optimizer hints per default
		optimizerHint = "";

		// feature counter filter
		List<Long> counterFilterState = featureCounterFilter.getFilterState();
		firstElement = counterFilterState.get(0);
		lastElement = counterFilterState.get(1);

		// gml:id filter
		List<String> gmlIdList = featureGmlIdFilter.getFilterState();
		if (gmlIdList != null && !gmlIdList.isEmpty()) {
			String gmlIdFilterString = Util.collection2string(gmlIdList, "', '");
			gmlIdFilter = "co.GMLID in ('" + gmlIdFilterString + "')";
		}

		// gml:name filter
		gmlNameFilter = featureGmlNameFilter.getFilterState();
		if (gmlNameFilter != null)
			gmlNameFilter = gmlNameFilter.toUpperCase();

		// bounding box filter
		BoundingBox bbox = boundingBoxFilter.getFilterState();
		if (bbox != null) {
			if (!bbox.getSrs().isSupported())
				throw new SQLException("The SRID " + bbox.getSrs().getSrid() + " of the bounding box filter is not supported.");

			TiledBoundingBox tiledBBox = expFilterConfig.getComplexFilter().getTiledBoundingBox();

			// convert the srid of the bbox to that of the database
			DatabaseSrs dbSrs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();				
			if (bbox.getSrs().getSrid() != dbSrs.getSrid())	{
				try {
					bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().transformBoundingBox(bbox, bbox.getSrs(), dbSrs);
				} catch (SQLException e) {
					throw new SQLException("Failed to transform bounding box filter to database SRID.", e);
				}
			}

			boolean overlap = tiledBBox.getTiling().getMode() != TilingMode.NO_TILING || tiledBBox.isSetOverlapMode();
			bboxFilter = dbConnectionPool.getActiveDatabaseAdapter().getSQLAdapter().getBoundingBoxPredicate("ENVELOPE", "co", bbox, overlap);

			// check whether a no_index hint for the objectclass_id column improves query performance
			if (dbConnectionPool.getActiveDatabaseAdapter().getSQLAdapter().spatialPredicateRequiresNoIndexHint())
				optimizerHint = "/*+ no_index(co cityobject_objectclass_fkx) */";
		}
	}

	public void shutdown() {
		shouldRun = false;
	}

	public void startQuery() throws SQLException {
		try {
			initFilter();
			queryCityObject();

			if (shouldRun) {
				try {
					dbWorkerPool.join();
				} catch (InterruptedException e) {
					//
				}
			}

			if (!featureClassFilter.filter(CityGMLClass.CITY_OBJECT_GROUP)) {
				queryCityObjectGroups();

				if (shouldRun) {
					try {
						dbWorkerPool.join();
					} catch (InterruptedException e) {
						//
					}
				}
			}

			if (config.getInternal().isExportGlobalAppearances() && elementCounter > 0)
				queryGlobalAppearance();

		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqlEx) {
					//
				}

				connection = null;
			}
		}
	}

	private void queryCityObject() throws SQLException {
		if (!shouldRun)
			return;

		// build query string...
		StringBuilder query = new StringBuilder();

		if (expFilterConfig.isSetSimpleFilter()) {
			query.append("select co.ID, co.OBJECTCLASS_ID from CITYOBJECT co where co.OBJECTCLASS_ID <> ").append(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP)).append(" ");
			if (gmlIdFilter != null)
				query.append("and ").append(gmlIdFilter);

		} else {
			query.append("select ").append(optimizerHint).append(" co.ID, co.OBJECTCLASS_ID from CITYOBJECT co where ");

			List<Integer> classIds = new ArrayList<Integer>();
			List<CityGMLClass> allowedFeature = featureClassFilter.getNotFilterState();
			for (CityGMLClass featureClass : allowedFeature) {
				if (featureClass == CityGMLClass.CITY_OBJECT_GROUP)
					continue;

				classIds.add(Util.cityObject2classId(featureClass));
			}

			if (classIds.isEmpty())
				return;

			String classIdQuery = Util.collection2string(classIds, ", ");
			query.append("co.OBJECTCLASS_ID in (").append(classIdQuery).append(") "); 

			if (gmlNameFilter != null)
				query.append("and upper(co.NAME) like '%").append(gmlNameFilter).append("%' ");

			if (bboxFilter != null)
				query.append("and ").append(bboxFilter);

			if (featureCounterFilter.isActive())
				query.append("order by ID");
		}

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query.toString());

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

		} catch (SQLException sqlEx) {
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

		if (lastElement != null && elementCounter > lastElement)
			return;

		LOG.info("Processing CityObjectGroup features.");
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.group.msg"), this));

		StringBuilder groupQuery = new StringBuilder();
		String classIdsString = "";

		if (expFilterConfig.isSetSimpleFilter()) {
			groupQuery.append("select co.ID, co.GMLID from CITYOBJECT co where co.OBJECTCLASS_ID=").append(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP)).append(" ");
			if (gmlIdFilter != null)
				groupQuery.append("and ").append(gmlIdFilter);
		} else {
			groupQuery.append("select ").append(optimizerHint).append(" co.ID, co.GMLID from CITYOBJECT co ")
			.append("where co.OBJECTCLASS_ID=").append(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP)).append(" ");

			if (gmlNameFilter != null)
				groupQuery.append("and upper(co.NAME) like '%").append(gmlNameFilter).append("%' ");

			if (bboxFilter != null)
				groupQuery.append("and ").append(bboxFilter);

			// build list of class ids for querying group members
			List<Integer> classIds = new ArrayList<Integer>();
			for (CityGMLClass featureClass : featureClassFilter.getNotFilterState()) {
				if (featureClass == CityGMLClass.CITY_OBJECT_GROUP)
					continue;

				classIds.add(Util.cityObject2classId(featureClass));
			}

			if (!classIds.isEmpty())
				classIdsString = "and co.OBJECTCLASS_ID in (" + Util.collection2string(classIds, ",") + ")";
		}

		Statement groupStmt = null;
		PreparedStatement memberStmt = null;
		ResultSet rs = null;

		try {
			// first step: retrieve group ids
			groupStmt = connection.createStatement();
			rs = groupStmt.executeQuery(groupQuery.toString());
			List<Long> groupIds = new ArrayList<Long>();

			while (rs.next() && shouldRun) {	
				elementCounter++;

				if (firstElement != null && elementCounter < firstElement)
					continue;

				if (lastElement != null && elementCounter > lastElement)
					break;

				long groupId = rs.getLong(1);
				String gmlId = rs.getString(2);

				// register group in gml:id cache
				if (gmlId.length() > 0)
					featureGmlIdCache.put(gmlId, groupId, -1, false, null, CityGMLClass.CITY_OBJECT_GROUP);

				groupIds.add(groupId);				
			}

			rs.close();	
			groupStmt.close();

			// second step: export group members
			if (!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks()) {
				StringBuilder memberQuery = new StringBuilder("select co.ID, co.OBJECTCLASS_ID, co.GMLID from CITYOBJECT co ")
						.append("where co.ID in (select co.ID from GROUP_TO_CITYOBJECT gtc, CITYOBJECT co ")
						.append("where gtc.CITYOBJECT_ID=co.ID ")
						.append("and gtc.CITYOBJECTGROUP_ID=? ")
						.append(classIdsString).append(" ")
						.append("union all ")
						.append("select grp.PARENT_CITYOBJECT_ID from CITYOBJECTGROUP grp where grp.ID=?) ");

				if (bboxFilter != null)
					memberQuery.append("and ").append(bboxFilter);

				memberStmt = connection.prepareStatement(memberQuery.toString());

				for (int i = 0; shouldRun && i < groupIds.size(); ++i) {
					long groupId = groupIds.get(i);
					memberStmt.setLong(1, groupId);
					memberStmt.setLong(2, groupId);

					rs = memberStmt.executeQuery();

					while (rs.next() && shouldRun) {				
						long memberId = rs.getLong(1);
						int memberClassId = rs.getInt(2);
						String gmlId = rs.getString(3);
						CityGMLClass cityObjectType = Util.classId2cityObject(memberClassId);

						if (cityObjectType == CityGMLClass.CITY_OBJECT_GROUP) {						
							// register group in gml:id cache
							if (gmlId.length() > 0)
								featureGmlIdCache.put(gmlId, memberId, -1, false, null, CityGMLClass.CITY_OBJECT_GROUP);

							if (!groupIds.contains(memberId))
								groupIds.add(memberId);

							continue;
						}

						// set initial context...
						DBSplittingResult splitter = new DBSplittingResult(gmlId, memberId, cityObjectType);
						splitter.setCheckIfAlreadyExported(true);
						dbWorkerPool.addWork(splitter);
					} 

					rs.close();
				}

				memberStmt.close();

				// wait for jobs to be done...
				try {
					dbWorkerPool.join();
				} catch (InterruptedException e) {
					//
				}
			}

			// finally export groups themselves
			// we assume that all group members have been exported and their gml:ids
			// are registered in the gml:id cache - that's why we registered groups above
			for (long groupId : groupIds) {
				if (!shouldRun)
					break;

				DBSplittingResult splitter = new DBSplittingResult(groupId, CityGMLClass.CITY_OBJECT_GROUP);
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

			if (groupStmt != null) {
				try {
					groupStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				groupStmt = null;
			}

			if (memberStmt != null) {
				try {
					memberStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				memberStmt = null;
			}
		}
	}

	private void queryGlobalAppearance() throws SQLException {
		if (!shouldRun)
			return;

		LOG.info("Processing global appearance features.");
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.globalApp.msg"), this));

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			CacheTable globalAppTemplTable = cacheTableManager.getCacheTable(CacheTableModelEnum.GLOBAL_APPEARANCE);
			globalAppTemplTable.createIndexes();

			StringBuilder query = new StringBuilder("select distinct a.ID from APPEARANCE a ")
					.append("inner join APPEAR_TO_SURFACE_DATA asd on asd.APPEARANCE_ID = a.ID ")
					.append("inner join SURFACE_DATA sd on sd.ID = asd.SURFACE_DATA_ID ")
					.append("inner join TEXTUREPARAM tp on tp.SURFACE_DATA_ID = sd.ID ")
					.append("inner join " + globalAppTemplTable.getTableName() + " tmp on tmp.ID = tp.SURFACE_GEOMETRY_ID ")
					.append("where a.CITYOBJECT_ID is null");

			stmt = globalAppTemplTable.getConnection().prepareStatement(query.toString());
			rs = stmt.executeQuery();

			while (rs.next() && shouldRun) {
				long appearanceId = rs.getLong(1);

				// send appearance to export workers
				DBSplittingResult splitter = new DBSplittingResult(appearanceId, CityGMLClass.APPEARANCE);
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
