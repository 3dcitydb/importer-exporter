package de.tub.citydb.db.kmlExporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import oracle.jdbc.OracleResultSet;

import org.citygml4j.geometry.BoundingVolume;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.filter.ExportFilter;

public class KmlSplitter {

	private final DBConnectionPool dbConnectionPool;
	private final WorkerPool<KmlSplittingResult> dbWorkerPool;
	private final DisplayLevel displayLevel;
	private final ExportFilter exportFilter;
	private final HashSet<String> alreadyExported;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private volatile boolean shouldRun = true;

	private Connection connection;
	
	public KmlSplitter(DBConnectionPool dbConnectionPool, 
					   WorkerPool<KmlSplittingResult> dbWorkerPool, 
					   ExportFilter exportFilter, 
					   DisplayLevel displayLevel,
					   HashSet<String> alreadyExported,
					   EventDispatcher eventDispatcher, 
					   Config config) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.dbWorkerPool = dbWorkerPool;
		this.exportFilter = exportFilter;
		this.displayLevel = displayLevel;
		this.alreadyExported = alreadyExported;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for connection if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.changeWorkspace(connection, 
										 database.getWorkspaces().getKmlExportWorkspace());

	}

	private void queryObjects() throws SQLException {

		long startTime = System.currentTimeMillis();

		if (config.getProject().getKmlExporter().getFilter().isSetSimpleFilter()) {
			for (String gmlId: config.getProject().getKmlExporter().getFilter().getSimpleFilter().getGmlIdFilter().getGmlIds()) {
				if (!shouldRun) break;
				if (alreadyExported.contains(gmlId)) continue;
				KmlSplittingResult splitter = new KmlSplittingResult(gmlId, displayLevel);
				dbWorkerPool.addWork(splitter);
				alreadyExported.add(gmlId);
			}
		}
		else if (config.getProject().getKmlExporter().getFilter().isSetComplexFilter() &&
				 config.getProject().getKmlExporter().getFilter().getComplexFilter().getTiledBoundingBox().isSet()) {
			
			BoundingVolume tile = exportFilter.getBoundingBoxFilter().getFilterState();
			OracleResultSet rs = null;
			PreparedStatement spatialQuery = null;
			try {
				spatialQuery = connection.prepareStatement(TileQueries.QUERY_GET_GMLIDS);

				int srid = ReferenceSystem.SAME_AS_IN_DB.getSrid();

				spatialQuery.setInt(1, srid);
				// coordinates for inside
				spatialQuery.setDouble(2, tile.getLowerCorner().getX());
				spatialQuery.setDouble(3, tile.getLowerCorner().getY());
				spatialQuery.setDouble(4, tile.getUpperCorner().getX());
				spatialQuery.setDouble(5, tile.getUpperCorner().getY());

				spatialQuery.setInt(6, srid);
				// coordinates for overlapbdydisjoint
				spatialQuery.setDouble(7, tile.getLowerCorner().getX());
				spatialQuery.setDouble(8, tile.getUpperCorner().getY());
				spatialQuery.setDouble(9, tile.getLowerCorner().getX());
				spatialQuery.setDouble(10, tile.getLowerCorner().getY());
				spatialQuery.setDouble(11, tile.getUpperCorner().getX());
				spatialQuery.setDouble(12, tile.getLowerCorner().getY());

				rs = (OracleResultSet)spatialQuery.executeQuery();
/*
				String absolutePath = config.getInternal().getExportFileName().trim();
				String filename = absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1,
														 absolutePath.lastIndexOf("."));
				String tileName = filename + "_Tile_" 
										   + filter.getComplexFilter().getTiledBoundingBox().getTileColumn()
										   + "_" 
										   + filter.getComplexFilter().getTiledBoundingBox().getTileRow();

				Logger.getInstance().info("Spatial query for " + tileName + " resolved in " +
										  (System.currentTimeMillis() - startTime) + " millis.");
*/
				while (rs.next() && shouldRun) {
					String gmlId = rs.getString("gmlId");
					if (alreadyExported.contains(gmlId)) continue;
					KmlSplittingResult splitter = new KmlSplittingResult(gmlId, displayLevel);
					dbWorkerPool.addWork(splitter);
					alreadyExported.add(gmlId);
				}

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

}
