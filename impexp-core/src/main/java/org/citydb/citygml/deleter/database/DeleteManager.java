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
import org.citydb.citygml.deleter.util.DeleteLogger;
import org.citydb.citygml.deleter.util.InternalConfig;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.deleter.DeleteListIdType;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.ConnectionManager;
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
import org.citydb.sqlbuilder.expression.StringLiteral;
import org.citydb.sqlbuilder.expression.TimestampLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.operator.comparison.InOperator;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.sqlbuilder.update.Update;
import org.citydb.sqlbuilder.update.UpdateToken;
import org.citygml4j.model.module.citygml.CoreModule;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class DeleteManager {
	private final Logger log = Logger.getInstance();

	private final ConnectionManager connectionManager;
	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final Query query;
	private final Config config;
	private final CacheTable cacheTable;
	private final DeleteLogger deleteLogger;
	private final InternalConfig internalConfig;
	private final EventDispatcher eventDispatcher;

	private final AbstractDatabaseAdapter databaseAdapter;
	private final Connection connection;
	private final SchemaMapping schemaMapping;
	private final SQLQueryBuilder builder;
	private final boolean preview;

	private volatile boolean shouldRun = true;
	private boolean calculateNumberMatched;
	private PreparedStatement interruptibleStmt;

	public DeleteManager(
			ConnectionManager connectionManager,
			SchemaMapping schemaMapping,
			WorkerPool<DBSplittingResult> dbWorkerPool,
			Query query,
			CacheTable cacheTable,
			DeleteLogger deleteLogger,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher,
			boolean preview) throws SQLException {
		this.connectionManager = connectionManager;
		this.schemaMapping = schemaMapping;
		this.dbWorkerPool = dbWorkerPool;
		this.query = query;
		this.cacheTable = cacheTable;
		this.deleteLogger = deleteLogger;
		this.internalConfig = internalConfig;
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
				BuildProperties.defaults().addProjectionColumn(MappingConstants.GMLID));
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

		if (interruptibleStmt != null) {
			try {
				interruptibleStmt.cancel();
			} catch (SQLException e) {
				//
			}
		}
	}

	public void deleteObjects() throws SQLException, IOException, QueryBuildException {
		try {
			process();
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

	private void process() throws SQLException, IOException, QueryBuildException {
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

		// create select statement
		Select select = builder.buildQuery(query);

		// in case a delete list is used, join the temporary table
		if (config.getDeleteConfig().isSetDeleteList() && cacheTable != null) {
			log.debug("Creating indexes on temporary delete list table...");
			cacheTable.createIndexes();
			joinDeleteList(select);
		}

		// get affected city objects
		Map<Integer, Long> counter = null;
		long hits = 0;
		if (calculateNumberMatched || preview || config.getDeleteConfig().getMode() == DeleteMode.TERMINATE) {
			log.debug("Calculating the number of matching top-level features...");
			counter = getNumberMatched(select);
			hits = counter.values().stream().mapToLong(Long::longValue).sum();

			if (hits > 0) {
				log.info("Found " + hits + " top-level feature(s) matching the request.");
			} else {
				log.info("No top-level feature matches the query expression.");
				return;
			}
		}

		if (preview) {
			eventDispatcher.triggerEvent(new ObjectCounterEvent(counter, this));
		} else if (config.getDeleteConfig().getMode() == DeleteMode.TERMINATE) {
			doTerminate(select);
			eventDispatcher.triggerEvent(new ObjectCounterEvent(counter, this));
		} else {
			doDelete(select, hits);
		}
	}

	private void doDelete(Select select, long hits) throws SQLException {
		try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) hits, this));
				do {
					long id = rs.getLong("id");
					int objectClassId = rs.getInt("objectclass_id");
					String gmlId = rs.getString("gmlid");

					AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(objectClassId);
					if (objectType == null) {
						log.error("Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + id + ").");
						continue;
					}

					DBSplittingResult splitter = new DBSplittingResult(id, objectType, gmlId);
					dbWorkerPool.addWork(splitter);
				} while (rs.next() && shouldRun);
			} else {
				log.info("No top-level feature matches the query expression.");
			}
		}
	}

	private void doTerminate(Select select) throws SQLException, IOException {
		if (deleteLogger != null) {
			try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
				 ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					long id = rs.getLong("id");
					int objectClassId = rs.getInt("objectclass_id");
					String gmlId = rs.getString("gmlid");

					AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(objectClassId);
					if (objectType == null) {
						throw new SQLException("Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + id + ").");
					}

					deleteLogger.write(objectType.getPath(), id, gmlId);
				}
			} catch (IOException e) {
				throw new IOException("A fatal error occurred while updating the delete log.", e);
			}
		}

		Table table = new Table(MappingConstants.CITYOBJECT, databaseAdapter.getConnectionDetails().getSchema());
		TimestampLiteral now = new TimestampLiteral(Timestamp.from(Instant.now()));
		TimestampLiteral terminationDate = internalConfig.getTerminationDate() != null ?
				new TimestampLiteral(GregorianCalendar.from(internalConfig.getTerminationDate().toZonedDateTime())) :
				now;

		StringLiteral updatingPerson = internalConfig.getUpdatingPersonMode() == UpdatingPersonMode.USER ?
				new StringLiteral(internalConfig.getUpdatingPerson()) :
				new StringLiteral(databaseAdapter.getConnectionDetails().getUser());

		Update update = new Update()
				.setTable(table)
				.addUpdateToken(new UpdateToken(table.getColumn(MappingConstants.TERMINATION_DATE), terminationDate))
				.addUpdateToken(new UpdateToken(table.getColumn(MappingConstants.LAST_MODIFICATION_DATE), now))
				.addUpdateToken(new UpdateToken(table.getColumn(MappingConstants.UPDATING_PERSON), updatingPerson));

		if (internalConfig.getReasonForUpdate() != null) {
			update.addUpdateToken(new UpdateToken(table.getColumn(MappingConstants.REASON_FOR_UPDATE),
					new StringLiteral(internalConfig.getReasonForUpdate())));
		}

		if (internalConfig.getLineage() != null) {
			update.addUpdateToken(new UpdateToken(table.getColumn(MappingConstants.LINEAGE),
					new StringLiteral(internalConfig.getLineage())));
		}

		select = new Select(select)
				.removeProjectionIf(t -> !(t instanceof Column) || !((Column) t).getName().equals(MappingConstants.ID));

		update.addSelection(new InOperator(table.getColumn(MappingConstants.ID), select));

		try {
			interruptibleStmt = databaseAdapter.getSQLAdapter().prepareStatement(update, connectionManager.getConnection());
			interruptibleStmt.executeUpdate();
		} catch (SQLException e) {
			if (shouldRun) {
				throw e;
			}
		} finally {
			if (interruptibleStmt != null) {
				interruptibleStmt.close();
			}
		}
	}

	private Map<Integer, Long> getNumberMatched(Select select) throws QueryBuildException, SQLException {
		Table table;
		if (query.isSetCounterFilter()) {
			table = new Table(select);
			select = new Select();
		} else {
			ProjectionToken token = select.getProjection().stream()
					.filter(v -> v instanceof Column && ((Column) v).getName().equals(MappingConstants.ID))
					.findFirst()
					.orElseThrow(() -> new QueryBuildException("Failed to build delete query due to unexpected SQL projection clause."));

			table = ((Column) token).getTable();
			select = new Select(select)
					.unsetOrderBy()
					.unsetProjection();
		}

		Column idColumn = table.getColumn(MappingConstants.ID);
		Column objectClassIdColumn = table.getColumn(MappingConstants.OBJECTCLASS_ID);
		select.addProjection(new Function("count", idColumn), objectClassIdColumn)
				.addGroupBy(objectClassIdColumn);

		Map<Integer, Long> counter = new HashMap<>();
		try {
			interruptibleStmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			try (ResultSet rs = interruptibleStmt.executeQuery()) {
				while (rs.next()) {
					long count = rs.getLong(1);
					int objectClassId = rs.getInt(2);
					counter.put(objectClassId, count);
				}
			}
		} catch (SQLException e) {
			counter.clear();
			if (shouldRun) {
				throw e;
			}
		} finally {
			if (interruptibleStmt != null) {
				interruptibleStmt.close();
			}
		}

		return counter;
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
