package de.tub.citydb.database.adapter.postgis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.database.adapter.AbstractGeometryConverterAdapter;

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

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
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

			envelope = GeometryObject.createEnvelope(coordinates, 3, geometry.getSrid());
		}

		return envelope;
	}

	@Override
	public GeometryObject getPoint(Object geomObj) throws SQLException {
		GeometryObject point = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() != Geometry.POINT)
				return null;

			point = GeometryObject.createPoint(getPointCoordinates((Point)geometry), geometry.getDimension(), geometry.getSrid());
		}

		return point;
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
				MultiPoint multiPointObj = (MultiPoint)geometry;
				double[][] coordinates = new double[multiPointObj.numPoints()][];
				int dimension = multiPointObj.getDimension();

				if (dimension == 3) {
					for (int i = 0; i < multiPointObj.numPoints(); i++) {
						Point point = multiPointObj.getPoint(i);
						coordinates[i] = new double[3];

						coordinates[i][0] = point.x;
						coordinates[i][1] = point.y;
						coordinates[i][2] = point.z;
					}
				} else {
					for (int i = 0; i < multiPointObj.numPoints(); i++) {
						Point point = multiPointObj.getPoint(i);
						coordinates[i] = new double[2];

						coordinates[i][0] = point.x;
						coordinates[i][1] = point.y;
					}
				}

				multiPoint = GeometryObject.createMultiPoint(coordinates, dimension, multiPointObj.getSrid());			
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

	@Override
	public GeometryObject getCurve(Object geomObj) throws SQLException {
		GeometryObject curve = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() != Geometry.LINESTRING)
				return null;

			curve = GeometryObject.createCurve(getCurveCoordinates((LineString)geometry), geometry.getDimension(), geometry.getSrid());
		}

		return curve;
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
				MultiLineString multiLineStringObj = (MultiLineString)geometry;
				double[][] coordinates = new double[multiLineStringObj.numLines()][];
				int dimension = multiLineStringObj.getDimension();

				for (int i = 0; i < multiLineStringObj.numLines(); i++) {
					LineString lineString = multiLineStringObj.getLine(i);
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

				multiCurve = GeometryObject.createMultiCurve(coordinates, dimension, multiLineStringObj.getSrid());
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

	@Override
	public GeometryObject getPolygon(Object geomObj) throws SQLException {
		GeometryObject polygon = null;

		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			if (geometry.getType() != Geometry.POLYGON)
				return null;

			Polygon polygonObj = (Polygon)geometry;
			double[][] coordinates = new double[polygonObj.numRings()][];
			int dimension = polygonObj.getDimension();

			for (int i = 0; i < polygonObj.numRings(); i++) {
				LinearRing ring = polygonObj.getRing(i);
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

			polygon = GeometryObject.createPolygon(coordinates, dimension, polygonObj.getSrid());
		}

		return polygon;
	}

	@Override
	public GeometryObject getGeometry(Object geomObj) throws SQLException {
		if (geomObj instanceof PGgeometry) {
			Geometry geometry = ((PGgeometry)geomObj).getGeometry();
			switch (geometry.getType()) {
			case Geometry.POINT:
				return getPoint(geometry);
			case Geometry.MULTIPOINT:
				return getMultiPoint(geometry);
			case Geometry.LINESTRING:
				return getCurve(geometry);
			case Geometry.MULTILINESTRING:
				return getMultiCurve(geometry);
			case Geometry.POLYGON:
				return getPolygon(geometry);
			}
		}

		return null;
	}

	@Override
	public Object getDatabaseObject(GeometryObject geomObj, Connection connection) throws SQLException {
		return new PGgeometry(PGgeometry.geomFromString(convertToEWKT(geomObj)));
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
		case CURVE:
			ewkt.append("LINESTRING");
			break;
		case POINT:
			ewkt.append("POINT");
			break;
		case MULTI_CURVE:
			ewkt.append("MULTILINESTRING");
			break;
		case MULTI_POINT:
			ewkt.append("MULTIPOINT");
			break;
		case ENVELOPE:
			ewkt.append("POLYGON");
			coordinates = new double[1][5 * dimension];
			int i = 0;

			coordinates[0][i++] = geomObj.getCoordinates()[0][0];
			coordinates[0][i++] = geomObj.getCoordinates()[0][1];
			if (dimension == 3)
				coordinates[0][i++] = geomObj.getCoordinates()[0][2];

			coordinates[0][i++] = geomObj.getCoordinates()[0][3];
			coordinates[0][i++] = geomObj.getCoordinates()[0][1];
			if (dimension == 3)
				coordinates[0][i++] = geomObj.getCoordinates()[0][2];

			coordinates[0][i++] = geomObj.getCoordinates()[0][3];
			coordinates[0][i++] = geomObj.getCoordinates()[0][4];
			if (dimension == 3)
				coordinates[0][i++] = geomObj.getCoordinates()[0][5];

			coordinates[0][i++] = geomObj.getCoordinates()[0][0];
			coordinates[0][i++] = geomObj.getCoordinates()[0][4];
			if (dimension == 3)
				coordinates[0][i++] = geomObj.getCoordinates()[0][5];

			coordinates[0][i++] = geomObj.getCoordinates()[0][0];
			coordinates[0][i++] = geomObj.getCoordinates()[0][1];
			if (dimension == 3)
				coordinates[0][i++] = geomObj.getCoordinates()[0][2];

			break;
		}
		
		switch (geomObj.getGeometryType()) {
		case POLYGON:
		case MULTI_POINT:
		case MULTI_CURVE:
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

				if (j < coordinates[i].length - 3)
					ewkt.append(",");
			}

			ewkt.append(")");
			if (i < coordinates.length - 1)
				ewkt.append(",");
		}

		switch (geomObj.getGeometryType()) {
		case POLYGON:
		case MULTI_POINT:
		case MULTI_CURVE:
		case ENVELOPE:
			ewkt.append(")");
			break;
		default:
			break;
		}
			
		return ewkt.toString();
	}

}
