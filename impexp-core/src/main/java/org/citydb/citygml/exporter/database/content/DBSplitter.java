/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citydb.citygml.common.database.uid.UIDCache;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.AppearanceFilterBuilder;
import org.citydb.query.builder.sql.BuildProperties;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;
import org.citydb.sqlbuilder.select.operator.set.SetOperationFactory;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citygml4j.model.module.citygml.AppearanceModule;
import org.citygml4j.model.module.citygml.CityObjectGroupModule;
import org.citygml4j.model.module.citygml.CoreModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DBSplitter {
	private final Logger log = Logger.getInstance();

	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final Query query;
	private final UIDCache featureGmlIdCache;
	private final CacheTableManager cacheTableManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private final AbstractDatabaseAdapter databaseAdapter;
	private final Connection connection;
	private final String schema;
	private final SchemaMapping schemaMapping;
	private final SQLQueryBuilder builder;

	private volatile boolean shouldRun = true;
	private boolean calculateNumberMatched;
	private long elementCounter;

	public DBSplitter(SchemaMapping schemaMapping,
			WorkerPool<DBSplittingResult> dbWorkerPool, 
			Query query,
			UIDCache featureGmlIdCache,
			CacheTableManager cacheTableManager,
			EventDispatcher eventDispatcher, 
			Config config) throws SQLException {
		this.schemaMapping = schemaMapping;
		this.dbWorkerPool = dbWorkerPool;
		this.query = query;
		this.featureGmlIdCache = featureGmlIdCache;
		this.cacheTableManager = cacheTableManager;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		connection = DatabaseConnectionPool.getInstance().getConnection();
		schema = databaseAdapter.getConnectionDetails().getSchema();

		// try and change workspace for connection
		if (databaseAdapter.hasVersioningSupport()) {
			databaseAdapter.getWorkspaceManager().gotoWorkspace(
					connection, 
					config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
		}

		// create temporary table for global appearances if needed
		if (config.getInternal().isExportGlobalAppearances()) {
			CacheTable temp = cacheTableManager.createCacheTableInDatabase(CacheTableModelEnum.ID_LIST);

			// try and change workspace for temporary table
			if (databaseAdapter.hasVersioningSupport()) {
				databaseAdapter.getWorkspaceManager().gotoWorkspace(
						temp.getConnection(), 
						config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
			}
		}

		BuildProperties buildProperties = BuildProperties.defaults()
				.addProjectionColumn(MappingConstants.GMLID);

		builder = new SQLQueryBuilder(
				schemaMapping, 
				databaseAdapter, 
				buildProperties);
	}

	public boolean isCalculateNumberMatched() {
		return calculateNumberMatched;
	}

	public void setCalculateNumberMatched(boolean calculateNumberMatched) {
		this.calculateNumberMatched = calculateNumberMatched;
	}

	public void shutdown() {
		shouldRun = false;
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));
	}

	public void startQuery() throws SQLException, QueryBuildException, FilterException {
		try {
			FeatureType cityObjectGroupType = schemaMapping.getFeatureType("CityObjectGroup", CityObjectGroupModule.v2_0_0.getNamespaceURI());
			HashMap<Long, AbstractObjectType<?>> cityObjectGroups = new HashMap<>();
			
			queryCityObject(cityObjectGroupType, cityObjectGroups);

			if (shouldRun) {
				try {
					dbWorkerPool.join();
				} catch (InterruptedException e) {
					//
				}
			}

			if (!cityObjectGroups.isEmpty()) {
				queryCityObjectGroups(cityObjectGroupType, cityObjectGroups);

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
			if (connection != null)
				connection.close();
		}
	}

	private void queryCityObject(FeatureType cityObjectGroupType, HashMap<Long, AbstractObjectType<?>> cityObjectGroups) throws SQLException, QueryBuildException {
		if (!shouldRun)
			return;

		if (query.getFeatureTypeFilter().isEmpty())
			return;

		// create query statement
		Select select = builder.buildQuery(query);
		if (query.isSetCounterFilter())
			select.addOrderBy(new OrderByToken((Column)select.getProjection().get(0)));

		// add hits counter
		if (calculateNumberMatched) {
			Table table = new Table(select);
			select = new Select()
					.addProjection(table.getColumn(MappingConstants.ID))
					.addProjection(table.getColumn(MappingConstants.OBJECTCLASS_ID))
					.addProjection(table.getColumn(MappingConstants.GMLID))
					.addProjection(new Function("count(1) over", "hits"));
		}

		// issue query
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			rs = stmt.executeQuery();

			if (rs.next()) {
				if (calculateNumberMatched) {
					long hits = rs.getLong("hits");
					log.info("Found " + hits + " top-level feature(s) matching the request.");

					if (query.isSetCounterFilter()) {
						long maxCount = query.getCounterFilter().getUpperLimit() - query.getCounterFilter().getLowerLimit() + 1;					
						if (maxCount < hits) {
							log.info("Exporting " + maxCount + " top-level feature(s) due to counter settings.");
							hits = maxCount;
						}
					}

					if (query.isSetTiling())
						log.info("The total number of exported features might be less due to tiling settings.");

					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int)hits, this));
				}
				
				do {
					elementCounter++;

					if (query.isSetCounterFilter()) {
						if (elementCounter < query.getCounterFilter().getLowerLimit())
							continue;

						if (elementCounter > query.getCounterFilter().getUpperLimit())
							break;
					}

					long id = rs.getLong("id");
					int objectClassId = rs.getInt("objectclass_id");

					AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(objectClassId);
					if (objectType == null) {
						log.error("Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + id + ").");
						continue;
					}
					
					if (objectType.isEqualToOrSubTypeOf(cityObjectGroupType)) {
						String gmlId = rs.getString("gmlid");
						cityObjectGroups.put(id, objectType);

						// register group in gml:id cache
						if (gmlId != null && gmlId.length() > 0)
							featureGmlIdCache.put(gmlId, id, -1, false, null, objectClassId);

						continue;
					}

					// set initial context...
					DBSplittingResult splitter = new DBSplittingResult(id, objectType);
					dbWorkerPool.addWork(splitter);
				} while (rs.next() && shouldRun);
			} else
				log.info("No top-level feature matches the request.");

		} finally {
			if (rs != null)
				rs.close();

			if (stmt != null)
				stmt.close();
		}
	}

	private void queryCityObjectGroups(FeatureType cityObjectGroupType, HashMap<Long, AbstractObjectType<?>> cityObjectGroups) throws SQLException, FilterException, QueryBuildException {
		if (!shouldRun)
			return;

		log.info("Processing CityObjectGroup features.");
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.group.msg"), this));

		// first step: export group members
		int hits = 0;
		if (!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks()) {

			// exclude previously exported features
			Select notInQuery = builder.buildQuery(query);
			ProjectionToken token = notInQuery.getProjection().get(0);
			notInQuery.unsetProjection().addProjection(token);

			// prepare query for group members
			Query groupQuery = new Query(query);

			// add all feature types if the type filter only contains CityObjectGroup
			if (groupQuery.getFeatureTypeFilter().size() == 1)
				groupQuery.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));

			// set generic spatial filter
			if (groupQuery.isSetSelection()) {
				Predicate predicate = groupQuery.getSelection().getGenericSpatialFilter(schemaMapping.getCommonSuperType(groupQuery.getFeatureTypeFilter().getFeatureTypes()));
				groupQuery.setSelection(new SelectionFilter(predicate));
			}

			// create query statement
			Select select = builder.buildQuery(groupQuery);
			if (groupQuery.isSetCounterFilter())
				select.addOrderBy(new OrderByToken((Column)select.getProjection().get(0)));

			// join group members
			Table cityObject = new Table("cityobject", schema);
			Table cityObjectGroup = new Table("cityobjectgroup", schema);
			Table groupToCityObject = new Table("group_to_cityobject", schema);
			LiteralList idLiteralList = new LiteralList(cityObjectGroups.keySet().stream().toArray(Long[]::new));

			select.addSelection(LogicalOperationFactory.AND(
					ComparisonFactory.in((Column)select.getProjection().get(0), 
							SetOperationFactory.union(
									new Select()
									.addProjection(cityObject.getColumn(MappingConstants.ID))
									.addJoin(JoinFactory.inner(cityObject, MappingConstants.ID, ComparisonName.EQUAL_TO, groupToCityObject.getColumn("cityobject_id")))
									.addSelection(ComparisonFactory.in(groupToCityObject.getColumn("cityobjectgroup_id"), idLiteralList)),
									new Select()
									.addProjection(cityObjectGroup.getColumn("parent_cityobject_id"))
									.addSelection(ComparisonFactory.in(cityObjectGroup.getColumn(MappingConstants.ID), idLiteralList))
									)),
					ComparisonFactory.notIn((Column)select.getProjection().get(0), notInQuery)));

			// add hits counter
			if (calculateNumberMatched) {
				Table table = new Table(select);
				select = new Select()
						.addProjection(table.getColumn(MappingConstants.ID))
						.addProjection(table.getColumn(MappingConstants.OBJECTCLASS_ID))
						.addProjection(table.getColumn(MappingConstants.GMLID))
						.addProjection(new Function("count(1) over", "hits"));
			}

			// issue query
			PreparedStatement stmt = null;
			ResultSet rs = null;

			try {
				stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
				rs = stmt.executeQuery();

				if (rs.next()) {
					if (calculateNumberMatched) {
						hits = rs.getInt("hits");
						log.info("Found " + hits + " additional group member(s).");
						eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, hits + cityObjectGroups.size(), this));
					}
					
					do {
						long id = rs.getLong("id");
						int objectClassId = rs.getInt("objectclass_id");
						
						AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(objectClassId);
						if (objectType == null) {
							log.error("Failed to map object class id '" + objectClassId + "' to an object type (ID: " + id + ").");
							continue;
						}

						if (objectType.isEqualToOrSubTypeOf(cityObjectGroupType)) {						
							if (!cityObjectGroups.containsKey(id)) {
								String gmlId = rs.getString("gmlid");
								cityObjectGroups.put(id, objectType);

								// register group in gml:id cache
								if (gmlId != null && gmlId.length() > 0)
									featureGmlIdCache.put(gmlId, id, -1, false, null, objectClassId);
							}

							continue;
						}

						// set initial context...
						DBSplittingResult splitter = new DBSplittingResult(id, objectType);
						dbWorkerPool.addWork(splitter);
					} while (rs.next() && shouldRun);
				}
			} finally {
				if (rs != null)
					rs.close();

				if (stmt != null)
					stmt.close();
			}

			// wait for jobs to be done...
			try {
				dbWorkerPool.join();
			} catch (InterruptedException e) {
				//
			}
		}

		// second step: export groups themselves
		// we assume that all group members have been exported and their gml:ids
		// are registered in the gml:id cache
		if (calculateNumberMatched && hits == 0)
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, cityObjectGroups.size(), this));

		for (Iterator<Entry<Long, AbstractObjectType<?>>> iter = cityObjectGroups.entrySet().iterator(); shouldRun && iter.hasNext(); ) {
			Entry<Long, AbstractObjectType<?>> entry = iter.next();
			DBSplittingResult splitter = new DBSplittingResult(entry.getKey(), entry.getValue());
			dbWorkerPool.addWork(splitter);
		}
	}

	private void queryGlobalAppearance() throws SQLException, QueryBuildException {
		if (!shouldRun)
			return;

		log.info("Processing global appearance features.");
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.globalApp.msg"), this));

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			CacheTable globalAppTempTable = cacheTableManager.getCacheTable(CacheTableModelEnum.ID_LIST);
			globalAppTempTable.createIndexes();

			Table appearance = new Table("appearance", schema);
			Table appearToSurfaceData = new Table("appear_to_surface_data", schema);
			Table surfaceData = new Table("surface_data", schema);
			Table textureParam = new Table("textureparam", schema);
			Table tempTable = new Table(globalAppTempTable.getTableName());

			Select select = new Select()
					.addProjection(appearance.getColumn(MappingConstants.ID)).setDistinct(true)
					.addJoin(JoinFactory.inner(appearToSurfaceData, "appearance_id", ComparisonName.EQUAL_TO, appearance.getColumn(MappingConstants.ID)))
					.addJoin(JoinFactory.inner(surfaceData, MappingConstants.ID, ComparisonName.EQUAL_TO, appearToSurfaceData.getColumn("surface_data_id")))
					.addJoin(JoinFactory.inner(textureParam, "surface_data_id", ComparisonName.EQUAL_TO, surfaceData.getColumn(MappingConstants.ID)))
					.addJoin(JoinFactory.inner(tempTable, MappingConstants.ID, ComparisonName.EQUAL_TO, textureParam.getColumn("surface_geometry_id")))
					.addSelection(ComparisonFactory.isNull(appearance.getColumn("cityobject_id")));

			// add appearance theme filter
			if (query.isSetAppearanceFilter()) {
				PredicateToken predicate = new AppearanceFilterBuilder(databaseAdapter).buildAppearanceFilter(query.getAppearanceFilter(), appearance.getColumn("theme"));
				select.addSelection(predicate);
			}

			// add hits counter
			if (calculateNumberMatched) {
				Table table = new Table(select);
				select = new Select()
						.addProjection(table.getColumn(MappingConstants.ID))
						.addProjection(new Function("count(1) over", "hits"));
			}

			stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, globalAppTempTable.getConnection());
			rs = stmt.executeQuery();

			if (rs.next()) {
				if (calculateNumberMatched) {
					long hits = rs.getLong("hits");
					log.info("Found " + hits + " global appearance feature(s).");
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int)hits, this));
				}

				FeatureType appearanceType = schemaMapping.getFeatureType("Appearance", AppearanceModule.v2_0_0.getNamespaceURI());

				do {
					long appearanceId = rs.getLong(1);

					// send appearance to export workers
					DBSplittingResult splitter = new DBSplittingResult(appearanceId, appearanceType);
					dbWorkerPool.addWork(splitter);
				} while (rs.next() && shouldRun);
			}

		} finally {
			if (rs != null)
				rs.close();

			if (stmt != null)
				stmt.close();
		}
	}

}
