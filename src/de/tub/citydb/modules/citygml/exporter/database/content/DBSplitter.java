/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.filter.TiledBoundingBox;
import de.tub.citydb.config.project.filter.TilingMode;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdLookupServer;
import de.tub.citydb.modules.common.event.StatusDialogMessage;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.common.filter.feature.BoundingBoxFilter;
import de.tub.citydb.modules.common.filter.feature.FeatureClassFilter;
import de.tub.citydb.modules.common.filter.feature.GmlIdFilter;
import de.tub.citydb.modules.common.filter.feature.GmlNameFilter;
import de.tub.citydb.modules.common.filter.statistic.FeatureCounterFilter;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.database.DBUtil;

public class DBSplitter {
	private final Logger LOG = Logger.getInstance();

	private final DatabaseConnectionPool dbConnectionPool;
	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final GmlIdLookupServer featureGmlIdCache;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private volatile boolean shouldRun = true;

	private Connection connection;
	private long elementCounter;

	private Long firstElement;
	private Long lastElement;
	private String gmlIdFilter;
	private String gmlNameFilter;
	private String[] bboxFilter;
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
			GmlIdLookupServer featureGmlIdCache,
			CacheManager cacheManager,
			EventDispatcher eventDispatcher, 
			Config config) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.dbWorkerPool = dbWorkerPool;
		this.featureGmlIdCache = featureGmlIdCache;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		init(exportFilter, cacheManager);
	}

	private void init(ExportFilter exportFilter, CacheManager cacheManager) throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for connection
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(
				connection, 
				database.getWorkspaces().getExportWorkspace());
		
		// create temporary table for global appearances if needed
		if (config.getInternal().isExportGlobalAppearances()) {
			TemporaryCacheTable temp = cacheManager.createTemporaryCacheTableWithIndexes(CacheTableModelEnum.GLOBAL_APPEARANCE);

			// try and change workspace for temporary table
			dbConnectionPool.gotoWorkspace(
					temp.getConnection(), 
					config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
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
		config.getInternal().setUseInternalBBoxFilter(false);
		if (bbox != null) {

			// check whether spatial indexes are active
			if (DBUtil.isIndexed("CITYOBJECT", "ENVELOPE")) {			
				TiledBoundingBox tiledBBox = expFilterConfig.getComplexFilter().getTiledBoundingBox();
				int bboxSrid = boundingBoxFilter.getSrid();

				double minX = bbox.getLowerLeftCorner().getX();
				double minY = bbox.getLowerLeftCorner().getY();
				double maxX = bbox.getUpperRightCorner().getX();
				double maxY = bbox.getUpperRightCorner().getY();

				boolean overlap = tiledBBox.getTiling().getMode() != TilingMode.NO_TILING || tiledBBox.isSetOverlapMode();
				bboxFilter = new String[overlap ? 3 : 2];

				String filter = "SDO_RELATE(co.ENVELOPE, MDSYS.SDO_GEOMETRY(2003, " + bboxSrid + ", NULL, " +
						"MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 3), " +
						"MDSYS.SDO_ORDINATE_ARRAY(" + minX + ", " + minY + ", " + maxX + ", " + maxY + ")), " +
						"'mask=";

				bboxFilter[0] = filter + "inside+coveredby') = 'TRUE'";
				bboxFilter[1] = filter + "equal') = 'TRUE'";				
				if (overlap)
					bboxFilter[2] = filter + "overlapbdyintersect') = 'TRUE'";

				// on Oracle 11g the query performance greatly benefits from setting
				// a no_index hint for the class_id column
				if (dbConnectionPool.getActiveConnectionMetaData().getDatabaseMajorVersion() == 11)
					optimizerHint = "/*+ no_index(co cityobject_fkx) */";

			} else {
				LOG.error("Bounding box filter is enabled although spatial indexes are disabled.");
				LOG.error("Filtering will not be performed using spatial database operations.");
				config.getInternal().setUseInternalBBoxFilter(true);
			}
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

			if (config.getInternal().isExportGlobalAppearances())
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

		Statement stmt = null;
		ResultSet rs = null;

		List<String> queryList = new ArrayList<String>();

		// build query strings...
		if (gmlNameFilter != null) {
			String tableName = null;
			String additionalWhere = null;

			for (CityGMLClass featureClass : featureClassFilter.getNotFilterState()) {
				additionalWhere = null;

				switch (featureClass) {
				case BUILDING:
					tableName = TableEnum.BUILDING.toString();
					break;
				case CITY_FURNITURE:
					tableName = TableEnum.CITY_FURNITURE.toString();
					break;
				case LAND_USE:
					tableName = TableEnum.LAND_USE.toString();
					break;
				case WATER_BODY:
					tableName = TableEnum.WATERBODY.toString();
					break;
				case PLANT_COVER:
					tableName = TableEnum.PLANT_COVER.toString();
					break;
				case SOLITARY_VEGETATION_OBJECT:
					tableName = TableEnum.SOLITARY_VEGETAT_OBJECT.toString();
					break;
				case TRANSPORTATION_COMPLEX:
				case ROAD:
				case RAILWAY:
				case TRACK:
				case SQUARE:
					tableName = TableEnum.TRANSPORTATION_COMPLEX.toString();
					additionalWhere = "co.CLASS_ID=" + Util.cityObject2classId(featureClass);
					break;
				case RELIEF_FEATURE:
					tableName = TableEnum.RELIEF_FEATURE.toString();
					additionalWhere = "co.CLASS_ID=" + Util.cityObject2classId(featureClass);
					break;
				case GENERIC_CITY_OBJECT:
					tableName = TableEnum.GENERIC_CITYOBJECT.toString();
					break;
				default:
					continue;
				}

				StringBuilder query = new StringBuilder("select ").append(optimizerHint).append(" co.ID, co.CLASS_ID from CITYOBJECT co, ")
						.append(tableName).append(" j where co.ID=j.ID and ");

				query.append("upper(j.NAME) like '%").append(gmlNameFilter).append("%' ");

				if (additionalWhere != null)
					query.append("and ").append(additionalWhere).append(" ");

				if (bboxFilter != null)
					query.append(appendSdoRelate(query.toString()));

				if (featureCounterFilter.isActive())
					query.append("order by co.ID");

				queryList.add(query.toString());
			}

		} else {
			StringBuilder query = new StringBuilder();

			if (expFilterConfig.isSetSimpleFilter()) {
				query.append("select co.ID, co.CLASS_ID from CITYOBJECT co where co.CLASS_ID <> 23 ");
				if (gmlIdFilter != null)
					query.append("and ").append(gmlIdFilter);

				queryList.add(query.toString());
			} else {
				query.append("select ").append(optimizerHint).append(" co.ID, co.CLASS_ID from CITYOBJECT co where ");

				List<Integer> classIds = new ArrayList<Integer>();
				List<CityGMLClass> allowedFeature = featureClassFilter.getNotFilterState();
				for (CityGMLClass featureClass : allowedFeature) {
					if (featureClass == CityGMLClass.CITY_OBJECT_GROUP)
						continue;

					classIds.add(Util.cityObject2classId(featureClass));
				}

				if (!classIds.isEmpty()) {
					String classIdQuery = Util.collection2string(classIds, ", ");

					query.append("co.CLASS_ID in (").append(classIdQuery).append(") "); 

					if (bboxFilter != null)
						query.append(appendSdoRelate(query.toString()));

					if (featureCounterFilter.isActive())
						query.append("order by ID");

					queryList.add(query.toString());
				}				
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
				stmt.close();
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
		eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.group.msg"), this));

		Statement groupStmt = null;
		PreparedStatement memberStmt = null;
		ResultSet rs = null;

		StringBuilder groupQuery = new StringBuilder();
		String classIdsString = "";

		if (expFilterConfig.isSetSimpleFilter()) {
			groupQuery.append("select co.ID, co.GMLID from CITYOBJECT co where co.CLASS_ID=23 ");
			if (gmlIdFilter != null)
				groupQuery.append("and ").append(gmlIdFilter);
		} else {
			groupQuery.append("select ").append(optimizerHint).append(" co.ID, co.GMLID from CITYOBJECT co");

			if (gmlNameFilter != null)
				groupQuery.append(", CITYOBJECTGROUP j where co.ID=j.ID ")
				.append("and upper(j.NAME) like '%").append(gmlNameFilter).append("%' ");
			else
				groupQuery.append(" where co.CLASS_ID=23 ");

			if (bboxFilter != null)
				groupQuery.append(appendSdoRelate(groupQuery.toString()));

			List<Integer> classIds = new ArrayList<Integer>();
			for (CityGMLClass featureClass : featureClassFilter.getNotFilterState()) {
				if (featureClass == CityGMLClass.CITY_OBJECT_GROUP)
					continue;

				classIds.add(Util.cityObject2classId(featureClass));
			}

			if (!classIds.isEmpty())
				classIdsString = "and co.CLASS_ID in (" + Util.collection2string(classIds, ",") + ")";
		}

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
			StringBuilder memberQuery = new StringBuilder("select co.ID, co.CLASS_ID, co.GMLID from CITYOBJECT co ")
			.append("where co.ID in (select co.ID from GROUP_TO_CITYOBJECT gtc, CITYOBJECT co ")
			.append("where gtc.CITYOBJECT_ID=co.ID ")
			.append("and gtc.CITYOBJECTGROUP_ID=? ")
			.append(classIdsString).append(" ")
			.append("union all ")
			.append("select grp.PARENT_CITYOBJECT_ID from CITYOBJECTGROUP grp where grp.ID=?) ");

			if (bboxFilter != null)
				memberQuery.append(appendSdoRelate(memberQuery.toString()));
			
			memberStmt = connection.prepareStatement(memberQuery.toString());

			for (int i = 0; shouldRun && i < groupIds.size(); ++i) {
				long groupId = groupIds.get(i);

				int params = bboxFilter == null ? 2 : bboxFilter.length * 2;
				for (int j = 1; j <= params; ++j)
					memberStmt.setLong(j, groupId);

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

	private String appendSdoRelate(String query) {
		StringBuilder unionAll = new StringBuilder();
		for (int i = 0; i < bboxFilter.length; ++i) {
			if (i > 0)
				unionAll.append(query);

			unionAll.append("and ").append(bboxFilter[i]).append(" ");

			if (i < bboxFilter.length - 1)
				unionAll.append("union all ");
		}

		return unionAll.toString();
	}

	private void queryGlobalAppearance() throws SQLException {
		if (!shouldRun)
			return;

		LOG.info("Processing global appearance features.");
		eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.globalApp.msg"), this));

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();
			String query = "select ID from APPEARANCE where CITYOBJECT_ID is NULL";
			rs = stmt.executeQuery(query);

			while (rs.next() && shouldRun) {
				elementCounter++;

				if (firstElement != null && elementCounter < firstElement)
					continue;

				if (lastElement != null && elementCounter > lastElement)
					break;

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
