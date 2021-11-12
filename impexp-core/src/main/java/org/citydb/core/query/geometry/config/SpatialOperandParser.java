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
package org.citydb.core.query.geometry.config;

import org.citydb.config.geometry.*;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.query.geometry.GeometryParseException;

import java.util.ArrayList;
import java.util.List;

public class SpatialOperandParser {
    private final AbstractDatabaseAdapter databaseAdapter;

    public SpatialOperandParser(AbstractDatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
    }

    public GeometryObject parseOperand(AbstractGeometry operand) throws GeometryParseException {
        if (!operand.isValid()) {
            throw new GeometryParseException("The spatial " + operand.getGeometryType() + " operand is invalid.");
        }

        int dimension = operand.is3D() ? 3 : 2;
        DatabaseSrs referenceSystem = operand.isSetSrs() ?
                operand.getSrs() :
                databaseAdapter.getConnectionMetaData().getReferenceSystem();

        switch (operand.getGeometryType()) {
            case ENVELOPE:
                return parseEnvelope((BoundingBox) operand, referenceSystem, dimension);
            case POINT:
				return parsePoint((Point) operand, referenceSystem, dimension);
            case MULTI_POINT:
				return parseMultiPoint((MultiPoint) operand, referenceSystem, dimension);
            case LINE_STRING:
				return parseLineString((LineString) operand, referenceSystem, dimension);
            case MULTI_LINE_STRING:
				return parseMultiLineString((MultiLineString) operand, referenceSystem, dimension);
            case POLYGON:
				return parsePolygon((Polygon) operand, referenceSystem, dimension);
            case MULTI_POLYGON:
				return parseMultiPolygon((MultiPolygon) operand, referenceSystem, dimension);
			default:
                return null;
        }
    }

	private GeometryObject parseEnvelope(BoundingBox envelope, DatabaseSrs referenceSystem, int dimension) {
		if (dimension == 3) {
			return GeometryObject.createEnvelope(new double[]{
					envelope.getLowerCorner().getX(),
					envelope.getLowerCorner().getY(),
					envelope.getLowerCorner().getZ(),
					envelope.getUpperCorner().getX(),
					envelope.getUpperCorner().getY(),
					envelope.getUpperCorner().getZ()
			}, 3, referenceSystem.getSrid());
		} else {
			return GeometryObject.createEnvelope(new double[]{
					envelope.getLowerCorner().getX(),
					envelope.getLowerCorner().getY(),
					envelope.getUpperCorner().getX(),
					envelope.getUpperCorner().getY()
			}, 2, referenceSystem.getSrid());
		}
	}

    private GeometryObject parsePoint(Point point, DatabaseSrs referenceSystem, int dimension) {
		double[] coordinates = new double[dimension];
		coordinates[0] = point.getPos().getX();
		coordinates[1] = point.getPos().getY();
		if (dimension == 3) {
			coordinates[2] = point.getPos().getZ();
		}

		return GeometryObject.createPoint(coordinates, dimension, referenceSystem.getSrid());
    }

    private GeometryObject parseMultiPoint(MultiPoint multiPoint, DatabaseSrs referenceSystem, int dimension) {
		double[][] pointArray = new double[multiPoint.getPoints().size()][];
        for (int i = 0; i < multiPoint.getPoints().size(); i++) {
            Point point = multiPoint.getPoints().get(i);
			pointArray[i] = new double[dimension];
			pointArray[i][0] = point.getPos().getX();
			pointArray[i][1] = point.getPos().getY();
			if (dimension == 3) {
				pointArray[i][2] = point.getPos().getZ();
			}
        }

        return GeometryObject.createMultiPoint(pointArray, dimension, referenceSystem.getSrid());
    }

    private GeometryObject parseLineString(LineString lineString, DatabaseSrs referenceSystem, int dimension) throws GeometryParseException {
        return GeometryObject.createCurve(
				convertPrimitive(lineString.getPosList().getCoords(), dimension),
				dimension, referenceSystem.getSrid());
    }

    private GeometryObject parseMultiLineString(MultiLineString multiLineString, DatabaseSrs referenceSystem, int dimension) throws GeometryParseException {
        List<List<Double>> pointList = new ArrayList<>();
        for (LineString lineString : multiLineString.getLineStrings()) {
			pointList.add(lineString.getPosList().getCoords());
		}

        return GeometryObject.createMultiCurve(
				convertAggregate(pointList, dimension, false),
				dimension, referenceSystem.getSrid());
    }

    private GeometryObject parsePolygon(Polygon polygon, DatabaseSrs referenceSystem, int dimension) throws GeometryParseException {
        List<List<Double>> pointList = new ArrayList<>();
        pointList.add(polygon.getExterior().getCoords());
        if (polygon.isSetInterior()) {
            for (PositionList interior : polygon.getInterior()) {
				pointList.add(interior.getCoords());
			}
        }

        return GeometryObject.createPolygon(
				convertAggregate(pointList, dimension, true),
				dimension, referenceSystem.getSrid());
    }

    private GeometryObject parseMultiPolygon(MultiPolygon multiPolygon, DatabaseSrs referenceSystem, int dimension) throws GeometryParseException {
        List<List<Double>> pointList = new ArrayList<>();
        List<Integer> exteriorRings = new ArrayList<>();

        int exteriorRing = 0;
        for (Polygon polygon : multiPolygon.getPolygons()) {
            pointList.add(polygon.getExterior().getCoords());
            if (polygon.isSetInterior()) {
                exteriorRing += polygon.getInterior().size();
                for (PositionList interior : polygon.getInterior()) {
					pointList.add(interior.getCoords());
				}
            }

            exteriorRings.add(++exteriorRing);
        }

        int[] tmp = new int[exteriorRings.size()];
        for (int i = 0; i < exteriorRings.size(); i++) {
			tmp[i] = exteriorRings.get(i);
		}

        return GeometryObject.createMultiPolygon(
				convertAggregate(pointList, dimension, true),
				tmp, dimension, referenceSystem.getSrid());
    }

    private void validateRing(List<Double> coords, int dimension) throws GeometryParseException {
        if (coords == null || coords.isEmpty()) {
            throw new GeometryParseException("Ring has too few points.");
        }

        // check closedness
        int nrOfPoints = coords.size();
        boolean closed = true;
        for (int i = 0; i < dimension; i++) {
            if (!coords.get(i).equals(coords.get(nrOfPoints - dimension + i))) {
                closed = false;
                break;
            }
        }

        if (!closed) {
            // repair unclosed ring
            for (int i = 0; i < dimension; i++) {
                coords.add(coords.get(i));
            }
        }

        // check for minimum number of points
        if (coords.size() / dimension < 4) {
			throw new GeometryParseException("Ring has too few points.");
		}
    }

    private double[] convertPrimitive(List<Double> pointList, int dimension) throws GeometryParseException {
        int nrOfPoints = pointList.size();
        if (nrOfPoints % dimension != 0) {
            throw new GeometryParseException("Number of coordinates does not match dimension.");
        }

        double[] result = new double[nrOfPoints];
        for (int i = 0; i < nrOfPoints; i++) {
            result[i] = pointList.get(i);
        }

        return result;
    }

    private double[][] convertAggregate(List<List<Double>> pointList, int dimension, boolean validate) throws GeometryParseException {
        double[][] result = new double[pointList.size()][];
        for (int i = 0; i < pointList.size(); i++) {
            List<Double> points = pointList.get(i);
            if (validate) {
                validateRing(points, dimension);
            }

            result[i++] = convertPrimitive(points, dimension);
        }

        return result;
    }
}
