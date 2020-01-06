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
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
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
import org.citygml4j.model.gml.geometry.primitives.PolygonPatch;
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
		try {
			ModelObject object = unmarshaller.unmarshal(geometry);
			if (object instanceof GML)
				return parseGeometry((GML)object, geometry.getName());
		} catch (MissingADESchemaException e) {
			throw new GeometryParseException("Failed to parse GML geometry.", e);
		}

		throw new GeometryParseException("Failed to parse the geometry element '" + geometry.getName() + "'.");
	}
	
	public GeometryObject parseGeometry(Node geometry) throws GeometryParseException, SrsParseException {
		try {
			ModelObject object = unmarshaller.unmarshal(geometry);
			if (object instanceof GML)
				return parseGeometry((GML)object, new QName(geometry.getNamespaceURI(), geometry.getLocalName()));
		} catch (MissingADESchemaException e) {
			throw new GeometryParseException("Failed to parse GML geometry.", e);
		}

		throw new GeometryParseException("Failed to parse the geometry element '" + geometry.getLocalName() + "'.");
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
		DatabaseSrs targetSrs = point.isSetSrsName() ? srsNameParser.getDatabaseSrs(point.getSrsName()) : srsNameParser.getDefaultSrs();

		// we assume the dim of the target SRS as default value
		int dimension = point.isSetSrsDimension() ? point.getSrsDimension() : (targetSrs.is3D() ? 3 : 2);
		if (point.isSetPos() && point.getPos().isSetSrsDimension() && point.getPos().getSrsDimension() == 3)
			dimension = 3;

		List<Double> coordinates = point.toList3d();
		if (!coordinates.isEmpty())
			return GeometryObject.createPoint(convertPrimitive(coordinates, dimension), dimension, targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseMultiPoint(MultiPoint multiPoint) throws GeometryParseException, SrsParseException {
		DatabaseSrs targetSrs = multiPoint.isSetSrsName() ? srsNameParser.getDatabaseSrs(multiPoint.getSrsName()) : srsNameParser.getDefaultSrs();

		// we assume the dim of the target SRS as default value
		int dimension = multiPoint.isSetSrsDimension() ? multiPoint.getSrsDimension() : (targetSrs.is3D() ? 3 : 2);
		List<List<Double>> coordinatesList = new ArrayList<>();

		if (multiPoint.isSetPointMember()) {
			for (PointProperty property : multiPoint.getPointMember()) {
				if (property.isSetPoint()) {
					Point point = property.getPoint();
					if (point.isSetPos() && point.getPos().isSetSrsDimension() && point.getPos().getSrsDimension() == 3)
						dimension = 3;

					List<Double> coordinates = point.toList3d();
					if (!coordinates.isEmpty())
						coordinatesList.add(coordinates);
				}
			}

		} else if (multiPoint.isSetPointMembers()) {
			PointArrayProperty property = multiPoint.getPointMembers();
			for (Point point : property.getPoint()) {
				if (point != null) {
					if (point.isSetPos() && point.getPos().isSetSrsDimension() && point.getPos().getSrsDimension() == 3)
						dimension = 3;

					List<Double> coordinates = point.toList3d();
					if (!coordinates.isEmpty())
						coordinatesList.add(coordinates);
				}
			}
		}

		if (!coordinatesList.isEmpty())
			return GeometryObject.createMultiPoint(convertAggregate(coordinatesList, dimension), dimension, targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseLineString(LineString lineString) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(lineString);
		checkDimension(lineString, dimInfo);

		List<Double> coordinates = lineString.toList3d();
		if (!coordinates.isEmpty())
			return GeometryObject.createCurve(convertPrimitive(coordinates, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseMultiLineString(MultiLineString multiLineString) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiLineString);

		List<List<Double>> coordinatesList = new ArrayList<>();
		for (LineStringProperty property : multiLineString.getLineStringMember()) {
			if (property.isSetLineString()) {
				LineString lineString = property.getLineString();
				checkDimension(lineString, dimInfo);

				List<Double> coordinates = lineString.toList3d();
				if (!coordinates.isEmpty())
					coordinatesList.add(coordinates);
			}
		}

		if (!coordinatesList.isEmpty())
			return GeometryObject.createMultiCurve(convertAggregate(coordinatesList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseCurve(Curve curve) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(curve);
		checkDimension(curve, dimInfo);

		List<Double> coordinates = curve.toList3d();
		if (!coordinates.isEmpty())
			return GeometryObject.createCurve(convertPrimitive(coordinates, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseMultiCurve(MultiCurve multiCurve) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiCurve);

		List<List<Double>> coordinatesList = new ArrayList<>();
		if (multiCurve.isSetCurveMember()) {
			for (CurveProperty property : multiCurve.getCurveMember()) {
				if (property.isSetCurve()) {
					AbstractCurve curve = property.getCurve();
					checkDimension(curve, dimInfo);

					List<Double> coordinates = curve.toList3d();
					if (!coordinates.isEmpty())
						coordinatesList.add(coordinates);
				}
			}
		} 

		else if (multiCurve.isSetCurveMembers()) {
			for (AbstractCurve curve : multiCurve.getCurveMembers().getCurve()) {
				if (curve != null) {
					checkDimension(curve, dimInfo);
					List<Double> coordinates = curve.toList3d();
					if (!coordinates.isEmpty())
						coordinatesList.add(coordinates);
				}
			}
		}

		if (!coordinatesList.isEmpty())
			return GeometryObject.createMultiCurve(convertAggregate(coordinatesList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return null;
	}

	private GeometryObject parsePolygon(Polygon polygon) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(polygon);

		List<List<Double>> coordinatesList = new ArrayList<>();
		generateCoordinatesList(polygon, coordinatesList, dimInfo, false);
		if (!coordinatesList.isEmpty())
			return GeometryObject.createPolygon(convertAggregate(coordinatesList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseMultiPolygon(MultiPolygon multiPolygon) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiPolygon);

		List<List<Double>> coordinatesList = new ArrayList<>();
		List<Integer> exteriorRings = new ArrayList<>();
		int exteriorRing = 0;

		for (PolygonProperty property : multiPolygon.getPolygonMember()) {
			if (property.isSetPolygon()) {
				List<List<Double>> coordinates = new ArrayList<>();
				generateCoordinatesList(property.getPolygon(), coordinates, dimInfo, false);

				if (!coordinates.isEmpty()) {
					coordinatesList.addAll(coordinates);
					exteriorRings.add(exteriorRing);
					exteriorRing += coordinates.size();
				}
			}
		}

		if (!coordinatesList.isEmpty()) {
			int[] tmp = new int[exteriorRings.size()];
			for (int i = 0; i < exteriorRings.size(); i++)
				tmp[i] = exteriorRings.get(i);

			return GeometryObject.createMultiPolygon(convertAggregate(coordinatesList, dimInfo.is2d ? 2 : 3), tmp, dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());
		}		

		return null;
	}

	private GeometryObject parseSurface(Surface surface) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(surface);

		List<List<Double>> coordinatesList = new ArrayList<>();
		generateCoordinatesList(surface, coordinatesList, dimInfo, false);
		if (!coordinatesList.isEmpty())
			return GeometryObject.createPolygon(convertAggregate(coordinatesList, dimInfo.is2d ? 2 : 3), dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());

		return null;
	}

	private GeometryObject parseMultiSurface(MultiSurface multiSurface) throws GeometryParseException, SrsParseException {
		SrsDimensionInfo dimInfo = getSrsDimensionInfo(multiSurface);

		List<List<Double>> coordinatesList = new ArrayList<>();
		List<Integer> exteriorRings = new ArrayList<>();
		int exteriorRing = 0;

		if (multiSurface.isSetSurfaceMember()) {
			for (SurfaceProperty property : multiSurface.getSurfaceMember()) {
				if (property.isSetSurface()) {
					List<List<Double>> coordinates = new ArrayList<>();
					generateCoordinatesList(property.getSurface(), coordinates, dimInfo, false);

					if (!coordinates.isEmpty()) {
						coordinatesList.addAll(coordinates);
						exteriorRings.add(exteriorRing);
						exteriorRing += coordinates.size();
					}
				}
			}
		}

		if (!coordinatesList.isEmpty()) {
			int[] tmp = new int[exteriorRings.size()];
			for (int i = 0; i < exteriorRings.size(); i++)
				tmp[i] = exteriorRings.get(i);

			return GeometryObject.createMultiPolygon(convertAggregate(coordinatesList, dimInfo.is2d ? 2 : 3), tmp, dimInfo.is2d ? 2 : 3, dimInfo.targetSrs.getSrid());
		}

		return null;
	}

	private List<Double> generateCoordinatesList(AbstractRing abstractRing, SrsDimensionInfo dimInfo, boolean reverse) throws GeometryParseException, SrsParseException {
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

		// get and set dimension on child elements
		if (abstractRing instanceof LinearRing) {
			LinearRing ring = (LinearRing)abstractRing;
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
		}

		else if (abstractRing instanceof Ring) {
			Ring ring = (Ring)abstractRing;
			for (CurveProperty property : ring.getCurveMember()) {
				if (property.isSetCurve())
					checkDimension(property.getCurve(), dimInfo);
			}
		}

		List<Double> points = abstractRing.toList3d(reverse);
		validateRing(points);

		return points;
	}

	private void generateCoordinatesList(AbstractSurface abstractSurface, List<List<Double>> coordinatesList, SrsDimensionInfo dimInfo, boolean reverse) throws GeometryParseException, SrsParseException {
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

		if (abstractSurface instanceof Polygon) {
			Polygon polygon = (Polygon)abstractSurface;
			if (polygon.isSetExterior() && polygon.getExterior().isSetRing()) {
				List<Double> coordinates = generateCoordinatesList(polygon.getExterior().getRing(), dimInfo, reverse);
				if (!coordinates.isEmpty())
					coordinatesList.add(coordinates);
			}

			if (!coordinatesList.isEmpty() && polygon.isSetInterior()) {
				for (AbstractRingProperty property : polygon.getInterior()) {
					if (property.isSetRing()) {
						List<Double> coords = generateCoordinatesList(property.getRing(), dimInfo, reverse);
						if (coords != null && !coords.isEmpty())
							coordinatesList.add(coords);
					}
				}
			}
		}

		else if (abstractSurface instanceof Surface) {
			Surface surface = (Surface)abstractSurface;
			if (surface.isSetPatches()) {
				for (AbstractSurfacePatch surfacePatch : surface.getPatches().getSurfacePatch()) {
					if (surfacePatch instanceof Triangle) {
						Triangle triangle = (Triangle)surfacePatch;
						if (triangle.isSetExterior() && triangle.getExterior().isSetRing()) {
							List<Double> coordinates = generateCoordinatesList(triangle.getExterior().getRing(), dimInfo, reverse);
							if (coordinates != null && !coordinates.isEmpty())
								coordinatesList.add(coordinates);
						}
					}

					else if (surfacePatch instanceof Rectangle) {
						Rectangle rectangle = (Rectangle)surfacePatch;
						if (rectangle.isSetExterior() && rectangle.getExterior().isSetRing()) {
							List<Double> coordinates = generateCoordinatesList(rectangle.getExterior().getRing(), dimInfo, reverse);
							if (coordinates != null && !coordinates.isEmpty())
								coordinatesList.add(coordinates);
						}
					}

					else if (surfacePatch instanceof PolygonPatch) {
						PolygonPatch polygonPatch = (PolygonPatch) surfacePatch;
						Polygon polygon = new Polygon();
						polygon.setExterior(polygonPatch.getExterior());
						polygon.setInterior(polygonPatch.getInterior());
						generateCoordinatesList(polygon, coordinatesList, dimInfo, reverse);
					}
				}
			}
		}

		else if (abstractSurface instanceof CompositeSurface) {
			CompositeSurface compositeSurface = (CompositeSurface)abstractSurface;
			if (compositeSurface.isSetSurfaceMember()) {
				for (SurfaceProperty property : compositeSurface.getSurfaceMember()) {
					if (property.isSetSurface())
						generateCoordinatesList(property.getSurface(), coordinatesList, dimInfo, reverse);
				}
			}
		}

		else if (abstractSurface instanceof OrientableSurface) {
			OrientableSurface orientableSurface = (OrientableSurface)abstractSurface;
			if (orientableSurface.isSetOrientation() && orientableSurface.getOrientation() == Sign.MINUS)
				reverse = !reverse;

			if (orientableSurface.isSetBaseSurface() && orientableSurface.getBaseSurface().isSetSurface())
				generateCoordinatesList(orientableSurface.getBaseSurface().getSurface(), coordinatesList, dimInfo, reverse);
		}
	}

	private void checkDimension(AbstractCurve abstractCurve, SrsDimensionInfo dimInfo) throws GeometryParseException, SrsParseException {
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

		if (abstractCurve instanceof LineString) {
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
		}

		else if (abstractCurve instanceof Curve) {
			Curve curve = (Curve) abstractCurve;
			if (curve.isSetSegments() && curve.getSegments().isSetCurveSegment()) {
				for (AbstractCurveSegment abstractCurveSegment : curve.getSegments().getCurveSegment()) {
					if (abstractCurveSegment instanceof LineStringSegment) {
						LineStringSegment segment = (LineStringSegment) abstractCurveSegment;
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
					}
				}
			}
		}

		else if (abstractCurve instanceof CompositeCurve) {
			CompositeCurve compositeCurve = (CompositeCurve)abstractCurve;
			if (compositeCurve.isSetCurveMember()) {		
				for (CurveProperty property : compositeCurve.getCurveMember()) {
					if (property.isSetCurve())
						checkDimension(property.getCurve(), dimInfo);
				}
			}
		} 

		else if (abstractCurve instanceof OrientableCurve) {
			OrientableCurve orientableCurve = (OrientableCurve)abstractCurve;
			if (orientableCurve.isSetBaseCurve() && orientableCurve.getBaseCurve().isSetCurve())
				checkDimension(orientableCurve.getBaseCurve().getCurve(), dimInfo);
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
		if (coords == null || coords.isEmpty())
			throw new GeometryParseException("Ring contains less than 4 coordinates.");

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
			throw new GeometryParseException("Ring contains less than 4 coordinates.");
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

	private static final class SrsDimensionInfo {
		int defaultDimension;
		boolean is2d;
		DatabaseSrs targetSrs;
	}
}
