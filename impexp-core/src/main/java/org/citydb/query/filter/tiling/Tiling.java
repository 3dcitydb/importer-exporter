/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.query.filter.tiling;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.filter.FilterException;

import java.sql.SQLException;

public class Tiling {
	private BoundingBox extent;
	private int rows;
	private int columns;
	
	private Tile activeTile;
	private double tileHeight;
	private double tileWidth;
	
	private Object tilingOptions;

	public Tiling(BoundingBox extent, int rows, int columns) throws FilterException {
		if (extent == null)
			throw new FilterException("The spatial tiling extent must not be null.");

		if (!extent.isValid())
			throw new FilterException("The bounding box extent is invalid.");

		if (extent.getSrs() != null && !extent.getSrs().isSupported())
			throw new FilterException("The reference system " + extent.getSrs().getDescription() + " of the tiling extent is not supported.");

		this.extent = extent;
		this.rows = rows > 0 ? rows : 1;
		this.columns = columns > 0 ? columns : 1;
		
		tileHeight = (extent.getUpperCorner().getY() - extent.getLowerCorner().getY()) / this.rows;
		tileWidth = (extent.getUpperCorner().getX() - extent.getLowerCorner().getX()) / this.columns;
	}

	public BoundingBox getExtent() {
		return extent;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	public Object getTilingOptions() {
		return tilingOptions;
	}
	
	public boolean isSetTilingOptions() {
		return tilingOptions != null;
	}

	public void setTilingOptions(Object tilingOptions) {
		this.tilingOptions = tilingOptions;
	}

	public void transformExtent(DatabaseSrs targetSrs, AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		if (!targetSrs.isSupported())
			throw new FilterException("The reference system " + targetSrs.getDescription() + " is not supported.");

		DatabaseSrs extentSrs = extent.isSetSrs() ? extent.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		if (targetSrs.getSrid() == extentSrs.getSrid())
			return;

		// convert extent into polygon
		GeometryObject extentObj = GeometryObject.createPolygon(new double[]{
				extent.getLowerCorner().getX(), extent.getLowerCorner().getY(),
				extent.getUpperCorner().getX(), extent.getLowerCorner().getY(),
				extent.getUpperCorner().getX(), extent.getUpperCorner().getY(),
				extent.getLowerCorner().getX(), extent.getUpperCorner().getY(),
				extent.getLowerCorner().getX(), extent.getLowerCorner().getY(),
		}, 2, extentSrs.getSrid());

		// transform polygon to new srs
		GeometryObject transformedExtent = null;
		try {
			transformedExtent = databaseAdapter.getUtil().transform(extentObj, targetSrs);
			if (transformedExtent == null)
				throw new FilterException("Failed to transform tiling extent to SRS " + targetSrs.getDescription() + ".");
		} catch (SQLException e) {
			throw new FilterException("Failed to transform tiling extent to SRS " + targetSrs.getDescription() + ".", e);
		}

		// create new extent from transformed polygon
		double[] coordinates = transformedExtent.getCoordinates(0);		
		extent = new BoundingBox(
				new Position(Math.min(coordinates[0], coordinates[6]), Math.min(coordinates[1], coordinates[3])),
				new Position(Math.max(coordinates[2], coordinates[4]), Math.max(coordinates[5], coordinates[7])),
				targetSrs
				);
		
		// adapt tile height and width
		tileHeight = (extent.getUpperCorner().getY() - extent.getLowerCorner().getY()) / rows;
		tileWidth = (extent.getUpperCorner().getX() - extent.getLowerCorner().getX()) / columns;
	}

	public Tile getTileAt(int x, int y) throws FilterException {
		if (x < 0 || y < 0 || x >= rows || y >= columns)
			throw new FilterException("Tile coordinates are out of bounds.");
		
		double minX = extent.getLowerCorner().getX() + (y * tileWidth);
		double minY = extent.getLowerCorner().getY() + (x * tileHeight);

		BoundingBox tileExtent = new BoundingBox(
				new Position(minX, minY),
				new Position(minX + tileWidth, minY + tileHeight),
				extent.getSrs()
				);

		return new Tile(tileExtent, x, y);
	}
	
	public Tile getActiveTile() {
		return activeTile;
	}

	public void setActiveTile(Tile activeTile) {
		this.activeTile = activeTile;
	}

}
