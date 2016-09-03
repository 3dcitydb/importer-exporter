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
package org.citydb.database.adapter.postgis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.api.geometry.ElementType;
import org.citydb.database.adapter.AbstractGeometryConverterAdapter;
import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

public class GeometryConverterAdapter extends AbstractGeometryConverterAdapter {

	protected GeometryConverterAdapter() {

	}

	@Override
	public int getNullGeometryType() {
		return Types.OTHER;
	}

	@Override
	public String getNullGeometryTypeName() {
		return "ST_GEOMETRY";
	}

	@Override
	public GeometryObject getEnvelope(Object geomObj) throws SQLException {
		GeometryObject envelope = null;
		if (geomObj instanceof PGgeometry)
			envelope = getEnvelope(((PGgeometry)geomObj).getGeometry());

		return envelope;
	}

	private GeometryObject getEnvelope(Geometry geometry) {
		double[] coordinates = new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};

		for (int i = 0; i < geometry.numPoints(); ++i) {
			Point point = geometry.getPoint(i);
			if (point.x < coordinates[0])
				coordinates[0] = point.x;
			if (point.y < coordinates[1])
				coordinates[1] = point.y;
			if (point.z < coordinates[2])
				coordinates[2] = point.z;
			if (point.x > coordinates[3])
				coordinates[3] = point.x;
			if (point.y > coordinates[4])
				coordinates[4] = point.y;
			if (point.z > coordinates[5])
				coordinates[5] = point.z;
		}

