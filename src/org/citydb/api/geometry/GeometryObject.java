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
package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometryObject {

	public enum GeometryType {
		SOLID,
		COMPOSITE_SOLID,
		POLYGON,
		MULTI_POLYGON,
		CURVE,
		POINT,
		MULTI_CURVE,
		MULTI_POINT,
		ENVELOPE
	};

	public enum ElementType {
		SHELL,
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
				throw new IllegalArgumentException("Number of coordinate values of " + (i + 1) + ". element must at least match geometry dimension.");

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
		return createPolygonCollection(GeometryType.MULTI_POLYGON, coordinates, exteriorRings, dimension, srid);
	}
	
	public static GeometryObject createSolid(double[][] coordinates, int[] exteriorRings, int srid) {
		return createPolygonCollection(GeometryType.SOLID, coordinates, exteriorRings, 3, srid);
	}
	
	private static GeometryObject createPolygonCollection(GeometryType type, double[][] coordinates, int[] exteriorRings, int dimension, int srid) {
		if (exteriorRings.length > coordinates.length)
			throw new IllegalArgumentException("The number of exterior linear rings exceeds the number of coordinate arrays.");
		
		if (exteriorRings[0] != 0)
			throw new IllegalArgumentException("First geometry element must be an exterior linear ring.");
		
		GeometryObject geometryObject = new GeometryObject(type, dimension, srid);
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
	
	public static GeometryObject createCompositeSolid(GeometryObject[] solids, int srid) {
		if (solids == null || solids.length == 0)
			throw new IllegalArgumentException("No solid geometry objects provided.");
			
		int numElements = 0;
		for (GeometryObject solid : solids) {
			if (solid.getGeometryType() != GeometryType.SOLID)
				throw new IllegalArgumentException("Only solid geometry objects are allowed for constructing a composite solid.");

			numElements += solid.getNumElements();
		}
		
		GeometryObject geometryObject = new GeometryObject(GeometryType.COMPOSITE_SOLID, 3, srid);
		geometryObject.elementTypes = new ElementType[numElements + solids.length];
		geometryObject.coordinates = new double[numElements + solids.length][];
		
		int i = 0;
		for (GeometryObject solid : solids) {
			geometryObject.elementTypes[i] = ElementType.SHELL;
			geometryObject.coordinates[i++] = new double[0];
			
			for (int j = 0; j < solid.getNumElements(); j++) {
				geometryObject.elementTypes[i] = solid.getElementType(j);
				geometryObject.coordinates[i++] = solid.getCoordinates(j);
			}
		}
		
		return geometryObject;
	}
	
	private final GeometryType geometryType;
	private final int dimension;
	private int srid;
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

	public void setSrid(int srid) {
		this.srid = srid;
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
