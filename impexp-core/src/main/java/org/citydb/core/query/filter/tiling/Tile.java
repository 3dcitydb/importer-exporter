/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.query.filter.tiling;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Point;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.operator.spatial.BinarySpatialOperator;
import org.citydb.core.query.filter.selection.operator.spatial.SpatialOperationFactory;

import java.sql.SQLException;

public class Tile {
	private final BoundingBox extent;
	private final int row;
	private final int column;

	private GeometryObject filterGeometry;
	
	public Tile(BoundingBox extent, int row, int column) {
		this.extent = extent;
		this.row = row;
		this.column = column;
	}

	public BoundingBox getExtent() {
		return extent;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
	
	public GeometryObject getFilterGeometry(AbstractDatabaseAdapter databaseAdapter) throws FilterException {
		if (filterGeometry != null)
			return filterGeometry;
			
		DatabaseSrs extentSrs = extent.isSetSrs() ? extent.getSrs() : databaseAdapter.getConnectionMetaData().getReferenceSystem();
		DatabaseSrs dbSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();

		BoundingBox envelope;
		if (extentSrs.getSrid() != dbSrs.getSrid()) {
			try {
				envelope = databaseAdapter.getUtil().transform2D(extent, extentSrs, dbSrs);
			} catch (SQLException e) {
				throw new FilterException("Failed to transform tiling extent to SRS " + dbSrs.getDescription() + ".", e);
			}
		} else {
			envelope = new BoundingBox(extent);
		}
		
		filterGeometry = GeometryObject.createEnvelope(new double[]{
				envelope.getLowerCorner().getX(), envelope.getLowerCorner().getY(),
				envelope.getUpperCorner().getX(), envelope.getUpperCorner().getY()
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
		
		Position pos;
		if (pointSrs.getSrid() != extentSrs.getSrid()) {			
			try {
				GeometryObject transformed = databaseAdapter.getUtil().transform(GeometryObject.createPoint(
						new double[]{point.getX(), point.getY()}, 2, pointSrs.getSrid()), extentSrs);
				pos = new Position(transformed.getCoordinates(0)[0], transformed.getCoordinates(0)[1]);
			} catch (SQLException e) {
				throw new FilterException("Failed to convert input geometry to tile SRS.", e);
			}
		} else {
			pos = point.getPos();
		}
		
		return pos.getX() > extent.getLowerCorner().getX() 
				&& pos.getX() <= extent.getUpperCorner().getX() 
				&& pos.getY() > extent.getLowerCorner().getY() 
				&& pos.getY() <= extent.getUpperCorner().getY();
	}

}