		return GeometryObject.createEnvelope(coordinates, 3, geometry.getSrid());
	}

	@Override
	public GeometryObject getPoint(Object geomObj) throws SQLException {
		GeometryObject point = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() != Geometry.POINT)
				return null;

			point = getPoint((Point)geometry);
		}

		return point;
	}

	private GeometryObject getPoint(Point point) {
		return GeometryObject.createPoint(getPointCoordinates(point), point.getDimension(), point.getSrid());
	}

	private double[] getPointCoordinates(Point point) {
		int dimension = point.getDimension();
		double[] coordinates = new double[dimension];

		coordinates[0] = point.x;
		coordinates[1] = point.y;
		if (dimension == 3)
			coordinates[2] = point.z;

		return coordinates;
	}

	@Override
	public GeometryObject getMultiPoint(Object geomObj) throws SQLException {
		GeometryObject multiPoint = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() == Geometry.MULTIPOINT) {
				multiPoint = getMultiPoint((MultiPoint)geometry);
			}

			else if (geometry.getType() == Geometry.POINT) {
				Point pointObj = (Point)geometry;
				double[][] coordiantes = new double[1][];
				coordiantes[0] = getPointCoordinates(pointObj);

				multiPoint = GeometryObject.createMultiPoint(coordiantes, pointObj.getDimension(), pointObj.getSrid());
			}
		}

		return multiPoint;
	}

	private GeometryObject getMultiPoint(MultiPoint multiPoint) {
		double[][] coordinates = new double[multiPoint.numPoints()][];
		int dimension = multiPoint.getDimension();

		if (dimension == 3) {
			for (int i = 0; i < multiPoint.numPoints(); i++) {
				Point point = multiPoint.getPoint(i);
				coordinates[i] = new double[3];

				coordinates[i][0] = point.x;
				coordinates[i][1] = point.y;
				coordinates[i][2] = point.z;
			}
		} else {
			for (int i = 0; i < multiPoint.numPoints(); i++) {
				Point point = multiPoint.getPoint(i);
				coordinates[i] = new double[2];

				coordinates[i][0] = point.x;
				coordinates[i][1] = point.y;
			}
		}

		return GeometryObject.createMultiPoint(coordinates, dimension, multiPoint.getSrid());	
	}

	@Override
	public GeometryObject getCurve(Object geomObj) throws SQLException {
		GeometryObject curve = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() != Geometry.LINESTRING)
				return null;

			curve = getCurve((LineString)geometry);
		}

		return curve;
	}

	private GeometryObject getCurve(LineString lineString) {
		return GeometryObject.createCurve(getCurveCoordinates(lineString), lineString.getDimension(), lineString.getSrid());
	}

	private double[] getCurveCoordinates(LineString lineString) {
		int dimension = lineString.getDimension();
		double[] coordinates = new double[lineString.numPoints() * dimension];
		int element = 0;

		if (dimension == 3) {
			for (Point point : lineString.getPoints()) {
				coordinates[element++] = point.x;
				coordinates[element++] = point.y;
				coordinates[element++] = point.z;
			}
		} else {
			for (Point point : lineString.getPoints()) {
				coordinates[element++] = point.x;
				coordinates[element++] = point.y;
			}
		}

		return coordinates;
	}

	@Override
	public GeometryObject getMultiCurve(Object geomObj) throws SQLException {
		GeometryObject multiCurve = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() == Geometry.MULTILINESTRING) {
				multiCurve = getMultiCurve((MultiLineString)geometry);
			}

			else if (geometry.getType() == Geometry.LINESTRING) {
				LineString lineStringObj = (LineString)geometry;
				double[][] coordiantes = new double[1][];
				coordiantes[0] = getCurveCoordinates(lineStringObj);

				multiCurve = GeometryObject.createMultiPoint(coordiantes, lineStringObj.getDimension(), lineStringObj.getSrid());
			}
		}

		return multiCurve;
	}

	private GeometryObject getMultiCurve(MultiLineString multiLineString) {
		double[][] coordinates = new double[multiLineString.numLines()][];
		int dimension = multiLineString.getDimension();

		for (int i = 0; i < multiLineString.numLines(); i++) {
			LineString lineString = multiLineString.getLine(i);
			coordinates[i] = new double[lineString.numPoints() * dimension];
			int element = 0;

			if (dimension == 3) {
				for (Point point : lineString.getPoints()) {
					coordinates[i][element++] = point.x;
					coordinates[i][element++] = point.y;
					coordinates[i][element++] = point.z;
				}
			} else {
				for (Point point : lineString.getPoints()) {
					coordinates[i][element++] = point.x;
					coordinates[i][element++] = point.y;
				}
			}
		}

		return GeometryObject.createMultiCurve(coordinates, dimension, multiLineString.getSrid());
	}	

	@Override
	public GeometryObject getPolygon(Object geomObj) throws SQLException {
		GeometryObject polygon = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() != Geometry.POLYGON)
				return null;

			polygon = getPolygon((Polygon)geometry);
		}

		return polygon;
	}

	private GeometryObject getPolygon(Polygon polygon) {
		return GeometryObject.createPolygon(getPolygonCoordinates(polygon), polygon.getDimension(), polygon.getSrid());
	}

	private double[][] getPolygonCoordinates(Polygon polygon) {
		double[][] coordinates = new double[polygon.numRings()][];
		int dimension = polygon.getDimension();

		for (int i = 0; i < polygon.numRings(); i++) {
			LinearRing ring = polygon.getRing(i);
			coordinates[i] = new double[ring.numPoints() * dimension];
			int element = 0;

			if (dimension == 3) {
				for (Point point : ring.getPoints()) {
					coordinates[i][element++] = point.x;
					coordinates[i][element++] = point.y;
					coordinates[i][element++] = point.z;
				}
			} else {
				for (Point point : ring.getPoints()) {
					coordinates[i][element++] = point.x;
					coordinates[i][element++] = point.y;
				}
			}
		}

		return coordinates;
	}

	@Override
	public GeometryObject getMultiPolygon(Object geomObj) throws SQLException {
		GeometryObject multiPolygon = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() == Geometry.MULTIPOLYGON) {
				multiPolygon = getMultiPolygon((MultiPolygon)geometry);
			}

			else if (geometry.getType() == Geometry.POLYGON) {
				Polygon polygonObj = (Polygon)geometry;
				double[][] coordinates = getPolygonCoordinates(polygonObj);
				int[] exteriorRings = new int[]{ 0 };

				multiPolygon = GeometryObject.createMultiPolygon(coordinates, exteriorRings, polygonObj.getDimension(), polygonObj.getSrid());
			}
		}

		return multiPolygon;
	}

	private GeometryObject getMultiPolygon(MultiPolygon multiPolygon) throws SQLException {
		int numRings = 0;
		for (Polygon polygon : multiPolygon.getPolygons())
			numRings += polygon.numRings();

		int[] exteriorRings = new int[multiPolygon.numPolygons()];
		double[][] coordinates = new double[numRings][];
		int dimension = multiPolygon.getDimension();

		int ringNo = 0;
		for (int i = 0; i < multiPolygon.numPolygons(); i++) {
			Polygon polygon = multiPolygon.getPolygon(i);
			exteriorRings[i] = ringNo;

			for (int j = 0; j < polygon.numRings(); j++, ringNo++) {
				LinearRing ring = polygon.getRing(j);
				coordinates[ringNo] = new double[ring.numPoints() * dimension];
				int element = 0;

				if (dimension == 3) {
					for (Point point : ring.getPoints()) {
						coordinates[ringNo][element++] = point.x;
						coordinates[ringNo][element++] = point.y;
						coordinates[ringNo][element++] = point.z;
					}
				} else {
					for (Point point : ring.getPoints()) {
						coordinates[ringNo][element++] = point.x;
						coordinates[ringNo][element++] = point.y;
					}
				}
			}
		}

		return GeometryObject.createMultiPolygon(coordinates, exteriorRings, dimension, multiPolygon.getSrid());
	}

	@Override
	public GeometryObject getGeometry(Object geomObj) throws SQLException {
		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			switch (geometry.getType()) {
			case Geometry.POINT:
				return getPoint((Point)geometry);
			case Geometry.MULTIPOINT:
				return getMultiPoint((MultiPoint)geometry);
			case Geometry.LINESTRING:
				return getCurve((LineString)geometry);
			case Geometry.MULTILINESTRING:
				return getMultiCurve((MultiLineString)geometry);
			case Geometry.POLYGON:
				return getPolygon((Polygon)geometry);
			case Geometry.MULTIPOLYGON:
				return getMultiPolygon((MultiPolygon)geometry);
			default:
				throw new SQLException("Cannot convert PostGIS geometry type '" + geometry.getType() + "' to internal representation: Unsupported type.");
			}
		}

		return null;
	}

	@Override
	public Object getDatabaseObject(GeometryObject geomObj, Connection connection) throws SQLException {
		Object geometry = null;

		switch (geomObj.getGeometryType()) {
		case POLYGON:
		case LINE_STRING:
		case POINT:
		case MULTI_LINE_STRING:
		case MULTI_POINT:
		case ENVELOPE:
		case MULTI_POLYGON:
			geometry = new PGgeometry(PGgeometry.geomFromString(convertToEWKT(geomObj)));
			break;
		case SOLID:
			// the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
			// thus, we return the EWKT only
			// TODO: rework as soon as the JDBC driver supports PolyhedralSurface
			geometry = convertToEWKT(geomObj);
			break;
		case COMPOSITE_SOLID:
			return null;
		}

		if (geometry == null)
			throw new SQLException("Failed to convert geometry to internal database representation.");

		return geometry;
	}

	private String convertToEWKT(GeometryObject geomObj) {
		double[][] coordinates = geomObj.getCoordinates();
		int dimension = geomObj.getDimension();

		StringBuilder ewkt = new StringBuilder()
		.append("SRID=").append(geomObj.getSrid()).append(";");

		switch (geomObj.getGeometryType()) {
		case POLYGON:
			ewkt.append("POLYGON");
			break;
		case LINE_STRING:
			ewkt.append("LINESTRING");
			break;
		case POINT:
			ewkt.append("POINT");
			break;
		case MULTI_LINE_STRING:
			ewkt.append("MULTILINESTRING");
			break;
		case MULTI_POINT:
			ewkt.append("MULTIPOINT");
			break;
		case ENVELOPE:
			ewkt.append("POLYGON");
			coordinates = new double[1][5 * dimension];
			int i = 0;

			if (dimension == 3) {
				coordinates[0][i++] = geomObj.getCoordinates()[0][0];
				coordinates[0][i++] = geomObj.getCoordinates()[0][1];
				coordinates[0][i++] = geomObj.getCoordinates()[0][2];

				coordinates[0][i++] = geomObj.getCoordinates()[0][3];
				coordinates[0][i++] = geomObj.getCoordinates()[0][1];
				coordinates[0][i++] = geomObj.getCoordinates()[0][2];

				coordinates[0][i++] = geomObj.getCoordinates()[0][3];
				coordinates[0][i++] = geomObj.getCoordinates()[0][4];
				coordinates[0][i++] = geomObj.getCoordinates()[0][5];

				coordinates[0][i++] = geomObj.getCoordinates()[0][0];
				coordinates[0][i++] = geomObj.getCoordinates()[0][4];
				coordinates[0][i++] = geomObj.getCoordinates()[0][5];

				coordinates[0][i++] = geomObj.getCoordinates()[0][0];
				coordinates[0][i++] = geomObj.getCoordinates()[0][1];
				coordinates[0][i++] = geomObj.getCoordinates()[0][2];
			} else {
				coordinates[0][i++] = geomObj.getCoordinates()[0][0];
				coordinates[0][i++] = geomObj.getCoordinates()[0][1];

				coordinates[0][i++] = geomObj.getCoordinates()[0][2];
				coordinates[0][i++] = geomObj.getCoordinates()[0][1];

				coordinates[0][i++] = geomObj.getCoordinates()[0][2];
				coordinates[0][i++] = geomObj.getCoordinates()[0][3];

				coordinates[0][i++] = geomObj.getCoordinates()[0][0];
				coordinates[0][i++] = geomObj.getCoordinates()[0][3];

				coordinates[0][i++] = geomObj.getCoordinates()[0][0];
				coordinates[0][i++] = geomObj.getCoordinates()[0][1];
			}

			break;
		case MULTI_POLYGON:
			// MultiPolyon is different due to its complex structure
			ewkt.append("MULTIPOLYGON");
			return convertPolygonCollectionToEWKT(geomObj, ewkt);
		case SOLID:
			// Solid is different due to its complex structure
			ewkt.append("POLYHEDRALSURFACE");
			return convertPolygonCollectionToEWKT(geomObj, ewkt);
		case COMPOSITE_SOLID:
			// CompositeSolids are not supported yet
			return null;
		}

		switch (geomObj.getGeometryType()) {
		case POLYGON:
		case MULTI_POINT:
		case MULTI_LINE_STRING:
		case ENVELOPE:
			ewkt.append("(");
			break;
		default:
			break;
		}

		for (int i = 0; i < coordinates.length; i++) {
			ewkt.append("(");

			for (int j = 0; j < coordinates[i].length; j += dimension) {
				for (int k = 0; k < dimension; k++) {
					ewkt.append(coordinates[i][j + k]);
					if (k < dimension - 1)
						ewkt.append(" ");
				}

				if (j < coordinates[i].length - dimension)
					ewkt.append(",");
			}

			ewkt.append(")");
			if (i < coordinates.length - 1)
				ewkt.append(",");
		}

		switch (geomObj.getGeometryType()) {
		case POLYGON:
		case MULTI_POINT:
		case MULTI_LINE_STRING:
		case ENVELOPE:
			ewkt.append(")");
			break;
		default:
			break;
		}

		return ewkt.toString();
	}

	private String convertPolygonCollectionToEWKT(GeometryObject geomObj, StringBuilder ewkt) {
		double[][] coordinates = geomObj.getCoordinates();
		int dimension = geomObj.getDimension();

		List<Integer> exteriorRings = new ArrayList<Integer>();
		for (int i = 0; i < geomObj.getNumElements(); i++)
			if (geomObj.getElementType(i) == ElementType.EXTERIOR_LINEAR_RING)
				exteriorRings.add(i);

		exteriorRings.add(coordinates.length);
		ewkt.append("(");

		for (int i = 0; i < exteriorRings.size() - 1; i++) {
			ewkt.append("(");

			for (int j = exteriorRings.get(i); j < exteriorRings.get(i + 1); j++) {
				ewkt.append("(");

				for (int k = 0; k < coordinates[j].length; k += dimension) {
					for (int l = 0; l < dimension; l++) {
						ewkt.append(coordinates[j][k + l]);
						if (l < dimension - 1)
							ewkt.append(" ");
					}

					if (k < coordinates[j].length - dimension)
						ewkt.append(",");
				}

				ewkt.append(")");
				if (j < exteriorRings.get(i + 1) - 1)
					ewkt.append(",");
			}

			ewkt.append(")");
			if (i < exteriorRings.size() - 2)
				ewkt.append(",");
		}

		ewkt.append(")");

		return ewkt.toString();
	}

}
