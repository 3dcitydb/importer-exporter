/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package org.citydb.modules.citygml.importer.database.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.modules.citygml.importer.util.RingValidator;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurve;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurveSegment;
import org.citygml4j.model.gml.geometry.primitives.AbstractGeometricPrimitive;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.Curve;
import org.citygml4j.model.gml.geometry.primitives.CurveArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.GeometricPositionGroup;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableCurve;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.util.walker.GeometryWalker;

public class DBOtherGeometry implements DBImporter {
	private final DBImporterManager dbImporterManager;

	private int dbSrid;
	private boolean affineTransformation;
	private RingValidator ringValidator;

	public DBOtherGeometry(Config config, DBImporterManager dbImporterManager) {
		this.dbImporterManager = dbImporterManager;

		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		ringValidator = new RingValidator();
	}

	public boolean isPointOrLineGeometry(AbstractGeometry abstractGeometry) {
		switch (abstractGeometry.getGMLClass()) {
		case POINT:
		case MULTI_POINT:
		case LINE_STRING:
		case CURVE:
		case COMPOSITE_CURVE:
		case ORIENTABLE_CURVE:
		case MULTI_CURVE:
			return true;
		case GEOMETRIC_COMPLEX:
			GeometricComplex complex = (GeometricComplex)abstractGeometry;
			return containsPointPrimitives(complex, true) || containsCurvePrimitives(complex, true);
		default:
			return false;
		}
	}

	public GeometryObject getPoint(Point point) {
		GeometryObject pointGeom = null;

		if (point != null) {
			List<Double> values = point.toList3d();
			if (values != null && !values.isEmpty())
				pointGeom = GeometryObject.createPoint(convertPrimitive(values), 3, dbSrid);
		}

		return pointGeom;
	}

	public GeometryObject getPointGeometry(GeometricComplex geometricComplex) {
		GeometryObject pointGeom = null;

		if (geometricComplex != null && geometricComplex.isSetElement()) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			for (GeometricPrimitiveProperty primitiveProperty : geometricComplex.getElement()) {
				if (primitiveProperty.isSetGeometricPrimitive()) {
					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();
					if (primitive.getGMLClass() == GMLClass.POINT)
						pointList.add(((Point)primitive).toList3d());					
				}
			}

			if (!pointList.isEmpty()) {				
				double[][] pointArray = convertAggregate(pointList);
				if (pointList.size() > 1)				
					pointGeom = GeometryObject.createMultiPoint(pointArray, 3, dbSrid);
				else
					pointGeom = GeometryObject.createPoint(pointArray[0], 3, dbSrid);
			}
		}

