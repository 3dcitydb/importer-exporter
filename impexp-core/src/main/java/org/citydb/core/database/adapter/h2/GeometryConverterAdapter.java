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
package org.citydb.core.database.adapter.h2;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.AbstractGeometryConverterAdapter;
import org.locationtech.jts.geom.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class GeometryConverterAdapter extends AbstractGeometryConverterAdapter {
	// Note: This is a very limited implementation only aiming at
	// supporting the storage of polygons which are required for 
	// representing texture coordinates in cache tables

	private final GeometryFactory factory;

	protected GeometryConverterAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
		factory = new GeometryFactory();
	}

	@Override
	public int getNullGeometryType() {
		return Types.OTHER;
	}

	@Override
	public String getNullGeometryTypeName() {
		return "GEOMETRY";
	}

	@Override
	public GeometryObject getEnvelope(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public GeometryObject getPoint(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public GeometryObject getMultiPoint(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public GeometryObject getCurve(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public GeometryObject getMultiCurve(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public GeometryObject getPolygon(Object geomObj) throws SQLException {
		GeometryObject polygon = null;

		if (geomObj instanceof Polygon)
			polygon = getPolygon((Polygon)geomObj);

		return polygon;
	}

	private GeometryObject getPolygon(Polygon polygon) {
		return GeometryObject.createPolygon(getPolygonCoordinates(polygon), polygon.getDimension(), polygon.getSRID());
	}

	private double[][] getPolygonCoordinates(Polygon polygon) {
		double[][] coordinates = new double[polygon.getNumGeometries()][];
		int dimension = polygon.getDimension();

		LineString exterior = polygon.getExteriorRing();
		coordinates[0] = new double[exterior.getNumPoints() * dimension];
		int element = 0;

		if (dimension == 3) {
			for (int i = 0; i < exterior.getNumPoints(); i++) {
				Coordinate coordinate = exterior.getPointN(i).getCoordinate();
				coordinates[0][element++] = coordinate.getX();
				coordinates[0][element++] = coordinate.getY();
				coordinates[0][element++] = coordinate.getZ();
			}
		} else {
			for (int i = 0; i < exterior.getNumPoints(); i++) {
				Coordinate coordinate = exterior.getPointN(i).getCoordinate();
				coordinates[0][element++] = coordinate.getX();
				coordinates[0][element++] = coordinate.getY();
			}
		}

		for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
			LineString interior = polygon.getInteriorRingN(i);
			element = 0;

			if (dimension == 3) {
				for (int j = 0; j < interior.getNumPoints(); j++) {
					Coordinate coordinate = exterior.getPointN(j).getCoordinate();
					coordinates[i + 1][element++] = coordinate.getX();
					coordinates[i + 1][element++] = coordinate.getY();
					coordinates[i + 1][element++] = coordinate.getZ();
				}
			} else {
				for (int j = 0; j < interior.getNumPoints(); j++) {
					Coordinate coordinate = exterior.getPointN(j).getCoordinate();
					coordinates[i + 1][element++] = coordinate.getX();
					coordinates[i + 1][element++] = coordinate.getY();
				}
			}
		}

		return coordinates;
	}

	@Override
	public GeometryObject getMultiPolygon(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public GeometryObject getGeometry(Object geomObj) throws SQLException {
		return null;
	}

	@Override
	public Object getDatabaseObject(GeometryObject geomObj, Connection connection) throws SQLException {
		Geometry geometry = null;

		switch (geomObj.getGeometryType()) {
		case POLYGON:
			geometry = convertPolygonToJTS(geomObj);
			break;
		case LINE_STRING:
		case POINT:
		case MULTI_LINE_STRING:
		case MULTI_POINT:
		case ENVELOPE:
		case MULTI_POLYGON:
		case SOLID:
		case COMPOSITE_SOLID:
			break;
		}

		if (geometry == null)
			throw new SQLException("Failed to convert geometry to internal database representation.");

		return geometry;		
	}

	@Override
	public String getDatabaseObjectConstructor(GeometryObject geomObj) {
		return null;
	}

	private Polygon convertPolygonToJTS(GeometryObject geomObj) {
		double[][] coordinates = geomObj.getCoordinates();
		int dimension = geomObj.getDimension();

		LinearRing shell = null;
		LinearRing[] holes = geomObj.getNumElements() - 1 > 0 ? new LinearRing[geomObj.getNumElements() - 1] : null;

		for (int i = 0; i < coordinates.length; i++) {
			LinearRing ring = factory.createLinearRing(getCoordinatesArray(coordinates[i], dimension));

			if (i == 0)
				shell = ring;
			else
				holes[i - 1] = ring;
		}

		return factory.createPolygon(shell, holes);
	}
	
	private Coordinate[] getCoordinatesArray(double[] coordinates, int dimension) {
		Coordinate[] result = new Coordinate[coordinates.length / dimension];

		for (int j = 0, element = 0; j < coordinates.length; j += dimension) {
			Coordinate coordinate = new Coordinate();
			for (int k = 0; k < dimension; k++)
				coordinate.setOrdinate(k, coordinates[j + k]);
			
			result[element++] = coordinate;
		}
		
		return result;
	}

}
