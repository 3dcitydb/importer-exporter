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
package de.tub.citydb.modules.kml.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.kml.controller.KmlExporter;
import de.tub.citydb.modules.kml.util.CityObject4JSON;
import de.tub.citydb.util.Util;

public class KmlSplitter {

	private static HashSet<CityGMLClass> CURRENTLY_ALLOWED_CITY_OBJECT_TYPES = new HashSet<CityGMLClass>();
	
	private final WorkerPool<KmlSplittingResult> dbWorkerPool;
	private final DisplayForm displayForm;
	private final ExportFilter exportFilter;
//	private final Config config;
	private ExportFilterConfig filterConfig;
	private volatile boolean shouldRun = true;

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
//		this.config = config;

		this.filterConfig = config.getProject().getKmlExporter().getFilter();
		CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.clear();
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBuilding()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.BUILDING);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.PLANT_COVER);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.GENERIC_CITY_OBJECT);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityFurniture()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_FURNITURE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityObjectGroup()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.CITY_OBJECT_GROUP);
		}
			
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);
		dbSrs = dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem();

		// try and change workspace for connection if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(connection, 
										 database.getWorkspaces().getKmlExportWorkspace());

	}

	private void queryObjects() throws SQLException {

//		long startTime = System.currentTimeMillis();

		if (filterConfig.isSetSimpleFilter()) {
			for (String gmlId: filterConfig.getSimpleFilter().getGmlIdFilter().getGmlIds()) {
				if (!shouldRun) break;
				if (KmlExporter.getAlreadyExported().containsKey(gmlId)) continue;

				OracleResultSet rs = null;
				PreparedStatement query = null;
				try {
					query = connection.prepareStatement(Queries.GET_OBJECTCLASS);
					query.setString(1, gmlId);
					rs = (OracleResultSet)query.executeQuery();
					
					if (rs.next()) {
						CityGMLClass cityObjectType = Util.classId2cityObject(rs.getInt("class_id"));
						addWorkToQueue(gmlId, cityObjectType, 0, 0);
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
			
			BoundingBox tile = exportFilter.getBoundingBoxFilter().getFilterState();
			OracleResultSet rs = null;
			PreparedStatement spatialQuery = null;
			try {
				spatialQuery = connection.prepareStatement(Queries.GET_GMLIDS);
				
				int srid = dbSrs.getSrid();

				spatialQuery.setInt(1, srid);
				// coordinates for overlapbydisjoint
				spatialQuery.setDouble(2, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(3, tile.getUpperRightCorner().getY());
				spatialQuery.setDouble(4, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(5, tile.getLowerLeftCorner().getY());
				spatialQuery.setDouble(6, tile.getUpperRightCorner().getX());
				spatialQuery.setDouble(7, tile.getLowerLeftCorner().getY());

				spatialQuery.setInt(8, srid);
				// coordinates for inside+coveredby
				spatialQuery.setDouble(9, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(10, tile.getLowerLeftCorner().getY());
				spatialQuery.setDouble(11, tile.getUpperRightCorner().getX());
				spatialQuery.setDouble(12, tile.getUpperRightCorner().getY());

				spatialQuery.setInt(13, srid);
				// coordinates for equals
				spatialQuery.setDouble(14, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(15, tile.getLowerLeftCorner().getY());
				spatialQuery.setDouble(16, tile.getUpperRightCorner().getX());
				spatialQuery.setDouble(17, tile.getUpperRightCorner().getY());

				rs = (OracleResultSet)spatialQuery.executeQuery();
/*
				String absolutePath = config.getInternal().getExportFileName().trim();
				String filename = absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1,
														 absolutePath.lastIndexOf("."));
				String tileName = filename + "_Tile_" 
										   + exportFilter.getBoundingBoxFilter().getTileColumn()
										   + "_" 
										   + exportFilter.getBoundingBoxFilter().getTileRow();

				Logger.getInstance().info("Spatial query for " + tileName + " resolved in " +
										  (System.currentTimeMillis() - startTime) + " millis.");
*/
				int objectCount = 0;

				while (rs.next() && shouldRun) {
					String gmlId = rs.getString("gmlId");
					CityGMLClass cityObjectType = Util.classId2cityObject(rs.getInt("class_id"));
					addWorkToQueue(gmlId, cityObjectType, 
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

			if (shouldRun) {
				try {
					dbWorkerPool.join();
				}
				catch (InterruptedException e) {}
			}

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

	private void addWorkToQueue(String gmlId, CityGMLClass cityObjectType, int row, int column) throws SQLException {

		if (CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.contains(cityObjectType) &&
			!KmlExporter.getAlreadyExported().containsKey(gmlId)) {

			CityObject4JSON cityObject4Json = new CityObject4JSON();
			cityObject4Json.setTileRow(row);
			cityObject4Json.setTileColumn(column);
			double[] ordinatesArray = getEnvelopeInWGS84(gmlId);
			cityObject4Json.setEnvelope(ordinatesArray);

			KmlSplittingResult splitter = new KmlSplittingResult(gmlId, cityObjectType, displayForm);
			dbWorkerPool.addWork(splitter);
			KmlExporter.getAlreadyExported().put(gmlId, cityObject4Json);

			if (splitter.isCityObjectGroup() &&
					CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.size() > 1) { // not only groups must be exported
				OracleResultSet rs = null;
				PreparedStatement query = null;
				try {
					if (filterConfig.isSetComplexFilter() &&
						filterConfig.getComplexFilter().getTiledBoundingBox().isSet()) {

						query = connection.prepareStatement(Queries.CITYOBJECTGROUP_MEMBERS_IN_BBOX);
						BoundingBox tile = exportFilter.getBoundingBoxFilter().getFilterState();
						int srid = dbSrs.getSrid();

						// group's gmlId
						query.setString(1, gmlId);

						query.setInt(2, srid);
						// coordinates for overlapbydisjoint
						query.setDouble(3, tile.getLowerLeftCorner().getX());
						query.setDouble(4, tile.getUpperRightCorner().getY());
						query.setDouble(5, tile.getLowerLeftCorner().getX());
						query.setDouble(6, tile.getLowerLeftCorner().getY());
						query.setDouble(7, tile.getUpperRightCorner().getX());
						query.setDouble(8, tile.getLowerLeftCorner().getY());

						// group's gmlId
						query.setString(9, gmlId);

						query.setInt(10, srid);
						// coordinates for inside+coveredby
						query.setDouble(11, tile.getLowerLeftCorner().getX());
						query.setDouble(12, tile.getLowerLeftCorner().getY());
						query.setDouble(13, tile.getUpperRightCorner().getX());
						query.setDouble(14, tile.getUpperRightCorner().getY());

						// group's gmlId
						query.setString(15, gmlId);

						query.setInt(16, srid);
						// coordinates for equals
						query.setDouble(17, tile.getLowerLeftCorner().getX());
						query.setDouble(18, tile.getLowerLeftCorner().getY());
						query.setDouble(19, tile.getUpperRightCorner().getX());
						query.setDouble(20, tile.getUpperRightCorner().getY());
					}
					else {
						query = connection.prepareStatement(Queries.CITYOBJECTGROUP_MEMBERS);
						query.setString(1, gmlId);
					}
					rs = (OracleResultSet)query.executeQuery();
					
					while (rs.next() && shouldRun) {
						addWorkToQueue(rs.getString("gmlId"), // recursion for recursive groups
									   Util.classId2cityObject(rs.getInt("class_id")), 
									   row,
									   column);
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
	
	private double[] getEnvelopeInWGS84(String gmlId) {
		double[] ordinatesArray = null;
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;

		try {
			psQuery = dbSrs.is3D() ? 
					  connection.prepareStatement(Queries.GET_ENVELOPE_IN_WGS84_3D_FROM_GML_ID):
					  connection.prepareStatement(Queries.GET_ENVELOPE_IN_WGS84_FROM_GML_ID);
						  
			psQuery.setString(1, gmlId);

			rs = (OracleResultSet)psQuery.executeQuery();
			if (rs.next()) {
				STRUCT struct = (STRUCT)rs.getObject(1); 
				if (!rs.wasNull() && struct != null) {
					JGeometry geom = JGeometry.load(struct);
					ordinatesArray = geom.getOrdinatesArray();
				}
			}
		} 
		catch (SQLException sqlEx) {}
		finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {}

				rs = null;
			}

			if (psQuery != null) {
				try {
					psQuery.close();
				} catch (SQLException sqlEx) {}

				psQuery = null;
			}
		}
		return ordinatesArray;
	}

}