		return pointGeom;
	}

	public GeometryObject getMultiPoint(MultiPoint multiPoint) {
		GeometryObject multiPointGeom = null;

		if (multiPoint != null) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			if (multiPoint.isSetPointMember()) {
				for (PointProperty pointProperty : multiPoint.getPointMember())
					if (pointProperty.isSetPoint())
						pointList.add(pointProperty.getPoint().toList3d());

			} else if (multiPoint.isSetPointMembers()) {
				PointArrayProperty pointArrayProperty = multiPoint.getPointMembers();
				for (Point point : pointArrayProperty.getPoint())
					pointList.add(point.toList3d());
			}

			if (!pointList.isEmpty())
				multiPointGeom = GeometryObject.createMultiPoint(convertAggregate(pointList), 3, dbSrid);
		}

		return multiPointGeom;
	}

	public GeometryObject getMultiPoint(ControlPoint controlPoint) {
		GeometryObject multiPointGeom = null;

		if (controlPoint != null) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			if (controlPoint.isSetPosList()) {
				List<Double> posList = controlPoint.getPosList().toList3d();
				if (posList != null && !posList.isEmpty())
					for (int i = 0; i < posList.size(); i += 3)
						pointList.add(posList.subList(i, i + 3));

			} else if (controlPoint.isSetGeometricPositionGroup()) {					
				for (GeometricPositionGroup posGroup : controlPoint.getGeometricPositionGroup()) {
					if (posGroup.isSetPos())
						pointList.add(posGroup.getPos().toList3d()); 
					else if (posGroup.isSetPointProperty()) {
						PointProperty pointProperty = posGroup.getPointProperty();
						if (pointProperty.isSetPoint())
							pointList.add(pointProperty.getPoint().toList3d());
					}
				}
			}

			if (!pointList.isEmpty())
				multiPointGeom = GeometryObject.createMultiPoint(convertAggregate(pointList), 3, dbSrid);
		}

		return multiPointGeom;
	}

	public GeometryObject getCurve(AbstractCurve curve) {
		GeometryObject curveGeom = null;

		if (curve != null) {
			List<Double> pointList = new ArrayList<Double>();
			generatePointList(curve, pointList, false);
			if (!pointList.isEmpty())
				curveGeom = GeometryObject.createCurve(convertPrimitive(pointList), 3, dbSrid);
		}

		return curveGeom;
	}

	public GeometryObject getMultiCurve(MultiCurve multiCurve) {
		GeometryObject multiCurveGeom = null;

		if (multiCurve != null) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			if (multiCurve.isSetCurveMember()) {
				for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
					if (curveProperty.isSetCurve()) {
						AbstractCurve curve = curveProperty.getCurve();
						List<Double> points = new ArrayList<Double>(); 
						generatePointList(curve, points, false);

						if (!points.isEmpty())
							pointList.add(points);
					}
				}
			} else if (multiCurve.isSetCurveMembers()) {
				CurveArrayProperty curveArrayProperty = multiCurve.getCurveMembers();
				for (AbstractCurve curve : curveArrayProperty.getCurve()) {
					List<Double> points = new ArrayList<Double>(); 
					generatePointList(curve, points, false);

					if (!points.isEmpty())
						pointList.add(points);
				}
			}

			if (!pointList.isEmpty())
				multiCurveGeom = GeometryObject.createMultiCurve(convertAggregate(pointList), 3, dbSrid);
		}

		return multiCurveGeom;
	}

	public GeometryObject getCurveGeometry(GeometricComplex geometricComplex) {
		GeometryObject curveGeom = null;

		if (geometricComplex != null && geometricComplex.isSetElement()) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			for (GeometricPrimitiveProperty primitiveProperty : geometricComplex.getElement()) {
				if (primitiveProperty.isSetGeometricPrimitive()) {
					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();
					List<Double> points = new ArrayList<Double>();

					switch (primitive.getGMLClass()) {
					case LINE_STRING:
					case COMPOSITE_CURVE:
					case ORIENTABLE_CURVE:
					case CURVE:
						generatePointList((AbstractCurve)primitive, points, false);
						break;
					default:
						// nothing to do
					}

					if (!points.isEmpty())
						pointList.add(points);
				}
			}

			if (!pointList.isEmpty()) {
				double[][] pointArray = convertAggregate(pointList);
				if (pointList.size() > 1)
					curveGeom = GeometryObject.createMultiCurve(pointArray, 3, dbSrid);
				else
					curveGeom = GeometryObject.createCurve(pointArray[0], 3, dbSrid);
			}
		}

		return curveGeom;
	}

	public GeometryObject getMultiCurve(List<LineStringSegmentArrayProperty> propertyList) {
		GeometryObject multiCurveGeom = null;

		if (propertyList != null && !propertyList.isEmpty()) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			for (LineStringSegmentArrayProperty property : propertyList) {
				if (property.isSetLineStringSegment()) {
					List<Double> points = new ArrayList<Double>();

					for (LineStringSegment segment : property.getLineStringSegment())
						points.addAll(segment.toList3d());

					if (!points.isEmpty())
						pointList.add(points);
				}
			}

			if (!pointList.isEmpty())
				multiCurveGeom = GeometryObject.createMultiCurve(convertAggregate(pointList), 3, dbSrid);
		}

		return multiCurveGeom;
	}

	public GeometryObject getPoint(PointProperty pointProperty) {
		return pointProperty != null ? getPoint(pointProperty.getPoint()) : null;
	}

	public GeometryObject getMultiPoint(MultiPointProperty multiPointProperty) {
		return multiPointProperty != null ? getMultiPoint(multiPointProperty.getMultiPoint()) : null;
	}

	public GeometryObject getPointGeometry(GeometricComplexProperty complexProperty) {
		return (complexProperty != null && complexProperty.isSetGeometricComplex()) ? 
				getPointGeometry(complexProperty.getGeometricComplex()) : null;
	}

	public GeometryObject getCurve(CurveProperty curveProperty) {
		return curveProperty != null ? getCurve(curveProperty.getCurve()) : null;
	}

	public GeometryObject getMultiCurve(MultiCurveProperty multiCurveProperty) {
		return multiCurveProperty != null ? getMultiCurve(multiCurveProperty.getMultiCurve()) : null;
	}

	public GeometryObject getCurveGeometry(GeometricComplexProperty complexProperty) {
		return (complexProperty != null && complexProperty.isSetGeometricComplex()) ? 
				getCurveGeometry(complexProperty.getGeometricComplex()) : null;
	}

	public GeometryObject getPointOrCurveGeometry(AbstractGeometry abstractGeometry) {
		switch (abstractGeometry.getGMLClass()) {
		case POINT:
			return getPoint((Point)abstractGeometry);
		case MULTI_POINT:
			return getMultiPoint((MultiPoint)abstractGeometry);
		case LINE_STRING:
		case CURVE:
		case COMPOSITE_CURVE:
		case ORIENTABLE_CURVE:
			return getCurve((AbstractCurve)abstractGeometry);
		case MULTI_CURVE:
			return getMultiCurve((MultiCurve)abstractGeometry);
		case GEOMETRIC_COMPLEX:
			GeometricComplex complex = (GeometricComplex)abstractGeometry;
			if (containsPointPrimitives(complex, true))
				return getPointGeometry((GeometricComplex)abstractGeometry);
			else if (containsCurvePrimitives(complex, true))
				return getCurveGeometry((GeometricComplex)abstractGeometry);
			else 
				return null;
		default:
			return null;
		}
	}

	private void generatePointList(AbstractCurve abstractCurve, List<Double> pointList, boolean reverse) {
		if (abstractCurve.getGMLClass() == GMLClass.LINE_STRING) {	
			LineString lineString = (LineString)abstractCurve;
			List<Double> points = lineString.toList3d(reverse);

			if (points != null && !points.isEmpty())
				pointList.addAll(points);				
		}

		else if (abstractCurve.getGMLClass() == GMLClass.CURVE) {
			Curve curve = (Curve)abstractCurve;
			if (curve.isSetSegments()) {
				CurveSegmentArrayProperty arrayProperty = curve.getSegments();

				if (arrayProperty.isSetCurveSegment()) {
					List<Double> points = new ArrayList<Double>();

					for (AbstractCurveSegment abstractCurveSegment : arrayProperty.getCurveSegment())
						if (abstractCurveSegment.getGMLClass() == GMLClass.LINE_STRING_SEGMENT)
							points.addAll(((LineStringSegment)abstractCurveSegment).toList3d());

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
						generatePointList(curveProperty.getCurve(), pointList, reverse);
					else {
						//xlinks are not allowed here...
					}
				}
			}
		} 

		else if (abstractCurve.getGMLClass() == GMLClass.ORIENTABLE_CURVE) {			
			OrientableCurve orientableCurve = (OrientableCurve)abstractCurve;
			if (orientableCurve.isSetOrientation() && orientableCurve.getOrientation().equals("-"))
				reverse = !reverse;

			if (orientableCurve.isSetBaseCurve()) {
				CurveProperty curveProperty = orientableCurve.getBaseCurve();
				if (curveProperty.isSetCurve())
					generatePointList(curveProperty.getCurve(), pointList, reverse);
				else {
					// xlinks are not allowed here
				}				
			}
		}
	}

	private double[] convertPrimitive(List<Double> pointList) {
		if (affineTransformation)
			dbImporterManager.getAffineTransformer().transformCoordinates(pointList);

		double[] result = new double[pointList.size()];

		int i = 0;
		for (Double point : pointList)
			result[i++] = point.doubleValue();

		return result;
	}

	private double[][] convertAggregate(List<List<Double>> pointList) {
		double[][] result = new double[pointList.size()][];
		int i = 0;
		for (List<Double> points : pointList) {
			if (affineTransformation)
				dbImporterManager.getAffineTransformer().transformCoordinates(points);

			double[] coords = new double[points.size()];

			int j = 0;
			for (Double coord : points)
				coords[j++] = coord.doubleValue();

			result[i++] = coords;					
		}

		return result;
	}

	public GeometryObject get2DPolygon(Polygon polygon) {
		return getPolygon(polygon, true);
	}

	public GeometryObject getPolygon(Polygon polygon) {
		return getPolygon(polygon, false);
	}

	private GeometryObject getPolygon(Polygon polygon, boolean is2d) {
		GeometryObject polygonGeom = null;

		if (polygon != null) {
			List<List<Double>> pointList = generatePointList(polygon, is2d, false);
			if (pointList != null && !pointList.isEmpty())
				polygonGeom = GeometryObject.createPolygon(convertAggregate(pointList), is2d ? 2 : 3, dbSrid);
		}

		return polygonGeom;
	}

	public GeometryObject get2DPolygon(PolygonProperty polygonProperty) {
		return polygonProperty != null ? get2DPolygon(polygonProperty.getPolygon()) : null;
	}

	public GeometryObject getPolygon(PolygonProperty polygonProperty) {
		return polygonProperty != null ? getPolygon(polygonProperty.getPolygon()) : null;
	}

	private List<List<Double>> generatePointList(Polygon polygon, boolean is2d, boolean reverse) {
		List<List<Double>> pointList = new ArrayList<>();

		if (polygon.isSetExterior()) {
			AbstractRing exteriorAbstractRing = polygon.getExterior().getRing();
			if (exteriorAbstractRing instanceof LinearRing) {
				LinearRing exteriorLinearRing = (LinearRing)exteriorAbstractRing;
				if (!ringValidator.validate(exteriorLinearRing, polygon.getId()))
					return null;

				pointList.add(exteriorLinearRing.toList3d(reverse));

				if (polygon.isSetInterior()) {
					for (AbstractRingProperty abstractRingProperty : polygon.getInterior()) {
						AbstractRing interiorAbstractRing = abstractRingProperty.getRing();
						if (interiorAbstractRing instanceof LinearRing) {
							LinearRing interiorLinearRing = (LinearRing)interiorAbstractRing;
							if (!ringValidator.validate(interiorLinearRing, polygon.getId()))
								return null;

							pointList.add(interiorLinearRing.toList3d(reverse));
						}
					}
				}
			}

			if (is2d) {
				// if we have to return a 2d polygon we first have to correct the
				// double lists we retrieved from citygml4j as they are always 3d
				for (List<Double> coordsList : pointList) {							
					Iterator<Double> iter = coordsList.iterator();

					int count = 0;							
					while (iter.hasNext()) {
						iter.next();

						if (count++ == 2) {
							count = 0;	
							iter.remove();
						}
					}
				}						
			}
		}

		return pointList;
	}

	public GeometryObject getSolid(Solid solid) {
		GeometryObject solidGeom = null;

		if (solid != null) {
			final List<List<Double>> pointList = new ArrayList<>();
			final List<Integer> rings = new ArrayList<>();

			solid.accept(new GeometryWalker() {
				boolean reverse = false;
				int ringNo = 0;

				public <T extends AbstractGeometry> void visit(GeometryProperty<T> geometryProperty) {
					if (geometryProperty.isSetHref()) {
						setShouldWalk(false);
						pointList.clear();
						return;
					}

					super.visit(geometryProperty);
				}

				public void visit(OrientableSurface orientableSurface) {
					if (orientableSurface.getOrientation() == Sign.MINUS) {
						reverse = !reverse;
						super.visit(orientableSurface);
						reverse = !reverse;
					} else
						super.visit(orientableSurface);
				}

				public void visit(Polygon polygon) {
					List<List<Double>> points = generatePointList(polygon, false, reverse);
					if (points == null || points.isEmpty()) {
						setShouldWalk(false);
						pointList.clear();
						return;
					}

					pointList.addAll(points);
					rings.add(ringNo);
					ringNo += points.size();
				}

				public void visit(LinearRing linearRing) {
					// required to handle surface patches such as triangles and rectangles
					if (ringValidator.validate(linearRing, null)) {
						List<Double> points = linearRing.toList3d(reverse);
						pointList.add(points);
						rings.add(ringNo);
						ringNo++;
					}
				}

			});

			if (!pointList.isEmpty()) {
				int[] exteriorRings = new int[rings.size()];

				int i = 0;
				for (Integer ringNo : rings)
					exteriorRings[i++] = ringNo.intValue();

				solidGeom = GeometryObject.createSolid(convertAggregate(pointList), exteriorRings, dbSrid);
			}
		}

		return solidGeom;
	}

	public GeometryObject getCompositeSolid(CompositeSolid compositeSolid) {
		GeometryObject compositeSolidGeom = null;

		if (compositeSolid != null) {
			final List<GeometryObject> solidMembers = new ArrayList<>();

			compositeSolid.accept(new GeometryWalker() {
				public void visit(Solid solid) {
					GeometryObject solidMember = getSolid(solid);
					if (solidMember != null)
						solidMembers.add(solidMember);
					else {
						setShouldWalk(false);
						solidMembers.clear();
						return;
					}
				}
			});

			if (!solidMembers.isEmpty()) {
				GeometryObject[] tmp = new GeometryObject[solidMembers.size()];
				
				int i = 0;
				for (GeometryObject solidMember : solidMembers)
					tmp[i++] = solidMember;
				
				compositeSolidGeom = GeometryObject.createCompositeSolid(tmp, dbSrid);
			}
		}

		return compositeSolidGeom;
	}

	private boolean containsPointPrimitives(GeometricComplex geometricComplex, boolean exclusive) {
		if (geometricComplex != null && geometricComplex.isSetElement()) {
			for (GeometricPrimitiveProperty primitiveProperty : geometricComplex.getElement()) {
				if (primitiveProperty.isSetGeometricPrimitive()) {
					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();

					switch (primitive.getGMLClass()) {
					case POINT:
						if (!exclusive)
							return true;
						else 
							break;
					default:
						if (!exclusive)
							return false;
					}
				}
			}
		}

		return true;
	}

	private boolean containsCurvePrimitives(GeometricComplex geometricComplex, boolean exclusive) {
		if (geometricComplex != null && geometricComplex.isSetElement()) {
			for (GeometricPrimitiveProperty primitiveProperty : geometricComplex.getElement()) {
				if (primitiveProperty.isSetGeometricPrimitive()) {
					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();

					switch (primitive.getGMLClass()) {
					case LINE_STRING:
					case CURVE:
					case COMPOSITE_CURVE:
					case ORIENTABLE_CURVE:
						if (!exclusive)
							return true;
						else 
							break;
					default:
						if (exclusive)
							return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		// nothing to do here
	}

	@Override
	public void close() throws SQLException {
		// nothing to do here
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.OTHER_GEOMETRY;
	}

}
