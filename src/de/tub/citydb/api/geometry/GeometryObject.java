package de.tub.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometryObject {

	public enum GeometryType {
		POLYGON,
		MULTI_POLYGON,
		CURVE,
		POINT,
		MULTI_CURVE,
		MULTI_POINT,
		ENVELOPE
	};

	public enum ElementType {
		EXTERIOR_LINEAR_RING,
		INTERIOR_LINEAR_RING,
		LINE_STRING,
		POINT,
		BOUNDING_RECTANGLE
	};

	public static GeometryObject createEnvelope(double[] coordinates, int dimension, int srid) {
		GeometryObject geometryObject = new GeometryObject(GeometryType.ENVELOPE, dimension, srid);
		geometryObject.elementTypes = new ElementType[]{ElementType.BOUNDING_RECTANGLE};
		geometryObject.coordinates = new double[1][];

		if (coordinates.length == dimension * 2) {
			geometryObject.coordinates[0] = coordinates;
		} else {
			geometryObject.coordinates[0] = new double[dimension * 2];
			for (int i = 0; i < geometryObject.coordinates[0].length; i++)
				geometryObject.coordinates[0][i] = (coordinates.length > i) ? coordinates[i] : 0;
		}

		return geometryObject;
	}

	public static GeometryObject createEnvelope(BoundingBox bbox) {
		return createEnvelope(new double[]{bbox.getLowerLeftCorner().getX(), bbox.getLowerLeftCorner().getY(), bbox.getUpperRightCorner().getX(), bbox.getUpperRightCorner().getY()}, 2, bbox.getSrs().getSrid());
	}

	public static GeometryObject createPoint(double[] coordinates, int dimension, int srid) {
		if (coordinates.length != dimension)
			throw new IllegalArgumentException("Number of coordinate values does not match geometry dimension.");
		
		GeometryObject geometryObject = new GeometryObject(GeometryType.POINT, dimension, srid);
		geometryObject.elementTypes = new ElementType[]{ElementType.POINT};
		geometryObject.coordinates = new double[1][];
		geometryObject.coordinates[0] = coordinates;

		return geometryObject;
	}

	public static GeometryObject createMultiPoint(double[][] coordinates, int dimension, int srid) {
		GeometryObject geometryObject = new GeometryObject(GeometryType.MULTI_POINT, dimension, srid);
		geometryObject.elementTypes = new ElementType[coordinates.length];
		geometryObject.coordinates = coordinates;

		for (int i = 0; i < geometryObject.elementTypes.length; i++) {
			if (coordinates[i].length != dimension)
				throw new IllegalArgumentException("Number of coordinate values of " + (i + 1) + ". element does not match geometry dimension.");

			geometryObject.elementTypes[i] = ElementType.POINT;
		}

		return geometryObject;
	}

	public static GeometryObject createCurve(double[] coordinates, int dimension, int srid) {
		if (coordinates.length < dimension)
			throw new IllegalArgumentException("Number of coordinate values must at least match geometry dimension.");

		GeometryObject geometryObject = new GeometryObject(GeometryType.CURVE, dimension, srid);
		geometryObject.elementTypes = new ElementType[]{ElementType.LINE_STRING};
		geometryObject.coordinates = new double[1][];
		geometryObject.coordinates[0] = coordinates;

		return geometryObject;
	}

	public static GeometryObject createMultiCurve(double[][] coordinates, int dimension, int srid) {
		GeometryObject geometryObject = new GeometryObject(GeometryType.MULTI_CURVE, dimension, srid);
		geometryObject.elementTypes = new ElementType[coordinates.length];
		geometryObject.coordinates = coordinates;

		for (int i = 0; i < geometryObject.elementTypes.length; i++) {
			if (coordinates[i].length < dimension)
				throw new IllegalArgumentException("Number of coordinate values of " + (i + 1) + ". element must at least match geometry dimension.");

			geometryObject.elementTypes[i] = ElementType.LINE_STRING;
		}

		return geometryObject;
	}
	
	public static GeometryObject createPolygon(double[] coordinates, int dimension, int srid) {
		if (coordinates.length < 4 * dimension)
			throw new IllegalArgumentException("The exterior linear ring must contain at least four coordinate tuples.");

		GeometryObject geometryObject = new GeometryObject(GeometryType.POLYGON, dimension, srid);
		geometryObject.elementTypes = new ElementType[]{ElementType.EXTERIOR_LINEAR_RING};
		geometryObject.coordinates = new double[1][];
		geometryObject.coordinates[0] = coordinates;
		
		return geometryObject;
	}

	public static GeometryObject createPolygon(double[][] coordinates, int dimension, int srid) {
		GeometryObject geometryObject = new GeometryObject(GeometryType.POLYGON, dimension, srid);
		geometryObject.elementTypes = new ElementType[coordinates.length];
		geometryObject.coordinates = coordinates;

		for (int i = 0; i < geometryObject.elementTypes.length; i++) {
			if (coordinates[i].length < 4 * dimension)
				throw new IllegalArgumentException("The " + (i + 1) + ". linear ring must contain at least four coordinate tuples.");

			geometryObject.elementTypes[i] = i == 0 ? ElementType.EXTERIOR_LINEAR_RING : ElementType.INTERIOR_LINEAR_RING;
		}

		return geometryObject;
	}
	
	public static GeometryObject createMultiPolygon(double[][] coordinates, int[] exteriorRings, int dimension, int srid) {
		if (exteriorRings.length > coordinates.length)
			throw new IllegalArgumentException("The number of exterior linear rings exceeds the number of coordinate arrays.");
				
		GeometryObject geometryObject = new GeometryObject(GeometryType.MULTI_POLYGON, dimension, srid);
		geometryObject.elementTypes = new ElementType[coordinates.length];
		geometryObject.coordinates = coordinates;
		
		for (int i = 0; i < geometryObject.elementTypes.length; i++) {
			if (coordinates[i].length < 4 * dimension)
				throw new IllegalArgumentException("The " + (i + 1) + ". linear ring must contain at least four coordinate tuples.");
			
			geometryObject.elementTypes[i] = ElementType.INTERIOR_LINEAR_RING;
		}
		
		for (int i = 0; i < exteriorRings.length; i++) {
			if (exteriorRings[i] >= coordinates.length)
				throw new IllegalArgumentException("The " + (i + 1) + ". exterior linear ring is not backed by a coordinate array.");
			
			geometryObject.elementTypes[i] = ElementType.EXTERIOR_LINEAR_RING;			
		}

		return geometryObject;
	}
	
	private final GeometryType geometryType;
	private final int dimension;
	private final int srid;
	private ElementType[] elementTypes;
	private double[][] coordinates;

	private GeometryObject(GeometryType geometryType, int dimension, int srid) {
		this.geometryType = geometryType;		
		this.dimension = dimension;
		this.srid = srid;
	}

	public GeometryType getGeometryType() {
		return geometryType;
	}

	public int getDimension() {
		return dimension;
	}

	public int getSrid() {
		return srid;
	}

	public int getNumElements() {
		return elementTypes.length;
	}
	
	public int getNumCoordinates() {
		int size = 0;
		for (double[] element : coordinates)
			size += element.length;
		
		return size;
	}

	public ElementType getElementType(int i) {
		return elementTypes[i];
	}
	
	public double[][] getCoordinates() {
		return coordinates;
	}

	public double[] getCoordinates(int i) {
		return coordinates[i];
	}

	public List<Double> getCoordinatesAsList(int i) {
		List<Double> coordinates = new ArrayList<Double>(this.coordinates[i].length);
		for (double coordinate : this.coordinates[i])
			coordinates.add(coordinate);

		return coordinates;
	}
}
