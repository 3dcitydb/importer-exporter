package de.tub.citydb.database.adapter.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.database.adapter.AbstractGeometryConverterAdapter;

public class GeometryConverterAdapter extends AbstractGeometryConverterAdapter {

	protected GeometryConverterAdapter() {

	}

	@Override
	public int getNullGeometryType() {
		return Types.STRUCT;
	}

	@Override
	public String getNullGeometryTypeName() {
		return "MDSYS.SDO_GEOMETRY";
	}

	@Override
	public GeometryObject getEnvelope(Object geomObj) throws SQLException {
		GeometryObject envelope = null;

		if (geomObj instanceof STRUCT) {
			JGeometry geometry = JGeometry.load((STRUCT)geomObj);
			double[] ordinates = geometry.getMBR();
			double[] coordinates;

			if (geometry.getDimensions() == 3)
				coordinates = new double[]{ordinates[0], ordinates[1], ordinates[2], ordinates[3], ordinates[4], ordinates[5]};
			else 
				coordinates = new double[]{ordinates[0], ordinates[1], 0, ordinates[2], ordinates[3], 0};

			envelope = GeometryObject.createEnvelope(coordinates, 3, geometry.getSRID());
		}

		return envelope;
	}

	@Override
	public GeometryObject getPoint(Object geomObj) throws SQLException {
		if (geomObj instanceof STRUCT)
			return getPoint(JGeometry.load((STRUCT)geomObj));

		return null;
	}

	private GeometryObject getPoint(JGeometry geometry) {
		if (geometry.getType() == JGeometry.GTYPE_POINT) 
			return GeometryObject.createPoint(geometry.getPoint(), geometry.getDimensions(), geometry.getSRID());

		return null;
	}

	@Override
	public GeometryObject getMultiPoint(Object geomObj) throws SQLException {
		if (geomObj instanceof STRUCT)
			return getMultiPoint(JGeometry.load((STRUCT)geomObj));

		return null;
	}

	private GeometryObject getMultiPoint(JGeometry geometry) {
		if (geometry.getType() == JGeometry.GTYPE_MULTIPOINT) {
			double[] ordinates = geometry.getOrdinatesArray();
			int dimension = geometry.getDimensions();

			double[][] coordinates = new double[ordinates.length / dimension][];
			for (int i = 0, ordinate = 0; i < coordinates.length; i++) {
				coordinates[i] = new double[dimension];
				for (int j = 0; j < dimension; ordinate++, j++)
					coordinates[i][j] = ordinates[ordinate];
			}

			return GeometryObject.createMultiPoint(coordinates, dimension, geometry.getSRID());
		}

		else if (geometry.getType() == JGeometry.GTYPE_POINT) {
			double[][] coordinates = new double[1][];				
			coordinates[0] = geometry.getPoint();

			return GeometryObject.createMultiPoint(coordinates, geometry.getDimensions(), geometry.getSRID());
		}

		return null;
	}

	@Override
	public GeometryObject getCurve(Object geomObj) throws SQLException {
		if (geomObj instanceof STRUCT)
			return getCurve(JGeometry.load((STRUCT)geomObj));

		return null;
	}

	private GeometryObject getCurve(JGeometry geometry) {
		if (geometry.getType() == JGeometry.GTYPE_CURVE)
			return GeometryObject.createCurve(geometry.getOrdinatesArray(), geometry.getDimensions(), geometry.getSRID());

		return null;
	}

	@Override
	public GeometryObject getMultiCurve(Object geomObj) throws SQLException {
		if (geomObj instanceof STRUCT)
			return getMultiCurve(JGeometry.load((STRUCT)geomObj));

		return null;
	}

	private GeometryObject getMultiCurve(JGeometry geometry) {
		if (geometry.getType() == JGeometry.GTYPE_MULTICURVE) {
			int[] elemInfo = geometry.getElemInfo();
			double[] ordinates = geometry.getOrdinatesArray();
			if (elemInfo.length < 3)
				return null;

			int[] curveLimits = new int[elemInfo.length / 3];
			curveLimits[curveLimits.length - 1] = ordinates.length;
			for (int i = 3, j = 0; i < elemInfo.length; i += 3, j++)
				curveLimits[j] = elemInfo[i] - 1;

			double[][] coordinates = new double[curveLimits.length][];			
			for (int i = 0, ordinate = 0; i < curveLimits.length; i++) {
				coordinates[i] = new double[curveLimits[i] - ordinate];
				for (int j = 0; ordinate < curveLimits[i]; ordinate++, j++)
					coordinates[i][j] = ordinates[ordinate];
			}

			return GeometryObject.createMultiCurve(coordinates, geometry.getDimensions(), geometry.getSRID());
		}

		else if (geometry.getType() == JGeometry.GTYPE_CURVE) {
			double[][] coordinates = new double[1][];
			coordinates[0] = geometry.getOrdinatesArray();

			return GeometryObject.createMultiCurve(coordinates, geometry.getDimensions(), geometry.getSRID());
		}

		return null;
	}

	@Override
	public GeometryObject getPolygon(Object geomObj) throws SQLException {
		if (geomObj instanceof STRUCT)
			return getPolygon(JGeometry.load((STRUCT)geomObj));

		return null;
	}

