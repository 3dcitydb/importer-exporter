/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.query.geometry.gml;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.query.filter.selection.operator.spatial.SpatialOperandName;
import org.citydb.query.geometry.DatabaseSrsParser;
import org.citydb.query.geometry.GeometryParseException;
import org.citydb.query.geometry.SrsParseException;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.geometry.BoundingBox;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.gml.GML;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.SRSReferenceGroup;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiLineString;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurve;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurveSegment;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurfacePatch;
import org.citygml4j.model.gml.geometry.primitives.Curve;
import org.citygml4j.model.gml.geometry.primitives.CurveArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringProperty;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableCurve;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRep;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRepOrCoord;
import org.citygml4j.model.gml.geometry.primitives.Rectangle;
import org.citygml4j.model.gml.geometry.primitives.Ring;
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Surface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class SimpleGMLParser {
	private final JAXBUnmarshaller unmarshaller;
	private final DatabaseSrsParser srsNameParser;

	public SimpleGMLParser(JAXBUnmarshaller unmarshaller, DatabaseSrsParser srsNameHandler) {
		this.unmarshaller = unmarshaller;
		this.srsNameParser = srsNameHandler;
	}

	public GeometryObject parseGeometry(JAXBElement<?> geometry) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;

		try {
			ModelObject object = unmarshaller.unmarshal(geometry);
			if (object instanceof GML)
				geometryObject = parseGeometry((GML)object, geometry.getName());
		} catch (MissingADESchemaException e) {
			throw new GeometryParseException("Failed to parse GML geometry.", e);
		}

		return geometryObject;
	}
	
	public GeometryObject parseGeometry(Node geometry) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		
		try {
			ModelObject object = unmarshaller.unmarshal(geometry);
			if (object instanceof GML)
				geometryObject = parseGeometry((GML)object, new QName(geometry.getNamespaceURI(), geometry.getLocalName()));
		} catch (MissingADESchemaException e) {
			throw new GeometryParseException("Failed to parse GML geometry.", e);
		}
		
		return geometryObject;
	}

	private GeometryObject parseGeometry(GML gml, QName name) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;

		// geometry is not advertised
		if (!SpatialOperandName.contains(gml.getGMLClass()))
			throw new GeometryParseException("The GML geometry type '" + name + "' is not supported as geometric value.");

		switch (gml.getGMLClass()) {
		case ENVELOPE:
			geometryObject = parseEnvelope((Envelope)gml);
			break;
		case POINT:
			geometryObject = parsePoint((Point)gml);
			break;
		case MULTI_POINT:
			geometryObject = parseMultiPoint((MultiPoint)gml);
			break;
		case LINE_STRING:
			geometryObject = parseLineString((LineString)gml);
			break;
		case MULTI_LINE_STRING:
			geometryObject = parseMultiLineString((MultiLineString)gml);
			break;
		case CURVE:
			geometryObject = parseCurve((Curve)gml);
			break;
		case MULTI_CURVE:
			geometryObject = parseMultiCurve((MultiCurve)gml);
			break;
		case POLYGON:
			geometryObject = parsePolygon((Polygon)gml);
			break;
		case MULTI_POLYGON:
			geometryObject = parseMultiPolygon((MultiPolygon)gml);
			break;
		case SURFACE:
			geometryObject = parseSurface((Surface)gml);
			break;
		case MULTI_SURFACE:
			geometryObject = parseMultiSurface((MultiSurface)gml);
			break;
		default:
			break;
		}

		if (geometryObject == null)
			throw new GeometryParseException("Failed to parse the geometry element '" + name + "'.");

		return geometryObject;
	}

	private GeometryObject parseEnvelope(Envelope envelope) throws SrsParseException {
		GeometryObject geometryObject;

		BoundingBox bbox = envelope.toBoundingBox();
		DatabaseSrs targetSrs = envelope.isSetSrsName() ? srsNameParser.getDatabaseSrs(envelope.getSrsName()) : srsNameParser.getDefaultSrs();

		// we assume the dim of the target SRS as default value
		int dimension = envelope.isSetSrsDimension() ? envelope.getSrsDimension() : (targetSrs.is3D() ? 3 : 2);

		if (dimension == 2) {
			double[] coordinates = new double[4];
			coordinates[0] = bbox.getLowerCorner().getX();
			coordinates[1] = bbox.getLowerCorner().getY();
			coordinates[2] = bbox.getUpperCorner().getX();
			coordinates[3] = bbox.getUpperCorner().getY();
			geometryObject = GeometryObject.createEnvelope(coordinates, dimension, targetSrs.getSrid());
		} else {
			double[] coordinates = new double[6];
			coordinates[0] = bbox.getLowerCorner().getX();
			coordinates[1] = bbox.getLowerCorner().getY();
			coordinates[2] = bbox.getLowerCorner().getZ();
			coordinates[3] = bbox.getUpperCorner().getX();
			coordinates[4] = bbox.getUpperCorner().getY();
			coordinates[5] = bbox.getUpperCorner().getZ();
			geometryObject = GeometryObject.createEnvelope(coordinates, dimension, targetSrs.getSrid());
		}

		return geometryObject;
	}

	private GeometryObject parsePoint(Point point) throws SrsParseException {
		GeometryObject geometryObject = null;
		DatabaseSrs targetSrs = point.isSetSrsName() ? srsNameParser.getDatabaseSrs(point.getSrsName()) : srsNameParser.getDefaultSrs();

		// we assume the dim of the target SRS as default value
		int dimension = point.isSetSrsDimension() ? point.getSrsDimension() : (targetSrs.is3D() ? 3 : 2);

		if (point.isSetPos() && point.getPos().isSetSrsDimension() && point.getPos().getSrsDimension() == 3)
			dimension = 3;

		List<Double> values = point.toList3d();
		if (values != null && !values.isEmpty())
			geometryObject = GeometryObject.createPoint(convertPrimitive(values, dimension), dimension, targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseMultiPoint(MultiPoint multiPoint) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		DatabaseSrs targetSrs = multiPoint.isSetSrsName() ? srsNameParser.getDatabaseSrs(multiPoint.getSrsName()) : srsNameParser.getDefaultSrs();

		// we assume the dim of the target SRS as default value
		int dimension = multiPoint.isSetSrsDimension() ? multiPoint.getSrsDimension() : (targetSrs.is3D() ? 3 : 2);
		List<List<Double>> pointList = new ArrayList<>();

		if (multiPoint.isSetPointMember()) {
			for (PointProperty pointProperty : multiPoint.getPointMember()) {
				if (pointProperty.isSetPoint()) {
					Point point = pointProperty.getPoint();
					if (point.isSetPos() && point.getPos().isSetSrsDimension() && point.getPos().getSrsDimension() == 3)
						dimension = 3;

					pointList.add(point.toList3d());
				} else
					throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
			}

		} else if (multiPoint.isSetPointMembers()) {
			PointArrayProperty pointArrayProperty = multiPoint.getPointMembers();
			for (Point point : pointArrayProperty.getPoint()) {
				if (point.isSetPos() && point.getPos().isSetSrsDimension() && point.getPos().getSrsDimension() == 3)
					dimension = 3;

				pointList.add(point.toList3d());
			}
		}

		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createMultiPoint(convertAggregate(pointList, dimension), dimension, targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseLineString(LineString lineString) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(lineString);

		List<Double> pointList = new ArrayList<>();
		generatePointList(lineString, pointList, dimInfo, false);
		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createCurve(convertPrimitive(pointList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseMultiLineString(MultiLineString multiLineString) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiLineString);

		List<List<Double>> pointList = new ArrayList<>();
		for (LineStringProperty lineStringProperty : multiLineString.getLineStringMember()) {
			if (lineStringProperty.isSetLineString()) {
				List<Double> points = new ArrayList<>();
				generatePointList(lineStringProperty.getLineString(), points, dimInfo, false);

				if (!points.isEmpty())
					pointList.add(points);
			} else
				throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
		}

		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createMultiCurve(convertAggregate(pointList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseCurve(Curve curve) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(curve);

		List<Double> pointList = new ArrayList<>();
		generatePointList(curve, pointList, dimInfo, false);
		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createCurve(convertPrimitive(pointList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseMultiCurve(MultiCurve multiCurve) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiCurve);

		List<List<Double>> pointList = new ArrayList<>();
		if (multiCurve.isSetCurveMember()) {
			for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
				if (curveProperty.isSetCurve()) {
					AbstractCurve curve = curveProperty.getCurve();
					List<Double> points = new ArrayList<>();
					generatePointList(curve, points, dimInfo, false);

					if (!points.isEmpty())
						pointList.add(points);
				} else
					throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
			}
		} 

		else if (multiCurve.isSetCurveMembers()) {
			CurveArrayProperty curveArrayProperty = multiCurve.getCurveMembers();
			for (AbstractCurve curve : curveArrayProperty.getCurve()) {
				List<Double> points = new ArrayList<>();
				generatePointList(curve, points, dimInfo, false);

				if (!points.isEmpty())
					pointList.add(points);
			}
		}

		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createMultiCurve(convertAggregate(pointList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parsePolygon(Polygon polygon) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(polygon);

		List<List<Double>> pointList = new ArrayList<>();
		generatePointList(polygon, pointList, dimInfo, false);
		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createPolygon(convertAggregate(pointList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseMultiPolygon(MultiPolygon multiPolygon) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiPolygon);

		List<List<Double>> pointList = new ArrayList<>();
		List<Integer> exteriorRings = new ArrayList<>();
		int exteriorRing = 0;

		for (PolygonProperty polygonProperty : multiPolygon.getPolygonMember()) {
			if (polygonProperty.isSetPolygon()) {
				List<List<Double>> tmp = new ArrayList<>();
				generatePointList(polygonProperty.getPolygon(), tmp, dimInfo, false);

				if (!tmp.isEmpty()) {
					pointList.addAll(tmp);
					exteriorRings.add(exteriorRing);
					exteriorRing += tmp.size();
				}
			} else
				throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
		}

		if (!pointList.isEmpty()) {
			int[] tmp = new int[exteriorRings.size()];
			for (int i = 0; i < exteriorRings.size(); i++)
				tmp[i] = exteriorRings.get(i);

			geometryObject = GeometryObject.createMultiPolygon(convertAggregate(pointList, dimInfo.is2d ? 2 : 3), tmp, dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());
		}		

		return geometryObject;
	}

	private GeometryObject parseSurface(Surface surface) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(surface);

		List<List<Double>> pointList = new ArrayList<>();
		generatePointList(surface, pointList, dimInfo, false);
		if (!pointList.isEmpty())
			geometryObject = GeometryObject.createPolygon(convertAggregate(pointList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return geometryObject;
	}

	private GeometryObject parseMultiSurface(MultiSurface multiSurface) throws GeometryParseException, SrsParseException {
		GeometryObject geometryObject = null;
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiSurface);

		List<List<Double>> pointList = new ArrayList<>();
		List<Integer> exteriorRings = new ArrayList<>();
		int exteriorRing = 0;

		if (multiSurface.isSetSurfaceMember()) {
			for (SurfaceProperty surfaceProperty : multiSurface.getSurfaceMember()) {
				if (surfaceProperty.isSetSurface()) {
					List<List<Double>> tmp = new ArrayList<>();
					generatePointList(surfaceProperty.getSurface(), tmp, dimInfo, false);

					if (!tmp.isEmpty()) {
						pointList.addAll(tmp);
						exteriorRings.add(exteriorRing);
						exteriorRing += tmp.size();
					}

				} else
					throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
			}
		}

		if (!pointList.isEmpty()) {
			int[] tmp = new int[exteriorRings.size()];
			for (int i = 0; i < exteriorRings.size(); i++)
				tmp[i] = exteriorRings.get(i);

			geometryObject = GeometryObject.createMultiPolygon(convertAggregate(pointList, dimInfo.is2d ? 2 : 3), tmp, dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());
		}

		return geometryObject;
	}

	private List<Double> generatePointList(AbstractRing abstractRing, SrsDimensionInfo dimInfo, boolean reverse) throws GeometryParseException, SrsParseException {
		List<Double> pointList = new ArrayList<>();

		// get and set dimension
		int dimension = abstractRing.isSetSrsDimension() ? abstractRing.getSrsDimension() : dimInfo.defaultDimension;
		if (dimension == 3)
			dimInfo.is2d = false;

		// get and set srs
		if (abstractRing.isSetSrsName()) {
			DatabaseSrs srs = srsNameParser.getDatabaseSrs(abstractRing.getSrsName());
			if (dimInfo.targetSrs == null)
				dimInfo.targetSrs = srs;
			else if (dimInfo.targetSrs.getSrid() != srs.getSrid())
				throw new GeometryParseException("Mixing different spatial reference systems in one geometry operand is not allowed.");
		}

		if (abstractRing instanceof LinearRing) {
			LinearRing ring = (LinearRing)abstractRing;

			// get and set dimension on child elements
			if (ring.isSetPosList())
				setSrsDimension(ring.getPosList(), dimInfo, dimension);
			if (ring.isSetPosOrPointPropertyOrPointRep()) {
				for (PosOrPointPropertyOrPointRep controlPoint : ring.getPosOrPointPropertyOrPointRep()) {
					if (controlPoint.isSetPos() 
							&& controlPoint.getPos().isSetSrsDimension() 
							&& controlPoint.getPos().getSrsDimension() == 3)
						dimInfo.is2d = false;
				}
			}

			List<Double> coords = ring.toList3d(reverse);
			if (coords != null && !coords.isEmpty())
				pointList.addAll(coords);

			validateRing(pointList);
		}

		else if (abstractRing instanceof Ring) {
			Ring ring = (Ring)abstractRing;
			pointList = new ArrayList<>();

			for (CurveProperty curveMember : ring.getCurveMember()) {
				List<Double> tmp = new ArrayList<>();
				generatePointList(curveMember.getCurve(), tmp, dimInfo, false);

				if (!pointList.isEmpty()) {
					// remove duplicates
					int i = pointList.size() - 3;
					if (pointList.get(i).doubleValue() == tmp.get(0).doubleValue()
							&& pointList.get(i + 1).doubleValue() == tmp.get(1).doubleValue()
							&& pointList.get(i + 2).doubleValue() == tmp.get(2).doubleValue())
						pointList.addAll(tmp.subList(3, tmp.size()));
					else
						pointList.addAll(tmp);
				} else
					pointList.addAll(tmp);
			}

			validateRing(pointList);
		}

		return pointList;
	}

	private void generatePointList(AbstractSurface abstractSurface, List<List<Double>> pointList, SrsDimensionInfo dimInfo, boolean reverse) throws GeometryParseException, SrsParseException {
		// get and set dimension
		int dimension = abstractSurface.isSetSrsDimension() ? abstractSurface.getSrsDimension() : dimInfo.defaultDimension;
		if (dimension == 3)
			dimInfo.is2d = false;

		// get and set srs
		if (abstractSurface.isSetSrsName()) {
			DatabaseSrs srs = srsNameParser.getDatabaseSrs(abstractSurface.getSrsName());
			if (dimInfo.targetSrs == null)
				dimInfo.targetSrs = srs;
			else if (dimInfo.targetSrs.getSrid() != srs.getSrid())
				throw new GeometryParseException("Mixing different spatial reference systems in one geometry operand is not allowed.");
		}

		if (abstractSurface.getGMLClass() == GMLClass.POLYGON) {
			Polygon polygon = (Polygon)abstractSurface;

			if (polygon.isSetExterior()) {
				List<Double> coords = generatePointList(polygon.getExterior().getRing(), dimInfo, reverse);
				if (coords != null && !coords.isEmpty())
					pointList.add(coords);
			}

			if (!pointList.isEmpty() && polygon.isSetInterior()) {
				for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
					List<Double> coords = generatePointList(abstractRingProperty.getRing(), dimInfo, reverse);
					if (coords != null && !coords.isEmpty())
						pointList.add(coords);
				}
			}
		}

		else if (abstractSurface.getGMLClass() == GMLClass.SURFACE) {
			Surface surface = (Surface)abstractSurface;

			if (surface.isSetPatches()) {
				for (AbstractSurfacePatch surfacePatch : surface.getPatches().getSurfacePatch()) {
					if (surfacePatch.getGMLClass() == GMLClass.TRIANGLE) {
						Triangle triangle = (Triangle)surfacePatch;
						if (triangle.isSetExterior()) {
							List<Double> coords = generatePointList(triangle.getExterior().getRing(), dimInfo, reverse);
							if (coords != null && !coords.isEmpty())
								pointList.add(coords);
						}
					}

					else if (surfacePatch.getGMLClass() == GMLClass.RECTANGLE) {
						Rectangle rectangle = (Rectangle)surfacePatch;
						List<Double> coords = generatePointList(rectangle.getExterior().getRing(), dimInfo, reverse);
						if (coords != null && !coords.isEmpty())
							pointList.add(coords);
					}
				}
			}
		}

		else if (abstractSurface.getGMLClass() == GMLClass.COMPOSITE_SURFACE) {		
			CompositeSurface compositeSurface = (CompositeSurface)abstractSurface;
			if (compositeSurface.isSetSurfaceMember()) {
				for (SurfaceProperty surfaceProperty : compositeSurface.getSurfaceMember()) {
					if (surfaceProperty.isSetSurface())
						generatePointList(surfaceProperty.getSurface(), pointList, dimInfo, reverse);
					else
						throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
				}
			}
		}

		else if (abstractSurface.getGMLClass() == GMLClass.ORIENTABLE_SURFACE) {
			OrientableSurface orientableSurface = (OrientableSurface)abstractSurface;
			if (orientableSurface.isSetOrientation() && orientableSurface.getOrientation() == Sign.MINUS)
				reverse = !reverse;

			if (orientableSurface.isSetBaseSurface()) {
				SurfaceProperty surfaceProperty = orientableSurface.getBaseSurface();
				if (surfaceProperty.isSetSurface())
					generatePointList(surfaceProperty.getSurface(), pointList, dimInfo, reverse);
				else
					throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
			}
		}
	}

	private void generatePointList(AbstractCurve abstractCurve, List<Double> pointList, SrsDimensionInfo dimInfo, boolean reverse) throws GeometryParseException, SrsParseException {
		// get and set dimension
		int dimension = abstractCurve.isSetSrsDimension() ? abstractCurve.getSrsDimension() : dimInfo.defaultDimension;
		if (dimension == 3)
			dimInfo.is2d = false;

		// get and set srs
		if (abstractCurve.isSetSrsName()) {
			DatabaseSrs srs = srsNameParser.getDatabaseSrs(abstractCurve.getSrsName());
			if (dimInfo.targetSrs == null)
				dimInfo.targetSrs = srs;
			else if (dimInfo.targetSrs.getSrid() != srs.getSrid())
				throw new GeometryParseException("Mixing different spatial reference systems in one geometry operand is not allowed.");
		}

		if (abstractCurve.getGMLClass() == GMLClass.LINE_STRING) {	
			LineString lineString = (LineString)abstractCurve;

			if (lineString.isSetPosList())
				setSrsDimension(lineString.getPosList(), dimInfo, dimension);
			else if (lineString.isSetPosOrPointPropertyOrPointRepOrCoord()) {
				for (PosOrPointPropertyOrPointRepOrCoord controlPoint : lineString.getPosOrPointPropertyOrPointRepOrCoord()) {
					if (controlPoint.isSetPos() 
							&& controlPoint.getPos().isSetSrsDimension() 
							&& controlPoint.getPos().getSrsDimension() == 3)
						dimInfo.is2d = false;
				}
			}

			List<Double> points = lineString.toList3d(reverse);
			if (points != null && !points.isEmpty())
				pointList.addAll(points);				
		}

		else if (abstractCurve.getGMLClass() == GMLClass.CURVE) {
			Curve curve = (Curve)abstractCurve;
			if (curve.isSetSegments()) {
				CurveSegmentArrayProperty arrayProperty = curve.getSegments();

				if (arrayProperty.isSetCurveSegment()) {
					List<Double> points = new ArrayList<>();

					for (AbstractCurveSegment abstractCurveSegment : arrayProperty.getCurveSegment())
						if (abstractCurveSegment.getGMLClass() == GMLClass.LINE_STRING_SEGMENT) {
							LineStringSegment segment = (LineStringSegment)abstractCurveSegment;

							if (segment.isSetPosList())
								setSrsDimension(segment.getPosList(), dimInfo, dimension);
							else if (segment.isSetPosOrPointPropertyOrPointRep()) {
								for (PosOrPointPropertyOrPointRep controlPoint : segment.getPosOrPointPropertyOrPointRep()) {
									if (controlPoint.isSetPos() 
											&& controlPoint.getPos().isSetSrsDimension() 
											&& controlPoint.getPos().getSrsDimension() == 3)
										dimInfo.is2d = false;
								}
							}

							points.addAll(((LineStringSegment)abstractCurveSegment).toList3d());
						}

					if (!points.isEmpty()) {
						if (!reverse)
							pointList.addAll(points);
						else {
							for (int i = points.size() - 3; i >= 0; i -= 3)
								pointList.addAll(points.subList(i, i + 3));
						}
					}
				}
			}
		}

		else if (abstractCurve.getGMLClass() == GMLClass.COMPOSITE_CURVE) {		
			CompositeCurve compositeCurve = (CompositeCurve)abstractCurve;
			if (compositeCurve.isSetCurveMember()) {		
				for (CurveProperty curveProperty : compositeCurve.getCurveMember()) {
					if (curveProperty.isSetCurve())
						generatePointList(curveProperty.getCurve(), pointList, dimInfo, reverse);
					else
						throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
				}
			}
		} 

		else if (abstractCurve.getGMLClass() == GMLClass.ORIENTABLE_CURVE) {			
			OrientableCurve orientableCurve = (OrientableCurve)abstractCurve;
			if (orientableCurve.isSetOrientation() && orientableCurve.getOrientation() == Sign.MINUS)
				reverse = !reverse;

			if (orientableCurve.isSetBaseCurve()) {
				CurveProperty curveProperty = orientableCurve.getBaseCurve();
				if (curveProperty.isSetCurve())
					generatePointList(curveProperty.getCurve(), pointList, dimInfo, reverse);
				else
					throw new GeometryParseException("Geometry properties may neither be empty nor given by reference.");
			}
		}
	}

	private void setSrsDimension(DirectPositionList posList, SrsDimensionInfo dimInfo, int parentDimension) throws GeometryParseException, SrsParseException {
		// get and set srs
		if (posList.isSetSrsName()) {
			DatabaseSrs srs = srsNameParser.getDatabaseSrs(posList.getSrsName());
			if (dimInfo.targetSrs == null)
				dimInfo.targetSrs = srs;
			else if (dimInfo.targetSrs.getSrid() != srs.getSrid())
				throw new GeometryParseException("Mixing different spatial reference systems in one geometry operand is not allowed.");
		}

		if (!posList.isSetSrsDimension())
			posList.setSrsDimension(parentDimension);
		else if (posList.getSrsDimension() == 3)
			dimInfo.is2d = false;
	}

	private void validateRing(List<Double> coords) throws GeometryParseException {
		if (coords == null || coords.isEmpty()) {
			throw new GeometryParseException("Linear ring contains less than 4 coordinates.");
		}

		// check closedness
		Double x = coords.get(0);
		Double y = coords.get(1);
		Double z = coords.get(2);

		int nrOfPoints = coords.size();

		if (!x.equals(coords.get(nrOfPoints - 3)) ||
				!y.equals(coords.get(nrOfPoints - 2)) ||
				!z.equals(coords.get(nrOfPoints - 1))) {

			// repair unclosed ring...
			coords.add(x);
			coords.add(y);
			coords.add(z);
		}

		// check for minimum number of coordinates
		if (coords.size() / 3 < 4)
			throw new GeometryParseException("Linear ring contains less than 4 coordinates.");
	}

	private double[] convertPrimitive(List<Double> pointList, int dimension) {
		double[] result = new double[pointList.size() / 3 * dimension];

		if (dimension == 2) {
			for (int i = 0, j = 0; i < pointList.size(); i += 3) {
				result[j++] = pointList.get(i);
				result[j++] = pointList.get(i + 1);
			}
		} else {
			for (int i = 0, j = 0; i < pointList.size(); i += 3) {
				result[j++] = pointList.get(i);
				result[j++] = pointList.get(i + 1);
				result[j++] = pointList.get(i + 2);
			}
		}

		return result;
	}

	private double[][] convertAggregate(List<List<Double>> pointList, int dimension) {
		double[][] result = new double[pointList.size()][];

		int i = 0;
		for (List<Double> points : pointList)
			result[i++] = convertPrimitive(points, dimension);					

		return result;
	}

	private SrsDimensionInfo getSrsDimensionInfo(SRSReferenceGroup srsReference) throws SrsParseException {
		SrsDimensionInfo dimInfo = new SrsDimensionInfo();
		dimInfo.targetSrs = srsReference.isSetSrsName() ? srsNameParser.getDatabaseSrs(srsReference.getSrsName()) : srsNameParser.getDefaultSrs();
		dimInfo.defaultDimension = srsReference.isSetSrsDimension() ? srsReference.getSrsDimension() : (dimInfo.targetSrs.is3D() ? 3 : 2);
		dimInfo.is2d = dimInfo.defaultDimension == 2;

		return dimInfo;
	}

	private final class SrsDimensionInfo {
		int defaultDimension;
		boolean is2d;
		DatabaseSrs targetSrs;
	}
}
