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
import org.citydb.config.geometry.Point;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.operator.spatial.BinarySpatialOperator;
import org.citydb.query.filter.selection.operator.spatial.SpatialOperationFactory;

import java.sql.SQLException;

public class Tile {
	private final BoundingBox extent;
	private final int x;
	private final int y;

	private GeometryObject filterGeometry;
	
	public Tile(BoundingBox extent, int x, int y) {
		this.extent = extent;
		this.x = x;
		this.y = y;
	}

	public BoundingBox getExtent() {
		return extent;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public GeometryObject getFilterGeometry(AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		if (filterGeometry != null)
			return filterGeometry;
			
		DatabaseSrs extentSrs = extent.isSetSrs() ? extent.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		DatabaseSrs dbSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();

		BoundingBox envelope = null;
		if (extentSrs.getSrid() != dbSrs.getSrid()) {
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
				transformedExtent = databaseAdapter.getUtil().transform(extentObj, dbSrs);
				if (transformedExtent == null)
					throw new FilterException("Failed to transform tiling extent to SRS " + dbSrs.getDescription() + ".");
			} catch (SQLException e) {
				throw new FilterException("Failed to transform tiling extent to SRS " + dbSrs.getDescription() + ".", e);
			}
			
			// create new extent from transformed polygon
			double[] coordinates = transformedExtent.getCoordinates(0);		
			envelope = new BoundingBox(
					new Position(Math.min(coordinates[0], coordinates[6]), Math.min(coordinates[1], coordinates[3])),
					new Position(Math.max(coordinates[2], coordinates[4]), Math.max(coordinates[5], coordinates[7])),
					dbSrs
					);
		} else
			envelope = new BoundingBox(extent);
		
		filterGeometry = GeometryObject.createEnvelope(new double[]{
				envelope.getLowerCorner().getX(), envelope.getLowerCorner().getY(), envelope.getUpperCorner().getX(), envelope.getUpperCorner().getY()
		}, 2, dbSrs.getSrid());
		
		return filterGeometry;
	}
	
	public BinarySpatialOperator getFilterPredicate(AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		return SpatialOperationFactory.bbox(getFilterGeometry(databaseAdapter));		
	}
	
	public boolean isOnTile(Point point, AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		DatabaseSrs extentSrs = extent.isSetSrs() ? extent.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		DatabaseSrs pointSrs = point.isSetSrs() ? point.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		
		if (!pointSrs.isSupported())
			throw new FilterException("The reference system " + pointSrs.getDescription() + " is not supported.");
		
		Position pos = null;
		if (pointSrs.getSrid() != extentSrs.getSrid()) {			
			try {
				GeometryObject transformed = databaseAdapter.getUtil().transform(GeometryObject.createPoint(new double[]{point.getX(), point.getY()}, 2, pointSrs.getSrid()), extentSrs);
				if (transformed == null)
					throw new FilterException("Failed to convert input geometry to tile SRS.");
					
				pos = new Position(transformed.getCoordinates(0)[0], transformed.getCoordinates(0)[1]);
			} catch (SQLException e) {
				throw new FilterException("Failed to convert input geometry to tile SRS.", e);
			}
		} else
			pos = point.getPos();
		
		return pos.getX() > extent.getLowerCorner().getX() 
				&& pos.getX() <= extent.getUpperCorner().getX() 
				&& pos.getY() > extent.getLowerCorner().getY() 
				&& pos.getY() <= extent.getUpperCorner().getY();
	}

}
