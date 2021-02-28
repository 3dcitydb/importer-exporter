/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.deleter.database;

import org.citydb.citygml.common.cache.CacheTable;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.deleter.DeleteListIdType;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.BuildProperties;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.comparison.ComparisonFactory;
import org.citydb.query.filter.selection.operator.comparison.NullOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.sqlbuilder.select.projection.WildCardColumn;
import org.citygml4j.model.module.citygml.CoreModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBSplitter {
	private final Logger log = Logger.getInstance();

	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final Query query;
	private final Config config;
	private final CacheTable cacheTable;
	private final EventDispatcher eventDispatcher;

	private final AbstractDatabaseAdapter databaseAdapter;
	private final Connection connection;
	private final SchemaMapping schemaMapping;
	private final SQLQueryBuilder builder;
	private final boolean preview;

	private volatile boolean shouldRun = true;
	private boolean calculateNumberMatched;

	public DBSplitter(
			SchemaMapping schemaMapping,
			WorkerPool<DBSplittingResult> dbWorkerPool,
			Query query,
			CacheTable cacheTable,
			Config config,
			EventDispatcher eventDispatcher,
			boolean preview) throws SQLException {
		this.schemaMapping = schemaMapping;
		this.dbWorkerPool = dbWorkerPool;
		this.query = query;
		this.cacheTable = cacheTable;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		this.preview = preview;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		connection = cacheTable != null ?
				cacheTable.getConnection() :
				DatabaseConnectionPool.getInstance().getConnection();

		connection.setAutoCommit(false);

		builder = new SQLQueryBuilder(
				schemaMapping, 
				databaseAdapter,
				BuildProperties.defaults());
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

	public void startQuery() throws SQLException, QueryBuildException {
		try {
			queryCityObject();
			if (shouldRun) {
				try {
					dbWorkerPool.join();
				} catch (InterruptedException e) {
					log.logStackTrace(e);
				}
			}
		} finally {
			// do not close cache table connection
			if (connection != null && cacheTable == null) {
				connection.close();
			}
		}
	}

	private void queryCityObject() throws SQLException, QueryBuildException {
		if (!shouldRun)
			return;

		if (!query.isSetFeatureTypeFilter() || query.getFeatureTypeFilter().isEmpty())
			return;

		// do not terminate city objects that have already been terminated
		if (config.getDeleteConfig().getMode() == DeleteMode.TERMINATE) {
			try {
				FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
				SchemaPath schemaPath = new SchemaPath(superType)
						.appendChild(superType.getProperty("terminationDate", CoreModule.v2_0_0.getNamespaceURI(), true));
				NullOperator isNull = ComparisonFactory.isNull(new ValueReference(schemaPath));

				if (query.isSetSelection()) {
					SelectionFilter selection = query.getSelection();
					selection.setPredicate(LogicalOperationFactory.AND(selection.getPredicate(), isNull));
				} else
					query.setSelection(new SelectionFilter(isNull));
			} catch (InvalidSchemaPathException | FilterException e) {
				throw new QueryBuildException("Failed to add is null test for termination date.", e);
			}
		}

		// create query statement
		Select select = builder.buildQuery(query);

		// in case a delete list is used, join the temporary table holding the ids from the list
		if (config.getDeleteConfig().isSetDeleteList() && cacheTable != null) {
			log.debug("Creating indexes on temporary delete list table...");
			cacheTable.createIndexes();

			joinDeleteList(select);
		}

		if (preview) {
			doPreview(select);
		} else {
			doDelete(query, select);
		}
	}

	private void doPreview(Select select) throws SQLException, QueryBuildException {
		ProjectionToken token = select.getProjection().stream()
				.filter(v -> v instanceof Column && ((Column) v).getName().equals(MappingConstants.ID))
				.findFirst()
				.orElseThrow(() -> new QueryBuildException("Failed to build delete query due to unexpected SQL projection clause."));

		Column objectClassIdColumn = ((Column) token).getTable().getColumn(MappingConstants.OBJECTCLASS_ID);
		select.unsetProjection()
				.unsetOrderBy()
				.addProjection(new Function("count", (Column) token), objectClassIdColumn)
				.addGroupBy(objectClassIdColumn);

		// issue query
		try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			 ResultSet rs = stmt.executeQuery()) {
			Map<Integer, Long> objectCounter = new HashMap<>();
			while (rs.next()) {
				long count = rs.getLong(1);
				int objectClassId = rs.getInt(2);
				objectCounter.put(objectClassId, count);
			}

			if (!objectCounter.isEmpty()) {
				eventDispatcher.triggerEvent(new ObjectCounterEvent(objectCounter, this));
			} else {
				log.info("No feature matches the request.");
			}
		}
	}

	private void doDelete(Query query, Select select) throws SQLException, QueryBuildException {
		// calculate hits
		long hits = 0;
		if (calculateNumberMatched) {
			log.debug("Calculating the number of matching top-level features...");
			hits = getNumberMatched(query);
		}

		// issue query
		try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				if (calculateNumberMatched) {
					log.info("Found " + hits + " top-level feature(s) matching the request.");

					if (query.isSetCounterFilter() && query.getCounterFilter().isSetCount()) {
						long count = query.getCounterFilter().getCount();
						long startIndex = query.getCounterFilter().isSetStartIndex() ? query.getCounterFilter().getStartIndex() : 0;
						long numberReturned = Math.min(Math.max(hits - startIndex, 0), count);
						if (numberReturned < hits) {
							log.info("Deleting at maximum " + numberReturned + " top-level feature(s) due to counter settings.");
							hits = count;
						}
					}

					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) hits, this));
				}

				do {
					long id = rs.getLong("id");
					int objectClassId = rs.getInt("objectclass_id");

					AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(objectClassId);
					if (objectType == null) {
						log.error("Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + id + ").");
						continue;
					}

					DBSplittingResult splitter = new DBSplittingResult(id, objectType);
					dbWorkerPool.addWork(splitter);
				} while (rs.next() && shouldRun);
			} else {
				log.info("No feature matches the request.");
			}
		}
	}

	private long getNumberMatched(Query query) throws QueryBuildException, SQLException {
		Query hitsQuery = new Query(query);
		hitsQuery.unsetCounterFilter();
		hitsQuery.unsetSorting();

		Select select = builder.buildQuery(hitsQuery)
				.removeProjectionIf(t -> !(t instanceof Column) || !((Column) t).getName().equals(MappingConstants.ID));

		if (config.getDeleteConfig().isSetDeleteList() && cacheTable != null) {
			joinDeleteList(select);
		}

		select = new Select().addProjection(new Function("count", new WildCardColumn(new Table(select), false)));
		try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			 ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getLong(1) : 0;
		}
	}

	private void joinDeleteList(Select select) throws QueryBuildException {
		ProjectionToken token = select.getProjection().stream()
				.filter(v -> v instanceof Column && ((Column) v).getName().equals(MappingConstants.ID))
				.findFirst()
				.orElseThrow(() -> new QueryBuildException("Failed to build delete query due to unexpected SQL projection clause."));

		String columnName = config.getDeleteConfig().getDeleteList().getIdType() == DeleteListIdType.DATABASE_ID ?
				MappingConstants.ID :
				MappingConstants.GMLID;

		Table cityObject = ((Column) token).getTable();
		Table table = new Table(cacheTable.getTableName(), builder.getBuildProperties().getAliasGenerator());
		select.addJoin(JoinFactory.inner(table, columnName, ComparisonName.EQUAL_TO, cityObject.getColumn(columnName)));
	}
}
