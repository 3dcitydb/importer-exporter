/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.common.filter.feature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.BoundingBoxCorner;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.FilterBoundingBox;
import org.citydb.config.project.filter.TiledBoundingBox;
import org.citydb.config.project.filter.Tiling;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.common.filter.Filter;
import org.citydb.modules.common.filter.FilterMode;
import org.citydb.modules.kml.database.Queries;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

public class BoundingBoxFilter implements Filter<Envelope> {
	private final Logger LOG = Logger.getInstance();
	private final AbstractFilterConfig filterConfig;
	private final FilterMode mode;
	private final Config config;

	private boolean isActive;
	private boolean useTiling;
	private FilterBoundingBox boundingBoxConfig;

	private BoundingBox boundingBox;
	private BoundingBox activeBoundingBox;
	private GeometryObject activeBoundingBoxGeometry;

	private double rowHeight = 0;  
	private double columnWidth = 0;
	private int rows = 1;  
	private int columns = 1;
	private int activeRow = 0;
	private int activeColumn = 0;

	public BoundingBoxFilter(Config config, FilterMode mode) {
		this.mode = mode;
		this.config = config;

		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else if (mode == FilterMode.KML_EXPORT)
			filterConfig = config.getProject().getKmlExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();			

		init();
	}

	private void init() {
		isActive = filterConfig.isSetComplexFilter() &&
		filterConfig.getComplexFilter().getBoundingBox().isSet();

		if (isActive) {
			boundingBoxConfig = filterConfig.getComplexFilter().getBoundingBox();
			if (mode == FilterMode.EXPORT || mode == FilterMode.KML_EXPORT)
				useTiling = ((TiledBoundingBox)boundingBoxConfig).getTiling().getMode() != TilingMode.NO_TILING;

			if (boundingBoxConfig.getLowerLeftCorner().getX() != null && 
					boundingBoxConfig.getLowerLeftCorner().getY() != null &&
					boundingBoxConfig.getUpperRightCorner().getX() != null && 
					boundingBoxConfig.getUpperRightCorner().getY() != null) {
				boundingBox = new BoundingBox(boundingBoxConfig);
				if (boundingBox.getSrs() == null) {
					boundingBox.setSrs(DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem());
					LOG.warn("SRS on bounding box filter not set. Choosing database SRS '" + boundingBox.getSrs().getDatabaseSrsName() + "' instead.");
				}

				// check whether we have to transform the bounding box
				DatabaseSrs targetSrs;
				if (mode == FilterMode.KML_EXPORT) {
					targetSrs = DatabaseSrs.createDefaultSrs();
					targetSrs.setSrid(4326);					
				}
				else {
					targetSrs = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
				}

				// targetSrs differs if a coordinate transformation is applied to the CityGML export
				if (mode == FilterMode.EXPORT) {
					DatabaseSrs tmp = config.getProject().getExporter().getTargetSRS();
					if (tmp.isSupported() && tmp.getSrid() != targetSrs.getSrid())
						targetSrs = tmp;
				}
				
				if (boundingBox.getSrs().isSupported() && boundingBox.getSrs().getSrid() != targetSrs.getSrid()) {			
					try {
						if (mode == FilterMode.KML_EXPORT) {
							boundingBox = convertToWGS84Bbox(boundingBox, targetSrs);
						}
						else {
							boundingBox = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil().transformBoundingBox(boundingBox, boundingBox.getSrs(), targetSrs);
						}
						
					} catch (SQLException sqlEx) {
						LOG.error("Failed to initialize bounding box filter.");
					}
				}

				activeBoundingBox = boundingBox;

				if (useTiling) {
					Tiling tiling = ((TiledBoundingBox)boundingBoxConfig).getTiling();					
					rows = tiling.getRows();
					columns = tiling.getColumns();
					rowHeight = (boundingBox.getUpperRightCorner().getY() - boundingBox.getLowerLeftCorner().getY()) / rows;  
					columnWidth = (boundingBox.getUpperRightCorner().getX() - boundingBox.getLowerLeftCorner().getX()) / columns;
				}
			} else
				isActive = false;
		}
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(Envelope envelope) {
		if (isActive) {
			if (!envelope.isSetLowerCorner() || !envelope.isSetUpperCorner())
				return true;

			DirectPosition lowerCorner = envelope.getLowerCorner();
			DirectPosition upperCorner = envelope.getUpperCorner();

			if (!lowerCorner.isSetValue() || !upperCorner.isSetValue())
				return true;

			List<Double> lowerCornerValue = lowerCorner.getValue();
			List<Double> upperCornerValue = upperCorner.getValue();

			if (lowerCornerValue.size() < 2 || upperCornerValue.size() < 2)
				return true;

			Double minX = lowerCornerValue.get(0);
			Double minY = lowerCornerValue.get(1);

			Double maxX = upperCornerValue.get(0);
			Double maxY = upperCornerValue.get(1);

			if (!useTiling) { // no tiling, just for CityGML Mode. Because "no_tiling" in KML_Export mode was internally mapped to manual tiling with one tile 
				if (boundingBoxConfig.isSetContainMode()) {
					if (minX >= activeBoundingBox.getLowerLeftCorner().getX() &&
							minY >= activeBoundingBox.getLowerLeftCorner().getY() &&
							maxX <= activeBoundingBox.getUpperRightCorner().getX() &&
							maxY <= activeBoundingBox.getUpperRightCorner().getY())
						return false;
					else
						return true;
				}

				else if (boundingBoxConfig.isSetOverlapMode()) {
					if (minX >= activeBoundingBox.getUpperRightCorner().getX() ||
							maxX <= activeBoundingBox.getLowerLeftCorner().getX() ||
							minY >= activeBoundingBox.getUpperRightCorner().getY() ||
							maxY <= activeBoundingBox.getLowerLeftCorner().getY())
						return true;
					else 
						return false;
				}
			}
			else { // manual tiling				
				double centroidX = minX + (maxX - minX) / 2;
				double centroidY = minY + (maxY - minY) / 2;
				
				if (mode == FilterMode.KML_EXPORT) {
					Point[] points = new Point[4];
					double[] coords = activeBoundingBoxGeometry.getCoordinates(0);
					
					if (activeBoundingBoxGeometry.getDimension() == 2) {
						points[0] = new Point(coords[0], coords[1]);
						points[1] = new Point(coords[2], coords[3]);
						points[2] = new Point(coords[4], coords[5]);
						points[3] = new Point(coords[6], coords[7]);
					}
					else if(activeBoundingBoxGeometry.getDimension() == 3) {
						points[0] = new Point(coords[0], coords[1]);
						points[1] = new Point(coords[3], coords[4]);
						points[2] = new Point(coords[6], coords[7]);
						points[3] = new Point(coords[9], coords[10]);
					}			
					
					boolean result = true;				
					int i, j;
					for (i = 0, j = points.length - 1; i < points.length; j = i++) {
						if ((points[i].y > centroidY) != (points[j].y > centroidY)
								&& (centroidX < (points[j].x - points[i].x) * (centroidY - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
							result = !result;
						}
					}
					return result;
				}
				else {
					if (centroidX >= activeBoundingBox.getLowerLeftCorner().getX() &&
							centroidY > activeBoundingBox.getLowerLeftCorner().getY() &&
							centroidX < activeBoundingBox.getUpperRightCorner().getX() &&
							centroidY <= activeBoundingBox.getUpperRightCorner().getY())
						return false;
					else
						return true;
				}
			}
		}

		return false;
	}

	public BoundingBox getFilterState() {
		return activeBoundingBox;
	}
	
	public GeometryObject getFilterStateForKml() {
		return activeBoundingBoxGeometry;
	}

	public void setActiveTile(int activeRow, int activeColumn) {
		if (!useTiling || 
				activeRow < 0 || activeRow > rows ||
				activeColumn < 0 || activeColumn > columns)
			return;

		this.activeRow = activeRow;
		this.activeColumn = activeColumn;

		double lowerLeftX = boundingBox.getLowerLeftCorner().getX() + (activeColumn * columnWidth);
		double lowerLeftY = boundingBox.getLowerLeftCorner().getY() + (activeRow * rowHeight);
		double upperRightX = lowerLeftX + columnWidth;
		double upperRightY = lowerLeftY + rowHeight;

		activeBoundingBox = new BoundingBox(
				new BoundingBoxCorner(lowerLeftX, lowerLeftY),
				new BoundingBoxCorner(upperRightX, upperRightY),
				boundingBox.getSrs()
		);
		
		if (mode == FilterMode.KML_EXPORT) {
			try {
				activeBoundingBoxGeometry = this.convertWGS84BboxToDBSrsGeometry(activeBoundingBox);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getTileRow() {
		return activeRow;
	}

	public int getTileColumn() {
		return activeColumn;
	}

	private GeometryObject convertWGS84BboxToDBSrsGeometry(BoundingBox bbox) throws SQLException {
		GeometryObject convertedGeomObj = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		DatabaseConnectionPool dbPool = DatabaseConnectionPool.getInstance();

		try {
			conn = dbPool.getConnection();
			stmt = conn.prepareStatement(Queries.TRANSFORM_GEOMETRY_TO_DBSRS(dbPool.getActiveDatabaseAdapter().getSQLAdapter()));

			GeometryObject geomObj = GeometryObject.createPolygon(new double[]{
					bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(),
					bbox.getUpperRightCorner().getX(), bbox.getLowerLeftCorner().getY(),
					bbox.getUpperRightCorner().getX(), bbox.getUpperRightCorner().getY(),
					bbox.getLowerLeftCorner().getX(), bbox.getUpperRightCorner().getY(),
					bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(),
			}, 2, bbox.getSrs().getSrid()); // Srid = 4326

			Object unconverted = dbPool.getActiveDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, conn);
			if (unconverted == null)
				return null;
			
			stmt.setObject(1, unconverted);
			int dbSrid2D = dbPool.getActiveDatabaseAdapter().getUtil().get2DSrid(dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem());
			stmt.setInt(2, dbSrid2D);
			rs = stmt.executeQuery();
			if (rs.next())
				convertedGeomObj = dbPool.getActiveDatabaseAdapter().getGeometryConverter().getGeometry(rs.getObject(1));

			double[] coords = convertedGeomObj.getCoordinates(0);
			if (dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().is3D()) {
				convertedGeomObj = GeometryObject.createPolygon(new double[]{
						coords[0], coords[1], 0,
						coords[2], coords[3], 0,
						coords[4], coords[5], 0,
						coords[6], coords[7], 0,
						coords[8], coords[9], 0
				}, 3, dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());
			}
			return convertedGeomObj;
							
		} catch (Exception e) {
			Logger.getInstance().error("Exception when converting bounding box geometry to DBsrs.");
			throw e;
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) { }
			if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
			if (conn != null) try { conn.close(); } catch (SQLException e) { }
		}
	}
	
	private BoundingBox convertToWGS84Bbox(BoundingBox bbox, DatabaseSrs wgs84Srs) throws SQLException {
		GeometryObject convertedGeomObj = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		DatabaseConnectionPool dbPool = DatabaseConnectionPool.getInstance();

		try {
			conn = dbPool.getConnection();
			stmt = conn.prepareStatement(Queries.TRANSFORM_GEOMETRY_TO_WGS84(dbPool.getActiveDatabaseAdapter().getSQLAdapter()));

			int srid2D = dbPool.getActiveDatabaseAdapter().getUtil().get2DSrid(bbox.getSrs());
			GeometryObject geomObj = GeometryObject.createPolygon(new double[]{
					bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(),
					bbox.getUpperRightCorner().getX(), bbox.getLowerLeftCorner().getY(),
					bbox.getUpperRightCorner().getX(), bbox.getUpperRightCorner().getY(),
					bbox.getLowerLeftCorner().getX(), bbox.getUpperRightCorner().getY(),
					bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(),
			}, 2, srid2D);

			Object unconverted = dbPool.getActiveDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, conn);
			if (unconverted == null)
				return null;

			stmt.setObject(1, unconverted);
			rs = stmt.executeQuery();
			if (rs.next())
				convertedGeomObj = dbPool.getActiveDatabaseAdapter().getGeometryConverter().getGeometry(rs.getObject(1));

			double[] coordinates = convertedGeomObj.getCoordinates(0);		
			double xmin = Math.min(coordinates[0], coordinates[6]);
			double ymin = Math.min(coordinates[1], coordinates[3]);
			double xmax = Math.max(coordinates[2], coordinates[4]);
			double ymax = Math.max(coordinates[5], coordinates[7]);

			return new BoundingBox(new BoundingBoxCorner(xmin, ymin), new BoundingBoxCorner(xmax, ymax), wgs84Srs);
			
		} catch (Exception e) {
			Logger.getInstance().error("Exception when converting to WGS84 Bounding Box.");
			throw e;
		} finally {
			if (rs != null) try { rs.close(); } catch (SQLException e) { }
			if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
			if (conn != null) try { conn.close(); } catch (SQLException e) { }
		}
	}
	
	private class Point {
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double x;
		public double y;
	}
}
