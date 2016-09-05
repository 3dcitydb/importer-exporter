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
package org.citydb.modules.common.filter.feature;

import java.sql.SQLException;
import java.util.List;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseUtil;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.api.geometry.Position;
import org.citydb.config.Config;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.FilterBoundingBox;
import org.citydb.config.project.filter.TiledBoundingBox;
import org.citydb.config.project.filter.Tiling;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.common.filter.Filter;
import org.citydb.modules.common.filter.FilterMode;
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

			if (boundingBoxConfig.getLowerCorner().getX() != null && 
					boundingBoxConfig.getLowerCorner().getY() != null &&
					boundingBoxConfig.getUpperCorner().getX() != null && 
					boundingBoxConfig.getUpperCorner().getY() != null) {
				boundingBox = new BoundingBox(boundingBoxConfig);
				if (boundingBox.getSrs() == null) {
					boundingBox.setSrs(DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem());
					LOG.warn("SRS on bounding box filter not set. Choosing database SRS '" + boundingBox.getSrs().getDatabaseSrsName() + "' instead.");
				}

				if (mode == FilterMode.KML_EXPORT) {
					try {
						boundingBox = convertToWGS84Bbox(boundingBox);
					} catch (SQLException e) {
						LOG.error("Failed to initialize bounding box filter.");
					}
				} else {
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
				}

				activeBoundingBox = boundingBox;

				if (useTiling) {
					Tiling tiling = ((TiledBoundingBox)boundingBoxConfig).getTiling();					
					rows = tiling.getRows();
					columns = tiling.getColumns();
					rowHeight = (boundingBox.getUpperCorner().getY() - boundingBox.getLowerCorner().getY()) / rows;  
					columnWidth = (boundingBox.getUpperCorner().getX() - boundingBox.getLowerCorner().getX()) / columns;
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
					if (minX >= activeBoundingBox.getLowerCorner().getX() &&
							minY >= activeBoundingBox.getLowerCorner().getY() &&
							maxX <= activeBoundingBox.getUpperCorner().getX() &&
							maxY <= activeBoundingBox.getUpperCorner().getY())
						return false;
					else
						return true;
				}

				else if (boundingBoxConfig.isSetOverlapMode()) {
					if (minX >= activeBoundingBox.getUpperCorner().getX() ||
							maxX <= activeBoundingBox.getLowerCorner().getX() ||
							minY >= activeBoundingBox.getUpperCorner().getY() ||
							maxY <= activeBoundingBox.getLowerCorner().getY())
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
					if (centroidX >= activeBoundingBox.getLowerCorner().getX() &&
							centroidY > activeBoundingBox.getLowerCorner().getY() &&
							centroidX < activeBoundingBox.getUpperCorner().getX() &&
							centroidY <= activeBoundingBox.getUpperCorner().getY())
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

		double lowerLeftX = boundingBox.getLowerCorner().getX() + (activeColumn * columnWidth);
		double lowerLeftY = boundingBox.getLowerCorner().getY() + (activeRow * rowHeight);
		double upperRightX = lowerLeftX + columnWidth;
		double upperRightY = lowerLeftY + rowHeight;

		activeBoundingBox = new BoundingBox(
				new Position(lowerLeftX, lowerLeftY),
				new Position(upperRightX, upperRightY),
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
		try {
			DatabaseSrs targetSrs = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
			DatabaseUtil util = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil();

			GeometryObject geomObj = GeometryObject.createPolygon(new double[]{
					bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(),
					bbox.getUpperCorner().getX(), bbox.getLowerCorner().getY(),
					bbox.getUpperCorner().getX(), bbox.getUpperCorner().getY(),
					bbox.getLowerCorner().getX(), bbox.getUpperCorner().getY(),
					bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(),
			}, 2, bbox.getSrs().getSrid()); // Srid = 4326

			GeometryObject convertedGeomObj = util.transform(geomObj, targetSrs);
			if (convertedGeomObj != null) {
				double[] coords = convertedGeomObj.getCoordinates(0);
				return GeometryObject.createPolygon(new double[]{
						coords[0], coords[1], 0,
						coords[2], coords[3], 0,
						coords[4], coords[5], 0,
						coords[6], coords[7], 0,
						coords[8], coords[9], 0
				}, 3, targetSrs.getSrid());
			}

			throw new SQLException("Transformation returned null geometry.");
		} catch (SQLException e) {
			LOG.error("Failed to convert bounding box geometry to database SRS.");
			throw e;
		}
	}

	private BoundingBox convertToWGS84Bbox(BoundingBox bbox) throws SQLException {	
		try {
			DatabaseSrs targetSrs = Database.PREDEFINED_SRS.get(Database.PredefinedSrsName.WGS84_2D);
			if (bbox.getSrs().getSrid() == targetSrs.getSrid())
				return bbox;			

			DatabaseUtil util = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getUtil();

			GeometryObject geomObj = GeometryObject.createPolygon(new double[]{
					bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(),
					bbox.getUpperCorner().getX(), bbox.getLowerCorner().getY(),
					bbox.getUpperCorner().getX(), bbox.getUpperCorner().getY(),
					bbox.getLowerCorner().getX(), bbox.getUpperCorner().getY(),
					bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(),
			}, 2, bbox.getSrs().getSrid());

			GeometryObject convertedGeomObj = util.transform(geomObj, targetSrs);
			if (convertedGeomObj != null) {
				double[] coordinates = convertedGeomObj.getCoordinates(0);		
				double xmin = Math.min(coordinates[0], coordinates[6]);
				double ymin = Math.min(coordinates[1], coordinates[3]);
				double xmax = Math.max(coordinates[2], coordinates[4]);
				double ymax = Math.max(coordinates[5], coordinates[7]);

				return new BoundingBox(new Position(xmin, ymin), new Position(xmax, ymax), targetSrs);
			}

			throw new SQLException("Transformation returned null geometry.");
		} catch (SQLException e) {
			LOG.error("Failed to convert bounding box geometry to database SRS.");
			throw e;
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
