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
package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometryObject {

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
		if (!bbox.isSetSrs())
			throw new IllegalArgumentException("The bounding box lacks a spatial reference system.");

		if (bbox.is3D()) {
			return createEnvelope(new double[]{
					bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(), bbox.getLowerCorner().getZ(), 
					bbox.getUpperCorner().getX(), bbox.getUpperCorner().getY(), bbox.getUpperCorner().getZ()}, 3, bbox.getSrs().getSrid());
		} else {
			return createEnvelope(new double[]{
					bbox.getLowerCorner().getX(), bbox.getLowerCorner().getY(), 
					bbox.getUpperCorner().getX(), bbox.getUpperCorner().getY()}, 2, bbox.getSrs().getSrid());
		}
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

		GeometryObject geometryObject = new GeometryObject(GeometryType.LINE_STRING, dimension, srid);
		geometryObject.elementTypes = new ElementType[]{ElementType.LINE_STRING};
		geometryObject.coordinates = new double[1][];
		geometryObject.coordinates[0] = coordinates;

		return geometryObject;
	}

	public static GeometryObject createMultiCurve(double[][] coordinates, int dimension, int srid) {
		GeometryObject geometryObject = new GeometryObject(GeometryType.MULTI_LINE_STRING, dimension, srid);
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

	public void changeSrid(int srid) {
		if (this.srid != srid)
			this.srid = srid;
	}

	public GeometryObject toEnvelope() {
		GeometryObject envelope = new GeometryObject(GeometryType.ENVELOPE, dimension, srid);
		envelope.elementTypes = new ElementType[]{ElementType.BOUNDING_RECTANGLE};
		envelope.coordinates = new double[1][];

		if (geometryType != GeometryType.POINT) {
			double bbox[] = dimension == 2 ? new double[]{Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE} :
				new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};

			for (int i = 0; i < elementTypes.length; i++) {
				if (elementTypes[i] == ElementType.INTERIOR_LINEAR_RING || elementTypes[i] == ElementType.SHELL)
					continue;

				double[] coords = coordinates[i];
				for (int j = 0; j < coords.length; j += dimension) {
					if (coords[j] < bbox[0])
						bbox[0] = coords[j];
					else if (coords[j] > bbox[dimension])
						bbox[dimension] = coords[j];

					if (coords[j + 1] < bbox[1])
						bbox[1] = coords[j + 1];
					else if (coords[j + 1] > bbox[dimension + 1])
						bbox[dimension + 1] = coords[j + 1];

					if (dimension == 3) {
						if (coords[j + 2] < bbox[2])
							bbox[2] = coords[j + 2];
						else if (coords[j + 2] > bbox[5])
							bbox[5] = coords[j + 2];
					}
				}
			}

			envelope.coordinates[0] = bbox;
		}
		
		else {
			envelope.coordinates[0] = dimension == 2 ? new double[]{coordinates[0][0], coordinates[0][1], coordinates[0][0], coordinates[0][1]} :
				new double[]{coordinates[0][0], coordinates[0][1], coordinates[0][2], coordinates[0][0], coordinates[0][1], coordinates[0][2]};
		}

		return envelope;
	}

}
