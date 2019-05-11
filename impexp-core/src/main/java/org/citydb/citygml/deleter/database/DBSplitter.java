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

import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.concurrent.WorkerPool;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.BuildProperties;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.projection.Function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBSplitter {
	private final Logger log = Logger.getInstance();

	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final Query query;
	private final EventDispatcher eventDispatcher;

	private final AbstractDatabaseAdapter databaseAdapter;
	private final Connection connection;
	private final SchemaMapping schemaMapping;
	private final SQLQueryBuilder builder;

	private volatile boolean shouldRun = true;
	private boolean calculateNumberMatched;
	private long elementCounter;

	public DBSplitter(SchemaMapping schemaMapping,
			WorkerPool<DBSplittingResult> dbWorkerPool, 
			Query query,
			EventDispatcher eventDispatcher) throws SQLException {
		
		this.schemaMapping = schemaMapping;
		this.dbWorkerPool = dbWorkerPool;
		this.query = query;
		this.eventDispatcher = eventDispatcher;
		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		connection = DatabaseConnectionPool.getInstance().getConnection();

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
			if (connection != null)
				connection.close();
		}
	}

	private void queryCityObject() throws SQLException, QueryBuildException {
		if (!shouldRun)
			return;

		if (query.getFeatureTypeFilter().isEmpty())
			return;

		// create query statement
		Select select = builder.buildQuery(query);
		select.unsetOrderBy();

		if (query.isSetCounterFilter())
			select.addOrderBy(new OrderByToken((Column) select.getProjection().get(0)));

		// add hits counter
		if (calculateNumberMatched) {
			Table table = new Table(select);
			select = new Select()
					.addProjection(table.getColumn(MappingConstants.ID))
					.addProjection(table.getColumn(MappingConstants.OBJECTCLASS_ID))
					.addProjection(new Function("count(1) over", "hits"));
		}

		// issue query
		try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				if (calculateNumberMatched) {
					long hits = rs.getLong("hits");
					log.info("Found " + hits + " top-level feature(s) matching the request.");

					if (query.isSetCounterFilter()) {
						long maxCount = query.getCounterFilter().getUpperLimit();
						if (maxCount < hits) {
							log.info("Deleting " + maxCount + " top-level feature(s) due to counter settings.");
							hits = maxCount;
						}
					}
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) hits, this));
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

					// set initial context...
					DBSplittingResult splitter = new DBSplittingResult(id, objectType);
					dbWorkerPool.addWork(splitter);
				} while (rs.next() && shouldRun);
			} else
				log.info("No feature matches the request.");
		}
	}
	
}
