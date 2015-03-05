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
package org.citydb.database.adapter.h2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.adapter.AbstractGeometryConverterAdapter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryConverterAdapter extends AbstractGeometryConverterAdapter {
	// Note: This is a very limited implementation only aiming at
	// supporting the storage of polygons which are required for 
	// representing texture coordinates in cache tables

	private final GeometryFactory factory;

	protected GeometryConverterAdapter() {
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
				coordinates[0][element++] = coordinate.x;
				coordinates[0][element++] = coordinate.y;
				coordinates[0][element++] = coordinate.z;
			}
		} else {
			for (int i = 0; i < exterior.getNumPoints(); i++) {
				Coordinate coordinate = exterior.getPointN(i).getCoordinate();
				coordinates[0][element++] = coordinate.x;
				coordinates[0][element++] = coordinate.y;
			}
		}

		for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
			LineString interior = polygon.getInteriorRingN(i);
			element = 0;

			if (dimension == 3) {
				for (int j = 0; j < interior.getNumPoints(); j++) {
					Coordinate coordinate = exterior.getPointN(j).getCoordinate();
					coordinates[i + 1][element++] = coordinate.x;
					coordinates[i + 1][element++] = coordinate.y;
					coordinates[i + 1][element++] = coordinate.z;
				}
			} else {
				for (int j = 0; j < interior.getNumPoints(); j++) {
					Coordinate coordinate = exterior.getPointN(j).getCoordinate();
					coordinates[i + 1][element++] = coordinate.x;
					coordinates[i + 1][element++] = coordinate.y;
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
		case CURVE:
		case POINT:
		case MULTI_CURVE:
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
