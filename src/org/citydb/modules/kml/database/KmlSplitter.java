/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package org.citydb.modules.kml.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.BoundingBoxCorner;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.api.geometry.GeometryObject.GeometryType;
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
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBuilding()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.BUILDING);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetWaterBody()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_BODY);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetLandUse()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.LAND_USE);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.PLANT_COVER);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetTransportation()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRANSPORTATION_COMPLEX);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRACK);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RAILWAY);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.ROAD);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SQUARE);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetReliefFeature()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RELIEF_FEATURE);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.GENERIC_CITY_OBJECT);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityFurniture()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_FURNITURE);
		}
		
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityObjectGroup()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_OBJECT_GROUP);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBridge()
				&& config.getProject().getKmlExporter().getLodToExportFrom() > 0) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.BRIDGE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetTunnel()
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
						addWorkToQueue(id, gmlId, cityObjectType, null, 0, 0);
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
				spatialQuery.setObject(1, databaseAdapter.getGeometryConverter().getDatabaseObject(GeometryObject.createEnvelope(exportFilter.getBoundingBoxFilter().getFilterState()), connection));
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
							exportFilter.getBoundingBoxFilter().getTileColumn());

					objectCount++;
				}
				Logger.getInstance().debug("Tile_" + exportFilter.getBoundingBoxFilter().getTileRow()
						+ "_" + exportFilter.getBoundingBoxFilter().getTileColumn()
						+ " contained " + objectCount + " objects.");
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

	private void addWorkToQueue(long id, String gmlId, CityGMLClass cityObjectType, GeometryObject envelope, int row, int column) throws SQLException {

		if (CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.contains(cityObjectType)) {

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

			if (splitter.isCityObjectGroup() &&  (filterConfig.isSetSimpleFilter() || CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.size() > 1)) {
				ResultSet rs = null;
				PreparedStatement query = null;
				try {
					if (filterConfig.isSetComplexFilter() && filterConfig.getComplexFilter().getTiledBoundingBox().isSet()) {
						query = connection.prepareStatement(Queries.CITYOBJECTGROUP_MEMBERS_IN_BBOX(databaseAdapter.getDatabaseType()));
						query.setObject(2, databaseAdapter.getGeometryConverter().getDatabaseObject(GeometryObject.createEnvelope(exportFilter.getBoundingBoxFilter().getFilterState()), connection));
					} else
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
						
						addWorkToQueue(_id,  _gmlId, _cityObjectType, _envelope, row, column);
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
		BoundingBox bbox = new BoundingBox(new BoundingBoxCorner(coordinates[0], coordinates[1]), new BoundingBoxCorner(coordinates[3], coordinates[4]));
		BoundingBox wgs84 = databaseAdapter.getUtil().transformBoundingBox(bbox, dbSrs, Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D));
		
		double[] result = new double[6];
		result[0] = wgs84.getLowerLeftCorner().getX();
		result[1] = wgs84.getLowerLeftCorner().getY();
		result[2] = 0;
		result[3] = wgs84.getUpperRightCorner().getX();
		result[4] = wgs84.getUpperRightCorner().getY();
		result[5] = 0;
		
		return result;
	}

}
