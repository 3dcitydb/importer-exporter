/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
	private GeometryObject activeBoundingGeometry;

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

		inti2();
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
				DatabaseSrs targetSrs = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();

				// targetSrs differs if a coordinate transformation is applied to the CityGML export
				if (mode == FilterMode.EXPORT) {
					DatabaseSrs tmp = config.getProject().getExporter().getTargetSRS();
					if (tmp.isSupported() && tmp.getSrid() != targetSrs.getSrid())
						targetSrs = tmp;
				}
				
				if (boundingBox.getSrs().isSupported() && boundingBox.getSrs().getSrid() != targetSrs.getSrid()) {			
					try {
						boundingBox = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil().transformBoundingBox(boundingBox, boundingBox.getSrs(), targetSrs);
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
	
	private void inti2() {
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
				DatabaseSrs targetSrs = DatabaseSrs.createDefaultSrs();
				targetSrs.setSrid(4326);
				
				if (boundingBox.getSrs().isSupported() && boundingBox.getSrs().getSrid() != targetSrs.getSrid()) {			
					try {
						boundingBox = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil().transformBoundingBox(boundingBox, boundingBox.getSrs(), targetSrs);
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

			if (!useTiling) { // no tiling
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
				if (centroidX >= activeBoundingBox.getLowerLeftCorner().getX() &&
						centroidY > activeBoundingBox.getLowerLeftCorner().getY() &&
						centroidX < activeBoundingBox.getUpperRightCorner().getX() &&
						centroidY <= activeBoundingBox.getUpperRightCorner().getY())
					return false;
				else
					return true;
			}
		}

		return false;
	}
	
	public boolean filter2(Envelope envelope) {
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

			if (!useTiling) { // no tiling
				// just for CityGML Export, do nothing here...
			}
			else { // manual tiling
				double centroidX = minX + (maxX - minX) / 2;
				double centroidY = minY + (maxY - minY) / 2;
				
				Point[] points = new Point[4];
				double[] coords = activeBoundingGeometry.getCoordinates(0);
				points[0] = new Point(coords[0], coords[1]);
				points[1] = new Point(coords[2], coords[3]);
				points[2] = new Point(coords[4], coords[5]);
				points[3] = new Point(coords[6], coords[7]);
				
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
		}

		return false;
	}

	public BoundingBox getFilterState() {
		return activeBoundingBox;
	}
	
	public GeometryObject getFilterState2() {
		return activeBoundingGeometry;
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
	}
	
	public void setActiveTile2(int activeRow, int activeColumn) {
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
		
		try {
			activeBoundingGeometry = this.convertBboxToDBSrsGeometry(activeBoundingBox);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private GeometryObject convertBboxToDBSrsGeometry(BoundingBox bbox) throws SQLException {
		GeometryObject convertedGeomObj = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = DatabaseConnectionPool.getInstance().getConnection();
			stmt = conn.prepareStatement(Queries.TRANSFORM_GEOMETRY_TO_DBSRS(DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getSQLAdapter()));

			GeometryObject geomObj = GeometryObject.createPolygon(new double[]{
					bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(),
					bbox.getUpperRightCorner().getX(), bbox.getLowerLeftCorner().getY(),
					bbox.getUpperRightCorner().getX(), bbox.getUpperRightCorner().getY(),
					bbox.getLowerLeftCorner().getX(), bbox.getUpperRightCorner().getY(),
					bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(),
			}, 2, bbox.isSetSrs() ? bbox.getSrs().getSrid() : DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());

			Object unconverted = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getGeometryConverter().getDatabaseObject(geomObj, conn);
			if (unconverted == null)
				return null;

			stmt.setObject(1, unconverted);
			stmt.setInt(2, DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());
			rs = stmt.executeQuery();
			if (rs.next())
				convertedGeomObj = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getGeometryConverter().getGeometry(rs.getObject(1));

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
	
	public int getTileRow() {
		return activeRow;
	}

	public int getTileColumn() {
		return activeColumn;
	}
	
	class Point {
		public Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double x;
		public double y;
	}
}
