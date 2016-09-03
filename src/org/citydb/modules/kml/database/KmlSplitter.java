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
package org.citydb.modules.kml.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.api.geometry.GeometryType;
import org.citydb.api.geometry.Position;
import org.citydb.config.Config;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Database.PredefinedSrsName;
import org.citydb.config.project.exporter.ExportFilterConfig;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.kml.util.CityObject4JSON;
import org.citydb.util.Util;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

public class KmlSplitter {
	private final HashSet<CityGMLClass> CURRENTLY_ALLOWED_CITY_OBJECT_TYPES = new HashSet<CityGMLClass>();
	private final WorkerPool<KmlSplittingResult> dbWorkerPool;
	private final DisplayForm displayForm;
	private final ExportFilter exportFilter;
	private ExportFilterConfig filterConfig;
	private volatile boolean shouldRun = true;

	private AbstractDatabaseAdapter databaseAdapter;
	private Connection connection;
	private DatabaseSrs dbSrs;

	public KmlSplitter(DatabaseConnectionPool dbConnectionPool, 
			WorkerPool<KmlSplittingResult> dbWorkerPool,
			ExportFilter exportFilter, 
			DisplayForm displayForm,
			Config config) throws SQLException {
		this.dbWorkerPool = dbWorkerPool;
		this.exportFilter = exportFilter;
		this.displayForm = displayForm;
		this.filterConfig = config.getProject().getKmlExporter().getFilter();
		
		CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.clear();
		boolean allowAllTypes = !filterConfig.isSetComplexFilter();
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetBuilding()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.BUILDING);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetWaterBody()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_BODY);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetLandUse()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.LAND_USE);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.PLANT_COVER);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetTransportation()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRANSPORTATION_COMPLEX);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRACK);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RAILWAY);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.ROAD);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SQUARE);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetReliefFeature()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RELIEF_FEATURE);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.GENERIC_CITY_OBJECT);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetCityFurniture()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_FURNITURE);
		}
		
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetCityObjectGroup()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_OBJECT_GROUP);
		}
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetBridge()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.BRIDGE);
		}
		if (allowAllTypes || filterConfig.getComplexFilter().getFeatureClass().isSetTunnel()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TUNNEL);
		}
		
		databaseAdapter = dbConnectionPool.getActiveDatabaseAdapter();
		connection = dbConnectionPool.getConnection();
		dbSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();

		// try and change workspace for connection if needed
		if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			Database database = config.getProject().getDatabase();
			dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(connection, 
					database.getWorkspaces().getKmlExportWorkspace());
		}

	}

	private void queryObjects() throws SQLException {

		if (filterConfig.isSetSimpleFilter()) {
			for (String gmlId: filterConfig.getSimpleFilter().getGmlIdFilter().getGmlIds()) {
				if (!shouldRun) break;

				ResultSet rs = null;
				PreparedStatement query = null;
				try {
					query = connection.prepareStatement(Queries.GET_ID_AND_OBJECTCLASS_FROM_GMLID);
					query.setString(1, gmlId);
					rs = query.executeQuery();					
					if (rs.next()) {
						long id = rs.getLong("id");
						CityGMLClass cityObjectType = Util.classId2cityObject(rs.getInt("objectclass_id"));
						addWorkToQueue(id, gmlId, cityObjectType, null, 0, 0, false);
					}
				}
				catch (SQLException sqlEx) {
					throw sqlEx;
				}
				finally {
					if (rs != null) {
						try { rs.close(); }	catch (SQLException sqlEx) { throw sqlEx; }
						rs = null;
					}

					if (query != null) {
						try { query.close(); } catch (SQLException sqlEx) { throw sqlEx; }
						query = null;
					}
				}
			}
		}
		else if (filterConfig.isSetComplexFilter() &&
				filterConfig.getComplexFilter().getTiledBoundingBox().isSet()) {

			ResultSet rs = null;
			PreparedStatement spatialQuery = null;
			try {
				spatialQuery = connection.prepareStatement(Queries.GET_IDS(databaseAdapter.getDatabaseType())); 				
				spatialQuery.setObject(1, databaseAdapter.getGeometryConverter().getDatabaseObject(exportFilter.getBoundingBoxFilter().getFilterStateForKml(), connection));
				rs = spatialQuery.executeQuery();

				int objectCount = 0;

				while (rs.next() && shouldRun) {
					long id = rs.getLong("id");
					String gmlId = rs.getString("gmlId");
					CityGMLClass cityObjectType = Util.classId2cityObject(rs.getInt("objectclass_id"));
					
					GeometryObject envelope = null;
					Object geomObj = rs.getObject("envelope");
					if (!rs.wasNull() && geomObj != null)
						envelope = databaseAdapter.getGeometryConverter().getEnvelope(geomObj);
					
					addWorkToQueue(id, gmlId, cityObjectType, envelope,
							exportFilter.getBoundingBoxFilter().getTileRow(),
							exportFilter.getBoundingBoxFilter().getTileColumn(), false);

					objectCount++;
				}
				Logger.getInstance().debug(objectCount + " candidate objects found for Tile_" + exportFilter.getBoundingBoxFilter().getTileRow()
						+ "_" + exportFilter.getBoundingBoxFilter().getTileColumn() + ".");
			}
			catch (SQLException sqlEx) {
				throw sqlEx;
			}
			finally {
				if (rs != null) {
					try { rs.close(); }	catch (SQLException sqlEx) { throw sqlEx; }
					rs = null;
				}

				if (spatialQuery != null) {
					try { spatialQuery.close(); } catch (SQLException sqlEx) { throw sqlEx; }
					spatialQuery = null;
				}
			}
		}
	}

	public void startQuery() throws SQLException {
		try {
			queryObjects();
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				}
				catch (SQLException sqlEx) {}

				connection = null;
			}
		}
	}

	public void shutdown() {
		shouldRun = false;
	}

	private void addWorkToQueue(long id, String gmlId, CityGMLClass cityObjectType, GeometryObject envelope, int row, int column, boolean isCityObjectGroupMember) throws SQLException {
		
		// In order to avoid the duplication of export, cityobjectgroup members
		// should not be exported if it belongs to the feature types (except CityObjectGroup) 
		// that have been already selected in the featureClass-Filter (ComplexFilter)
		if (isCityObjectGroupMember && CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.contains(cityObjectType)
				&& !cityObjectType.equals(CityGMLClass.CITY_OBJECT_GROUP) && filterConfig.isSetComplexFilter())
			return;

		// 1) If only the feature type CityObjectGroup is checked, then all city
		// object groups and all their group members (independent of their
		// feature type) are exported.
		// 2) If further feature types are selected in addition to
		// CityObjectGroup, then only group members matching those feature types
		// are exported. Of course, all features that match the type selection
		// but are not group members are also exported.
		if (CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.contains(cityObjectType) || (isCityObjectGroupMember && CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.size() == 1)) {

			// check whether center point of the feature's envelope is within the tile extent
			if (envelope != null && envelope.getGeometryType() == GeometryType.ENVELOPE) {
				double coordinates[] = envelope.getCoordinates(0);
				
				Envelope tmp = new Envelope();
				tmp.setLowerCorner(new Point(coordinates[0], coordinates[1], 0));
				tmp.setUpperCorner(new Point(coordinates[3], coordinates[4], 0));
				
				if (exportFilter.getBoundingBoxFilter().filter(tmp))
					return;
			}
						
			// create json
			CityObject4JSON cityObject4Json = new CityObject4JSON(gmlId);
			cityObject4Json.setTileRow(row);
			cityObject4Json.setTileColumn(column);
			cityObject4Json.setEnvelope(getEnvelopeInWGS84(envelope));
			
			// put on work queue
			KmlSplittingResult splitter = new KmlSplittingResult(id, gmlId, cityObjectType, cityObject4Json, displayForm);
			dbWorkerPool.addWork(splitter);

			if (splitter.isCityObjectGroup()) {
				ResultSet rs = null;
				PreparedStatement query = null;
				try {
					query = connection.prepareStatement(Queries.CITYOBJECTGROUP_MEMBERS);

					// set group's id
					query.setLong(1, id);
					rs = query.executeQuery();

					while (rs.next() && shouldRun) {
						long _id = rs.getLong("id");
						String _gmlId = rs.getString("gmlId");
						CityGMLClass _cityObjectType = Util.classId2cityObject(rs.getInt("objectclass_id"));
						
						GeometryObject _envelope = null;
						Object geomObj = rs.getObject("envelope");
						if (!rs.wasNull() && geomObj != null)
							_envelope = databaseAdapter.getGeometryConverter().getEnvelope(geomObj);
						
						// Recursion in CityObjectGroup
						addWorkToQueue(_id,  _gmlId, _cityObjectType, _envelope, row, column, true);
					}
				}
				catch (SQLException sqlEx) {
					throw sqlEx;
				}
				finally {
					if (rs != null) {
						try { rs.close(); }	catch (SQLException sqlEx) { throw sqlEx; }
						rs = null;
					}

					if (query != null) {
						try { query.close(); } catch (SQLException sqlEx) { throw sqlEx; }
						query = null;
					}
				}
			}
		}
	}

	private double[] getEnvelopeInWGS84(GeometryObject envelope) throws SQLException {
		if (envelope == null)
			return null;
		
		double[] coordinates = envelope.getCoordinates(0);
		BoundingBox bbox = new BoundingBox(new Position(coordinates[0], coordinates[1]), new Position(coordinates[3], coordinates[4]));
		BoundingBox wgs84 = databaseAdapter.getUtil().transformBoundingBox(bbox, dbSrs, Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D));
		
		double[] result = new double[6];
		result[0] = wgs84.getLowerCorner().getX();
		result[1] = wgs84.getLowerCorner().getY();
		result[2] = 0;
		result[3] = wgs84.getUpperCorner().getX();
		result[4] = wgs84.getUpperCorner().getY();
		result[5] = 0;
		
		return result;
	}
	
}
