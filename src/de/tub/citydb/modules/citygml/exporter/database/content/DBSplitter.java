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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private final ExportFilter exportFilter;
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
			EventDispatcher eventDispatcher, 
			Config config) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.dbWorkerPool = dbWorkerPool;
		this.exportFilter = exportFilter;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for connection if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(
				connection, 
				database.getWorkspaces().getExportWorkspace());

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

				String mask = ((tiledBBox.getTiling().getMode() != TilingMode.NO_TILING || tiledBBox.isSetOverlapMode())) ? 
						"INSIDE+CONTAINS+EQUAL+COVERS+COVEREDBY+OVERLAPBDYINTERSECT" :
							"INSIDE+COVEREDBY+EQUAL";

				bboxFilter = "SDO_RELATE(co.ENVELOPE, MDSYS.SDO_GEOMETRY(2003, " + bboxSrid + ", NULL, " +
				"MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 3), " +
				"MDSYS.SDO_ORDINATE_ARRAY(" + minX + ", " + minY + ", " + maxX + ", " + maxY + ")), " +
				"'querytype=WINDOW mask=" + mask + "') = 'TRUE'";

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

				if (bboxFilter != null)
					query.append(bboxFilter).append(" and ");

				if (additionalWhere != null)
					query.append(additionalWhere).append(" and ");

				query.append("upper(j.NAME) like '%").append(gmlNameFilter).append("%' ");

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
						query.append("and ").append(bboxFilter).append(" ");

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

		LOG.info("Processing CityObjectGroup features.");
		eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.group.msg"), this));

		Statement stmt = null;
		ResultSet rs = null;

		StringBuilder query = new StringBuilder();
		List<Integer> classIds = new ArrayList<Integer>();

		if (expFilterConfig.isSetSimpleFilter()) {
			query.append("select co.ID from CITYOBJECT co where co.CLASS_ID=23 ");
			if (gmlIdFilter != null)
				query.append("and ").append(gmlIdFilter);
		} else {
			query.append("select ").append(optimizerHint).append(" co.ID from CITYOBJECT co");
			
			if (gmlNameFilter != null)
				query.append(", CITYOBJECTGROUP j where co.ID=j.ID ")
				.append("and upper(j.NAME) like '%").append(gmlNameFilter).append("%' ");
			else
				query.append(" where co.CLASS_ID=23 ");

			if (bboxFilter != null)
				query.append("and ").append(bboxFilter);

			List<CityGMLClass> allowedFeature = featureClassFilter.getNotFilterState();
			for (CityGMLClass featureClass : allowedFeature) {
				if (featureClass == CityGMLClass.CITY_OBJECT_GROUP)
					continue;

				classIds.add(Util.cityObject2classId(featureClass));
			}
		}

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query.toString());
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
			StringBuilder innerGroupQuery = new StringBuilder("select ").append(optimizerHint)
			.append(" co.ID from GROUP_TO_CITYOBJECT gtc, CITYOBJECT co where co.ID=gtc.CITYOBJECT_ID ")
			.append("and gtc.CITYOBJECTGROUP_ID=").append(groupId).append(" and co.CLASS_ID=23 ");

			if (bboxFilter != null)
				innerGroupQuery.append("and ").append(bboxFilter);

			rs = stmt.executeQuery(innerGroupQuery.toString());

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
				classIdsString = "and co.CLASS_ID in (" + Util.collection2string(classIds, ",") + ")";

			// second: work on groupMembers which are not groups
			StringBuilder groupMemberQuery = new StringBuilder("select ").append(optimizerHint)
			.append(" co.ID, co.CLASS_ID from CITYOBJECT co, GROUP_TO_CITYOBJECT gtc where gtc.CITYOBJECT_ID=co.ID ")
			.append("and gtc.CITYOBJECTGROUP_ID=").append(groupId);

			if (bboxFilter != null)
				groupMemberQuery.append(" and ").append(bboxFilter);

			groupMemberQuery.append(" and not co.CLASS_ID=23 ").append(classIdsString);

			rs = stmt.executeQuery(groupMemberQuery.toString());

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
			StringBuilder parentQuery = new StringBuilder("select ").append(optimizerHint)
			.append(" grp.PARENT_CITYOBJECT_ID, co.CLASS_ID from CITYOBJECTGROUP grp, CITYOBJECT co ")
			.append("where co.ID=grp.PARENT_CITYOBJECT_ID and grp.ID=").append(groupId).append(" and not grp.PARENT_CITYOBJECT_ID is NULL");

			if (bboxFilter != null)
				parentQuery.append(" and ").append(bboxFilter);

			parentQuery.append(" and not co.CLASS_ID=23 ").append(classIdsString);			

			rs = stmt.executeQuery(parentQuery.toString());

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
			DBSplittingResult splitter = new DBSplittingResult(groupId, CityGMLClass.CITY_OBJECT_GROUP);
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
