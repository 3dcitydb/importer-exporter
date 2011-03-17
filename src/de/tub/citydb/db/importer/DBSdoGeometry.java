package de.tub.citydb.db.importer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import org.citygml4j.impl.jaxb.gml._3_1_1.LinearRingImpl;
import org.citygml4j.model.gml.AbstractCurve;
import org.citygml4j.model.gml.AbstractCurveSegment;
import org.citygml4j.model.gml.AbstractGeometricPrimitive;
import org.citygml4j.model.gml.AbstractRingProperty;
import org.citygml4j.model.gml.CompositeCurve;
import org.citygml4j.model.gml.ControlPoint;
import org.citygml4j.model.gml.Curve;
import org.citygml4j.model.gml.CurveProperty;
import org.citygml4j.model.gml.CurveSegmentArrayProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.GeometricComplex;
import org.citygml4j.model.gml.GeometricPositionGroup;
import org.citygml4j.model.gml.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.LineString;
import org.citygml4j.model.gml.LineStringSegment;
import org.citygml4j.model.gml.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.LinearRing;
import org.citygml4j.model.gml.MultiCurve;
import org.citygml4j.model.gml.MultiCurveProperty;
import org.citygml4j.model.gml.MultiPoint;
import org.citygml4j.model.gml.MultiPointProperty;
import org.citygml4j.model.gml.OrientableCurve;
import org.citygml4j.model.gml.Point;
import org.citygml4j.model.gml.PointArrayProperty;
import org.citygml4j.model.gml.PointProperty;
import org.citygml4j.model.gml.Polygon;
import org.citygml4j.model.gml.PolygonProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBSdoGeometry implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	private int dbSrid;

	public DBSdoGeometry(Config config) {
		dbSrid = config.getInternal().getOpenConnection().getMetaData().getSrid();
	}

	public JGeometry getPoint(PointProperty pointProperty) {
		JGeometry pointGeom = null;

		if (pointProperty != null && pointProperty.isSetPoint()) {
			Point point = pointProperty.getPoint();
			List<Double> values = point.toList();

			if (values != null && !values.isEmpty()) {
				double[] coords = new double[values.size()];
				int i = 0;
				for (Double value : values) {
					coords[i] = value.doubleValue();
					i++;
				}

				pointGeom = JGeometry.createPoint(coords, 3, dbSrid);
			}
		}

		return pointGeom;
	}

	public JGeometry getMultiPoint(MultiPointProperty multiPointProperty) {
		JGeometry multiPointGeom = null;

		if (multiPointProperty != null && multiPointProperty.isSetMultiPoint()) {
			MultiPoint multiPoint = multiPointProperty.getMultiPoint();
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			if (multiPoint.isSetPointMember()) {
				for (PointProperty pointProperty : multiPoint.getPointMember())
					if (pointProperty.isSetPoint())
						pointList.add(pointProperty.getPoint().toList());

			} else if (multiPoint.isSetPointMembers()) {
				PointArrayProperty pointArrayProperty = multiPoint.getPointMembers();
				for (Point point : pointArrayProperty.getPoint())
					pointList.add(point.toList());
			}

			if (!pointList.isEmpty()) {
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					double[] coords = new double[3];

					coords[0] = coordsList.get(0).doubleValue();
					coords[1] = coordsList.get(1).doubleValue();
					coords[2] = coordsList.get(2).doubleValue();

					pointArray[i] = coords;					
					i++;
				}

				multiPointGeom = JGeometry.createMultiPoint(pointArray, 3, dbSrid);
			}
		}

		return multiPointGeom;
	}

	public JGeometry getMultiPoint(ControlPoint controlPoint) {
		JGeometry multiPointGeom = null;

		if (controlPoint != null) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			if (controlPoint.isSetPosList()) {
				List<Double> posList = controlPoint.getPosList().toList();

				if (posList != null && !posList.isEmpty())
					for (int i = 0; i < posList.size(); i += 3)
						pointList.add(posList.subList(i, i + 3));

			} else if (controlPoint.isSetGeometricPositionGroup()) {					

				for (GeometricPositionGroup posGroup : controlPoint.getGeometricPositionGroup()) {
					if (posGroup.isSetPos())
						pointList.add(posGroup.getPos().toList()); 
					else if (posGroup.isSetPointProperty()) {
						PointProperty pointProperty = posGroup.getPointProperty();
						if (pointProperty.isSetPoint())
							pointList.add(pointProperty.getPoint().toList());
					}
				}
			}

			if (!pointList.isEmpty()) {
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					double[] coords = new double[3];

					coords[0] = coordsList.get(0).doubleValue();
					coords[1] = coordsList.get(1).doubleValue();
					coords[2] = coordsList.get(2).doubleValue();

					pointArray[i] = coords;					
					i++;
				}

				multiPointGeom = JGeometry.createMultiPoint(pointArray, 3, dbSrid);
			}
		}

		return multiPointGeom;
	}

	public JGeometry getMultiCurve(MultiCurveProperty multiCurveProperty) {
		JGeometry multiCurveGeom = null;

		if (multiCurveProperty != null && multiCurveProperty.isSetMultiCurve()) {
			MultiCurve multiCurve = multiCurveProperty.getMultiCurve();

			if (multiCurve.isSetCurveMember()) {
				List<List<Double>> pointList = new ArrayList<List<Double>>();

				for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
					if (curveProperty.isSetCurve()) {
						AbstractCurve curve = curveProperty.getCurve();
						List<Double> points = new ArrayList<Double>(); 
						generatePointList(curve, points, false);

						if (!points.isEmpty())
							pointList.add(points);
					} else {
						// xlinks are not allowed here...
					}
				}

				if (!pointList.isEmpty()) {
					Object[] pointArray = new Object[pointList.size()];
					int i = 0;
					for (List<Double> coordsList : pointList) {
						double[] coords = new double[coordsList.size()];

						int j = 0;
						for (Double coord : coordsList) {
							coords[j] = coord.doubleValue();
							j++;
						}

						pointArray[i] = coords;					
						i++;
					}

					multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, dbSrid);
				}
			}
		}

		return multiCurveGeom;
	}

	public JGeometry getMultiCurve(GeometricComplex geometricComplex) {
		JGeometry multiCurveGeom = null;

		if (geometricComplex != null && geometricComplex.isSetElement()) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			for (GeometricPrimitiveProperty primitiveProperty : geometricComplex.getElement()) {
				if (primitiveProperty.isSetGeometricPrimitive()) {
					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();
					List<Double> points = new ArrayList<Double>();

					switch (primitive.getGMLClass()) {
					case LINESTRING:
					case COMPOSITECURVE:
					case ORIENTABLECURVE:
					case CURVE:
						generatePointList((AbstractCurve)primitive, points, false);
					}

					if (!points.isEmpty())
						pointList.add(points);
				}
			}

			if (!pointList.isEmpty()) {
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					double[] coords = new double[coordsList.size()];

					int j = 0;
					for (Double coord : coordsList) {
						coords[j] = coord.doubleValue();
						j++;
					}

					pointArray[i] = coords;					
					i++;
				}

				multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, dbSrid);
			}
		}

		return multiCurveGeom;
	}

	private void generatePointList(AbstractCurve abstractCurve, List<Double> pointList, boolean reverse) {

		if (abstractCurve.getGMLClass() == GMLClass.LINESTRING) {	
			LineString lineString = (LineString)abstractCurve;
			List<Double> points = lineString.toList(reverse);

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
						if (abstractCurveSegment.getGMLClass() == GMLClass.LINESTRINGSEGMENT)
							points.addAll(((LineStringSegment)abstractCurveSegment).toList());

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

		else if (abstractCurve.getGMLClass() == GMLClass.COMPOSITECURVE) {		
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

		else if (abstractCurve.getGMLClass() == GMLClass.ORIENTABLECURVE) {			
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

	public JGeometry getMultiCurve(List<LineStringSegmentArrayProperty> propertyList) {
		JGeometry multiCurveGeom = null;

		if (propertyList != null && !propertyList.isEmpty()) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();

			for (LineStringSegmentArrayProperty property : propertyList) {
				if (property.isSetLineStringSegment()) {
					List<Double> points = new ArrayList<Double>();

					for (LineStringSegment segment : property.getLineStringSegment())
						points.addAll(segment.toList());

					if (!points.isEmpty())
						pointList.add(points);
				}
			}

			if (!pointList.isEmpty()) {
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					double[] coords = new double[coordsList.size()];

					int j = 0;
					for (Double coord : coordsList) {
						coords[j] = coord.doubleValue();
						j++;
					}

					pointArray[i] = coords;					
					i++;
				}

				multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, dbSrid);
			}
		}

		return multiCurveGeom;
	}

	public JGeometry get2DPolygon(PolygonProperty polygonProperty) {
		return getPolygon(polygonProperty, true);
	}
	
	public JGeometry getPolygon(PolygonProperty polygonProperty) {
		return getPolygon(polygonProperty, false);
	}
	
	private JGeometry getPolygon(PolygonProperty polygonProperty, boolean is2d) {
		JGeometry polygonGeom = null;

		if (polygonProperty != null && polygonProperty.isSetPolygon()) {
			Polygon polygon = polygonProperty.getPolygon();

			// exterior
			if (polygon.isSetExterior()) {
				LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();
				List<List<Double>> pointList = new ArrayList<List<Double>>();

				if (exteriorLinearRing != null) {
					List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList();

					if (points != null && !points.isEmpty()) {
						Double x = points.get(0);
						Double y = points.get(1);
						Double z = points.get(2);
						int nrOfPoints = points.size();
						
						if (!x.equals(points.get(nrOfPoints - 3)) ||
								!y.equals(points.get(nrOfPoints - 2)) ||
								!z.equals(points.get(nrOfPoints - 1))) {
							// repair unclosed ring because sdoapi fails to do its job...
							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									exteriorLinearRing.getGMLClass(), 
									polygon.getId()));
							msg.append(": Exterior ring is not closed. Appending first coordinate to fix it.");
							LOG.warn(msg.toString());
							
							points.add(x);
							points.add(y);
							points.add(z);
							++nrOfPoints;
						}
						
						if (nrOfPoints < 4) {
							// invalid ring...
							StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
									exteriorLinearRing.getGMLClass(), 
									polygon.getId()));
							msg.append(": Exterior ring contains less than 4 coordinates. Skipping invalid ring.");
							LOG.error(msg.toString());
							return null;
						}
						
						pointList.add(points);

						if (polygon.getInterior() != null) {
							List<AbstractRingProperty> abstractRingPropertyList = polygon.getInterior();
							for (AbstractRingProperty abstractRingProperty : abstractRingPropertyList) {
								LinearRing interiorLinearRing = (LinearRing)abstractRingProperty.getRing();
								List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList();

								if (interiorPoints != null && !interiorPoints.isEmpty()) {
									x = interiorPoints.get(0);
									y = interiorPoints.get(1);
									z = interiorPoints.get(2);
									nrOfPoints = interiorPoints.size();
									
									if (!x.equals(interiorPoints.get(nrOfPoints - 3)) ||
											!y.equals(interiorPoints.get(nrOfPoints - 2)) ||
											!z.equals(interiorPoints.get(nrOfPoints - 1))) {
										// repair unclosed ring because sdoapi fails to do its job...
										StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
												interiorLinearRing.getGMLClass(), 
												polygon.getId()));
										msg.append(": Interior ring is not closed. Appending first coordinate to fix it.");
										LOG.warn(msg.toString());
										
										interiorPoints.add(x);
										interiorPoints.add(y);
										interiorPoints.add(z);
										++nrOfPoints;
									}	
									
									if (nrOfPoints < 4) {
										// invalid ring...
										StringBuilder msg = new StringBuilder(Util.getGeometrySignature(
												interiorLinearRing.getGMLClass(), 
												polygon.getId()));
										msg.append(": Interior ring contains less than 4 coordinates. Skipping invalid ring.");
										LOG.error(msg.toString());
										return null;
									}
									
									pointList.add(interiorPoints);			
								}
							}
						}
					}
				}

				if (!pointList.isEmpty()) {
					Object[] pointArray = new Object[pointList.size()];
					int dim = is2d ? 2 : 3;
					
					// if we have to return a 2d polygon we first have to correct the
					// double lists we retrieved from citygml4j as they are always 3d
					if (is2d) {
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
					
					int i = 0;
					for (List<Double> coordsList : pointList) {
						double[] coords = new double[coordsList.size()];

						int j = 0;
						for (Double coord : coordsList) {
							coords[j] = coord.doubleValue();								
							j++;
						}

						pointArray[i] = coords;					
						i++;
					}

					polygonGeom = JGeometry.createLinearPolygon(pointArray, dim, dbSrid);
				}
			}
		}

		return polygonGeom;
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
		return DBImporterEnum.SDO_GEOMETRY;
	}

}
