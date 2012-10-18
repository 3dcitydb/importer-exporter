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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.citygml4j.model.citygml.CityGMLClass;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
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
		if (filterConfig.getComplexFilter().getFeatureClass().isSetWaterBody()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_BODY);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_SURFACE);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_CLOSURE_SURFACE);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.WATER_GROUND_SURFACE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetLandUse()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.LAND_USE);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SOLITARY_VEGETATION_OBJECT);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.PLANT_COVER);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetTransportation()) {
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRAFFIC_AREA);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.AUXILIARY_TRAFFIC_AREA);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRANSPORTATION_COMPLEX);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.TRACK);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.RAILWAY);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.ROAD);
			CURRENTLY_ALLOWED_CITY_OBJECT_TYPES.add(CityGMLClass.SQUARE);
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
		/*Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(connection, 
										 database.getWorkspaces().getKmlExportWorkspace());*/

	}

	private void queryObjects() throws SQLException {

//		long startTime = System.currentTimeMillis();

		if (filterConfig.isSetSimpleFilter()) {
			for (String gmlId: filterConfig.getSimpleFilter().getGmlIdFilter().getGmlIds()) {
				if (!shouldRun) break;
				if (KmlExporter.getAlreadyExported().containsKey(gmlId)) continue;

//				OracleResultSet rs = null;
				ResultSet rs = null;
				PreparedStatement query = null;
				try {
					query = connection.prepareStatement(Queries.GET_OBJECTCLASS);
					query.setString(1, gmlId);
//					rs = (OracleResultSet)query.executeQuery();
					rs = query.executeQuery();
					
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
//			OracleResultSet rs = null;
			ResultSet rs = null;
			PreparedStatement spatialQuery = null;
			String lineGeom = null;
			String polyGeom = null;
						
			try {
				spatialQuery = connection.prepareStatement(Queries.GET_GMLIDS);

				int srid = dbSrs.getSrid();

				lineGeom = "SRID=" + srid + ";LINESTRING(" +
						tile.getLowerLeftCorner().getX() + " " + tile.getUpperRightCorner().getY() + "," +
						tile.getLowerLeftCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "," +
						tile.getUpperRightCorner().getX() + " " + tile.getLowerLeftCorner().getY() + ")";
				
				polyGeom = "SRID=" + srid + ";POLYGON((" +
						tile.getLowerLeftCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "," +
						tile.getLowerLeftCorner().getX() + " " + tile.getUpperRightCorner().getY() + "," +
						tile.getUpperRightCorner().getX() + " " + tile.getUpperRightCorner().getY() + "," +
						tile.getUpperRightCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "," +
						tile.getLowerLeftCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "))";
				
				spatialQuery.setString(1, lineGeom);
				spatialQuery.setString(2, polyGeom);
				spatialQuery.setString(3, polyGeom);
				spatialQuery.setString(4, polyGeom);
				spatialQuery.setString(5, polyGeom);
				spatialQuery.setString(6, polyGeom);
				
				rs = spatialQuery.executeQuery();
				
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
//				OracleResultSet rs = null;
				ResultSet rs = null;
				PreparedStatement query = null;
				String lineGeom = null;
				String polyGeom = null;
				
				try {
					if (filterConfig.isSetComplexFilter() &&
						filterConfig.getComplexFilter().getTiledBoundingBox().isSet()) {

						query = connection.prepareStatement(Queries.CITYOBJECTGROUP_MEMBERS_IN_BBOX);
						BoundingBox tile = exportFilter.getBoundingBoxFilter().getFilterState();
						int srid = dbSrs.getSrid();
						
						lineGeom = "SRID=" + srid + ";LINESTRING(" +
								tile.getLowerLeftCorner().getX() + " " + tile.getUpperRightCorner().getY() + "," +
								tile.getLowerLeftCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "," +
								tile.getUpperRightCorner().getX() + " " + tile.getLowerLeftCorner().getY() + ")'";
						
						polyGeom = "SRID=" + srid + ";POLYGON((" +
								tile.getLowerLeftCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "," +
								tile.getLowerLeftCorner().getX() + " " + tile.getUpperRightCorner().getY() + "," +
								tile.getUpperRightCorner().getX() + " " + tile.getUpperRightCorner().getY() + "," +
								tile.getUpperRightCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "," +
								tile.getLowerLeftCorner().getX() + " " + tile.getLowerLeftCorner().getY() + "))";
						
						query.setString(1, lineGeom);
						query.setString(2, polyGeom);
						query.setString(3, polyGeom);
						query.setString(4, polyGeom);
						query.setString(5, polyGeom);
						query.setString(6, polyGeom);
						
						rs = query.executeQuery();
					}
					else {
						query = connection.prepareStatement(Queries.CITYOBJECTGROUP_MEMBERS);
						query.setString(1, gmlId);
					}
//					rs = (OracleResultSet)query.executeQuery();
					rs = query.executeQuery();
					
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
//		OracleResultSet rs = null;
		ResultSet rs = null;

		try {
			psQuery = dbSrs.is3D() ? 
					  connection.prepareStatement(Queries.GET_ENVELOPE_IN_WGS84_3D_FROM_GML_ID):
					  connection.prepareStatement(Queries.GET_ENVELOPE_IN_WGS84_FROM_GML_ID);
						  
			psQuery.setString(1, gmlId);

//			rs = (OracleResultSet)psQuery.executeQuery();
//			if (rs.next()) {
//				STRUCT struct = (STRUCT)rs.getObject(1); 
//				if (!rs.wasNull() && struct != null) {
//					JGeometry geom = JGeometry.load(struct);
//					ordinatesArray = geom.getOrdinatesArray();
//				}
//			}
			rs = psQuery.executeQuery();
			if (rs.next()) {
				PGgeometry pgGeom = (PGgeometry)rs.getObject(1); 
				if (!rs.wasNull() && pgGeom != null) {
					Geometry geom = pgGeom.getGeometry();
					
					ordinatesArray = new double[geom.numPoints() * 3];
					
					for (int i=0, j=0; i<geom.numPoints(); i+=3, j++){
						ordinatesArray[i] = geom.getPoint(j).x;
						ordinatesArray[i+1] = geom.getPoint(j).y;
						ordinatesArray[i+2] = geom.getPoint(j).z;
					}
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