	private GeometryObject getPolygon(JGeometry geometry) {
		if (geometry.getType() == JGeometry.GTYPE_POLYGON) {
			int[] elemInfo = geometry.getElemInfo();
			double[] ordinates = geometry.getOrdinatesArray();
			if (elemInfo.length < 3)
				return null;

			int[] ringLimits = new int[elemInfo.length / 3];
			ringLimits[ringLimits.length - 1] = ordinates.length;
			for (int i = 3, j = 0; i < elemInfo.length; i += 3, j++)
				ringLimits[j] = elemInfo[i] - 1;

			double[][] coordinates = new double[ringLimits.length][];			
			for (int i = 0, ordinate = 0; i < ringLimits.length; i++) {
				coordinates[i] = new double[ringLimits[i] - ordinate];
				for (int j = 0; ordinate < ringLimits[i]; ordinate++, j++)
					coordinates[i][j] = ordinates[ordinate];
			}

			return GeometryObject.createPolygon(coordinates, geometry.getDimensions(), geometry.getSRID());
		}

		return null;
	}

	@Override
	public GeometryObject getGeometry(Object geomObj) throws SQLException {
		if (geomObj instanceof STRUCT) {
			JGeometry geometry = JGeometry.load((STRUCT)geomObj);
			switch (geometry.getType()) {
			case JGeometry.GTYPE_POINT:
				return getPoint(geometry);
			case JGeometry.GTYPE_MULTIPOINT:
				return getMultiPoint(geometry);
			case JGeometry.GTYPE_CURVE:
				return getCurve(geometry);
			case JGeometry.GTYPE_MULTICURVE:
				return getMultiCurve(geometry);
			case JGeometry.GTYPE_POLYGON:
				return getPolygon(geometry);
			}
		}

		return null;
	}

	@Override
	public Object getDatabaseObject(GeometryObject geomObj, Connection connection) throws SQLException {
		JGeometry geometry = null;
		
		switch (geomObj.getGeometryType()) {
		case POLYGON:
			geometry = convertPolygonToJGeometry(geomObj);
			break;
		case CURVE:
			geometry = convertCurveToJGeometry(geomObj);
			break;
		case POINT:
			geometry = convertPointToJGeometry(geomObj);
			break;
		case MULTI_CURVE:
			geometry = convertMultiCurveToJGeometry(geomObj);
			break;
		case MULTI_POINT:
			geometry = convertMultiPointToJGeometry(geomObj);
			break;
		case ENVELOPE:
			geometry = convertEnvelopeToJGeometry(geomObj);
			break;
		}
		
		if (geometry == null)
			throw new SQLException("Failed to convert geometry to internal database representation.");

		return SyncJGeometry.syncStore(geometry, connection);
	}
	
	private JGeometry convertPointToJGeometry(GeometryObject geomObj) {
		return JGeometry.createPoint(geomObj.getCoordinates(0), geomObj.getDimension(), geomObj.getSrid());
	}
	
	private JGeometry convertMultiPointToJGeometry(GeometryObject geomObj) {
		Object[] objectArray = new Object[geomObj.getNumElements()];
		for (int i = 0; i < geomObj.getNumElements(); i++)
			objectArray[i] = geomObj.getCoordinates(i);

		return JGeometry.createMultiPoint(objectArray, geomObj.getDimension(), geomObj.getSrid());
	}
	
	private JGeometry convertCurveToJGeometry(GeometryObject geomObj) {
		return JGeometry.createLinearLineString(geomObj.getCoordinates(0), geomObj.getDimension(), geomObj.getSrid());
	}
	
	private JGeometry convertMultiCurveToJGeometry(GeometryObject geomObj) {
		Object[] objectArray = new Object[geomObj.getNumElements()];
		for (int i = 0; i < geomObj.getNumElements(); i++)
			objectArray[i] = geomObj.getCoordinates(i);

		return JGeometry.createLinearMultiLineString(objectArray, geomObj.getDimension(), geomObj.getSrid());
	}
	
	private JGeometry convertPolygonToJGeometry(GeometryObject geomObj) {
		if (geomObj.getNumElements() == 1) {
			return JGeometry.createLinearPolygon(geomObj.getCoordinates(0), geomObj.getDimension(), geomObj.getSrid());
		} else {
			Object[] objectArray = new Object[geomObj.getNumElements()];
			for (int i = 0; i < geomObj.getNumElements(); i++)
				objectArray[i] = geomObj.getCoordinates(i);

			return JGeometry.createLinearPolygon(objectArray, geomObj.getDimension(), geomObj.getSrid());
		} 
	}

	private JGeometry convertEnvelopeToJGeometry(GeometryObject geomObj) {
		int dimension = geomObj.getDimension();
		double[] coordinates = new double[5 * dimension];
		int i = 0;

		coordinates[i++] = geomObj.getCoordinates()[0][0];
		coordinates[i++] = geomObj.getCoordinates()[0][1];
		if (dimension == 3)
			coordinates[i++] = geomObj.getCoordinates()[0][2];

		coordinates[i++] = geomObj.getCoordinates()[0][3];
		coordinates[i++] = geomObj.getCoordinates()[0][1];
		if (dimension == 3)
			coordinates[i++] = geomObj.getCoordinates()[0][2];

		coordinates[i++] = geomObj.getCoordinates()[0][3];
		coordinates[i++] = geomObj.getCoordinates()[0][4];
		if (dimension == 3)
			coordinates[i++] = geomObj.getCoordinates()[0][5];

		coordinates[i++] = geomObj.getCoordinates()[0][0];
		coordinates[i++] = geomObj.getCoordinates()[0][4];
		if (dimension == 3)
			coordinates[i++] = geomObj.getCoordinates()[0][5];

		coordinates[i++] = geomObj.getCoordinates()[0][0];
		coordinates[i++] = geomObj.getCoordinates()[0][1];
		if (dimension == 3)
			coordinates[i++] = geomObj.getCoordinates()[0][2];
		
		return JGeometry.createLinearPolygon(coordinates, geomObj.getDimension(), geomObj.getSrid());
	}
	
}
