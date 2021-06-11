/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.vis.database;

import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.DatabaseConfig.PredefinedSrsName;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.visExporter.DisplayForm;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.log.Logger;
import org.citydb.vis.util.CityObject4JSON;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.sql.BuildProperties;
import org.citydb.query.builder.sql.SQLQueryBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.tiling.Tile;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBSplitter {
	private final WorkerPool<DBSplittingResult> dbWorkerPool;
	private final DisplayForm displayForm;
	private final Query query;
	private volatile boolean shouldRun = true;

	private AbstractDatabaseAdapter databaseAdapter;
	private Connection connection;
	private DatabaseSrs dbSrs;

	private String schema;
	private SchemaMapping schemaMapping;
	private SQLQueryBuilder builder;

	public DBSplitter(SchemaMapping schemaMapping,
                      WorkerPool<DBSplittingResult> dbWorkerPool,
                      Query query,
                      DisplayForm displayForm,
                      Config config) throws SQLException {
		this.dbWorkerPool = dbWorkerPool;
		this.schemaMapping = schemaMapping;
		this.query = query;
		this.displayForm = displayForm;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		connection = DatabaseConnectionPool.getInstance().getConnection();
		connection.setAutoCommit(false);
		dbSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();		
		schema = databaseAdapter.getConnectionDetails().getSchema();

		BuildProperties buildProperties = BuildProperties.defaults()
				.addProjectionColumn(MappingConstants.GMLID);

		builder = new SQLQueryBuilder(
				schemaMapping,
				databaseAdapter,
				buildProperties);
	}

	private void queryObjects() throws SQLException, QueryBuildException, FilterException {
		// tiling
		Tile activeTile = null;
		if (query.isSetTiling()) {
			activeTile = query.getTiling().getActiveTile();
			builder.getBuildProperties().addProjectionColumn(MappingConstants.ENVELOPE);
		}

		// create query statement
		Select select = builder.buildQuery(query);

		try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
			 ResultSet rs = stmt.executeQuery()) {
			int objectCount = 0;

			while (rs.next() && shouldRun) {
				long id = rs.getLong(MappingConstants.ID);
				String gmlId = rs.getString(MappingConstants.GMLID);
				int objectClassId = rs.getInt(MappingConstants.OBJECTCLASS_ID);

				GeometryObject envelope = null;
				if (query.isSetTiling()) {
					Object geomObj = rs.getObject(MappingConstants.ENVELOPE);
					if (!rs.wasNull() && geomObj != null)
						envelope = databaseAdapter.getGeometryConverter().getEnvelope(geomObj);
				}

				addWorkToQueue(id, gmlId, objectClassId, envelope, activeTile, false);
				objectCount++;
			}

			if (query.isSetTiling())
				Logger.getInstance().debug(objectCount + " candidate objects found for Tile_" + activeTile.getRow() + "_" + activeTile.getColumn() + ".");
		}
	}

	public void startQuery() throws SQLException, QueryBuildException, FilterException {
		try {
			queryObjects();
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	public void shutdown() {
		shouldRun = false;
	}

	private void addWorkToQueue(long id, String gmlId, int objectClassId, GeometryObject envelope, Tile activeTile, boolean isCityObjectGroupMember) throws SQLException, FilterException {
		FeatureType featureType = schemaMapping.getFeatureType(objectClassId);

		// In order to avoid the duplication of export, cityobjectgroup members
		// should not be exported if it belongs to the feature types (except CityObjectGroup) 
		// that have been already selected in the featureClass-Filter (ComplexFilter)
		if (isCityObjectGroupMember 
				&& query.getFeatureTypeFilter().containsFeatureType(featureType)
				&& Util.getCityGMLClass(objectClassId) != CityGMLClass.CITY_OBJECT_GROUP)
			return;

		// 1) If only the feature type CityObjectGroup is checked, then all city
		// object groups and all their group members (independent of their
		// feature type) are exported.
		// 2) If further feature types are selected in addition to
		// CityObjectGroup, then only group members matching those feature types
		// are exported. Of course, all features that match the type selection
		// but are not group members are also exported.
		if (query.getFeatureTypeFilter().containsFeatureType(featureType)
				|| (isCityObjectGroupMember && query.getFeatureTypeFilter().size() == 1)) {

			// check whether center point of the feature's envelope is within the tile extent
			if (envelope != null && envelope.getGeometryType() == GeometryType.ENVELOPE) {
				double coordinates[] = envelope.getCoordinates(0);
				if (!activeTile.isOnTile(new org.citydb.config.geometry.Point(
						(coordinates[0] + coordinates[3]) / 2.0,
						(coordinates[1] + coordinates[4]) / 2.0,
						databaseAdapter.getConnectionMetaData().getReferenceSystem()), 
						databaseAdapter))
					return;
			}

			// create json
			CityObject4JSON cityObject4Json = new CityObject4JSON(gmlId);
			cityObject4Json.setTileRow(activeTile != null ? activeTile.getRow() : 0);
			cityObject4Json.setTileColumn(activeTile != null ? activeTile.getColumn() : 0);
			cityObject4Json.setEnvelope(getEnvelopeInWGS84(envelope));

			// put on work queue
			DBSplittingResult splitter = new DBSplittingResult(id, gmlId, objectClassId, cityObject4Json, displayForm);
			dbWorkerPool.addWork(splitter);

			if (splitter.getCityGMLClass() == CityGMLClass.CITY_OBJECT_GROUP) {
				Table cityObject = new Table("cityobject", schema);
				Table groupToCityObject = new Table("group_to_cityobject", schema);
				PlaceHolder<Long> groupId = new PlaceHolder<>(id);

				Select select = new Select()
						.addProjection(cityObject.getColumn(MappingConstants.ID))
						.addProjection(cityObject.getColumn(MappingConstants.GMLID))
						.addProjection(cityObject.getColumn(MappingConstants.OBJECTCLASS_ID))
						.addProjection(cityObject.getColumn(MappingConstants.ENVELOPE))
						.addSelection(ComparisonFactory.in(
						cityObject.getColumn(MappingConstants.ID),
						new Select()
						.addProjection(cityObject.getColumn(MappingConstants.ID))
						.addJoin(JoinFactory.inner(cityObject, MappingConstants.ID, ComparisonName.EQUAL_TO, groupToCityObject.getColumn("cityobject_id")))
						.addSelection(ComparisonFactory.equalTo(groupToCityObject.getColumn("cityobjectgroup_id"), groupId))
						));

				try (PreparedStatement stmt = databaseAdapter.getSQLAdapter().prepareStatement(select, connection);
					 ResultSet rs = stmt.executeQuery()) {
					while (rs.next() && shouldRun) {
						long _id = rs.getLong(MappingConstants.ID);
						String _gmlId = rs.getString(MappingConstants.GMLID);
						int _objectClassId = rs.getInt(MappingConstants.OBJECTCLASS_ID);

						GeometryObject _envelope = null;
						Object geomObj = rs.getObject(MappingConstants.ENVELOPE);
						if (!rs.wasNull() && geomObj != null)
							_envelope = databaseAdapter.getGeometryConverter().getEnvelope(geomObj);

						// Recursion in CityObjectGroup
						addWorkToQueue(_id,  _gmlId, _objectClassId, _envelope, activeTile, true);
					}
				}
			}
		}
	}

	private BoundingBox getEnvelopeInWGS84(GeometryObject envelope) throws SQLException {
		if (envelope == null)
			return null;

		double[] coordinates = envelope.getCoordinates(0);
		BoundingBox bbox = new BoundingBox(new Position(coordinates[0], coordinates[1]), new Position(coordinates[3], coordinates[4]));
		return databaseAdapter.getUtil().transform2D(bbox, dbSrs, DatabaseConfig.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D));
	}

}
