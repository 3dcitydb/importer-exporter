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

import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.modules.kml.controller.KmlExporter;
import de.tub.citydb.modules.kml.util.CityObject4JSON;
import de.tub.citydb.util.Util;

public class KmlSplitter {

	private final static int BUILDING = Util.cityObject2classId(CityGMLClass.BUILDING);
	
	private final DatabaseConnectionPool dbConnectionPool;
	private final WorkerPool<KmlSplittingResult> dbWorkerPool;
	private final DisplayLevel displayLevel;
	private final ExportFilter exportFilter;
	private final Config config;
	private volatile boolean shouldRun = true;

	private Connection connection;
	
	public KmlSplitter(DatabaseConnectionPool dbConnectionPool, 
					   WorkerPool<KmlSplittingResult> dbWorkerPool, 
					   ExportFilter exportFilter, 
					   DisplayLevel displayLevel,
					   Config config) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.dbWorkerPool = dbWorkerPool;
		this.exportFilter = exportFilter;
		this.displayLevel = displayLevel;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for connection if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(connection, 
										 database.getWorkspaces().getKmlExportWorkspace());

	}

	private void queryObjects() throws SQLException {

//		long startTime = System.currentTimeMillis();

		if (config.getProject().getKmlExporter().getFilter().isSetSimpleFilter()) {
			for (String gmlId: config.getProject().getKmlExporter().getFilter().getSimpleFilter().getGmlIdFilter().getGmlIds()) {
				if (!shouldRun) break;
				if (KmlExporter.getAlreadyExported().containsKey(gmlId)) continue;
				KmlSplittingResult splitter = new KmlSplittingResult(gmlId, displayLevel);
				dbWorkerPool.addWork(splitter);

				double[] ordinatesArray = getEnvelopeInWGS84(gmlId);
				CityObject4JSON cityObject4Json = new CityObject4JSON();
				cityObject4Json.setEnvelope(ordinatesArray);
				KmlExporter.getAlreadyExported().put(gmlId, cityObject4Json);
			}
		}
		else if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter() &&
				 config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().isSet()) {
			
			BoundingBox tile = exportFilter.getBoundingBoxFilter().getFilterState();
			OracleResultSet rs = null;
			PreparedStatement spatialQuery = null;
			try {
				spatialQuery = connection.prepareStatement(TileQueries.QUERY_GET_GMLIDS);

				int srid = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().getSrid();

				spatialQuery.setInt(1, srid);
				// coordinates for inside
				spatialQuery.setDouble(2, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(3, tile.getLowerLeftCorner().getY());
				spatialQuery.setDouble(4, tile.getUpperRightCorner().getX());
				spatialQuery.setDouble(5, tile.getUpperRightCorner().getY());

				spatialQuery.setInt(6, srid);
				// coordinates for overlapbdydisjoint
				spatialQuery.setDouble(7, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(8, tile.getUpperRightCorner().getY());
				spatialQuery.setDouble(9, tile.getLowerLeftCorner().getX());
				spatialQuery.setDouble(10, tile.getLowerLeftCorner().getY());
				spatialQuery.setDouble(11, tile.getUpperRightCorner().getX());
				spatialQuery.setDouble(12, tile.getLowerLeftCorner().getY());

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
					int classId = rs.getInt("class_id");
					if (classId != BUILDING || KmlExporter.getAlreadyExported().containsKey(gmlId)) continue;
					KmlSplittingResult splitter = new KmlSplittingResult(gmlId, displayLevel);
					dbWorkerPool.addWork(splitter);

					CityObject4JSON cityObject4Json = new CityObject4JSON();
					cityObject4Json.setTileRow(exportFilter.getBoundingBoxFilter().getTileRow());
					cityObject4Json.setTileColumn(exportFilter.getBoundingBoxFilter().getTileColumn());
					double[] ordinatesArray = getEnvelopeInWGS84(gmlId);
					cityObject4Json.setEnvelope(ordinatesArray);
					KmlExporter.getAlreadyExported().put(gmlId, cityObject4Json);

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
					try {
						rs.close();
					}
					catch (SQLException sqlEx) {
						throw sqlEx;
					}

					rs = null;
				}

				if (spatialQuery != null) {
					try {
						spatialQuery.close();
					}
					catch (SQLException sqlEx) {
						throw sqlEx;
					}

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

	private double[] getEnvelopeInWGS84(String gmlId) {
		double[] ordinatesArray = null;
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;

		try {
			psQuery = dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem().is3D() ? 
					  connection.prepareStatement(TileQueries.QUERY_GET_ENVELOPE_IN_WGS84_3D_FROM_GML_ID):
					  connection.prepareStatement(TileQueries.QUERY_GET_ENVELOPE_IN_WGS84_FROM_GML_ID);
						  
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
