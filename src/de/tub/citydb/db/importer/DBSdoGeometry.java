package de.tub.citydb.db.importer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import de.tub.citydb.config.Config;
import de.tub.citygml4j.implementation.gml._3_1_1.LinearRingImpl;
import de.tub.citygml4j.model.gml.AbstractCurve;
import de.tub.citygml4j.model.gml.AbstractCurveSegment;
import de.tub.citygml4j.model.gml.AbstractGeometricPrimitive;
import de.tub.citygml4j.model.gml.AbstractRingProperty;
import de.tub.citygml4j.model.gml.CompositeCurve;
import de.tub.citygml4j.model.gml.ControlPoint;
import de.tub.citygml4j.model.gml.Curve;
import de.tub.citygml4j.model.gml.CurveProperty;
import de.tub.citygml4j.model.gml.CurveSegmentArrayProperty;
import de.tub.citygml4j.model.gml.GMLClass;
import de.tub.citygml4j.model.gml.GeometricComplex;
import de.tub.citygml4j.model.gml.GeometricPositionGroup;
import de.tub.citygml4j.model.gml.GeometricPrimitiveProperty;
import de.tub.citygml4j.model.gml.LineString;
import de.tub.citygml4j.model.gml.LineStringSegment;
import de.tub.citygml4j.model.gml.LineStringSegmentArrayProperty;
import de.tub.citygml4j.model.gml.LinearRing;
import de.tub.citygml4j.model.gml.MultiCurve;
import de.tub.citygml4j.model.gml.MultiCurveProperty;
import de.tub.citygml4j.model.gml.MultiPoint;
import de.tub.citygml4j.model.gml.MultiPointProperty;
import de.tub.citygml4j.model.gml.OrientableCurve;
import de.tub.citygml4j.model.gml.Point;
import de.tub.citygml4j.model.gml.PointProperty;
import de.tub.citygml4j.model.gml.Polygon;
import de.tub.citygml4j.model.gml.PolygonProperty;

public class DBSdoGeometry implements DBImporter {
	private final Config config;

	private String dbSrid;

	public DBSdoGeometry(Config config) {
		this.config = config;

		init();
	}

	private void init() {
		dbSrid = config.getInternal().getDbSrid();
	}

	public JGeometry getPoint(PointProperty pointProperty) {
		JGeometry pointGeom = null;
		
		if (pointProperty != null && pointProperty.getPoint() != null) {
			Point point = pointProperty.getPoint();
			List<Double> values = point.toList();
			
			if (values != null && !values.isEmpty()) {
				double[] coords = new double[values.size()];
				int i = 0;
				for (Double value : values) {
					coords[i] = value.doubleValue();
					i++;
				}
				
				pointGeom = JGeometry.createPoint(coords, 3, Integer.valueOf(dbSrid));
			}
		}
		
		return pointGeom;
	}
	
	public JGeometry getMultiPoint(MultiPointProperty multiPointProperty) {
		JGeometry multiPointGeom = null;

		if (multiPointProperty != null) {
			MultiPoint multiPoint = multiPointProperty.getMultiPoint();

			if (multiPoint != null && multiPoint.getPointMember() != null && !multiPoint.getPointMember().isEmpty()) {
				List<PointProperty> multiPointList = multiPoint.getPointMember();
				List<List<Double>> pointList = new ArrayList<List<Double>>();

				for (PointProperty pointProperty : multiPointList) {
					Point point = pointProperty.getPoint();

					if (point != null) {
						pointList.add(point.toList());
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

					multiPointGeom = JGeometry.createMultiPoint(pointArray, 3, Integer.valueOf(dbSrid));
				}
			}
		}

		return multiPointGeom;
	}

	public JGeometry getMultiPoint(ControlPoint controlPoint) {
		JGeometry multiPointGeom = null;
		
		if (controlPoint != null) {
			List<List<Double>> pointList = new ArrayList<List<Double>>();
			
			if (controlPoint.getPosList() != null) {
				List<Double> posList = controlPoint.getPosList().toList();
				
				if (posList != null && !posList.isEmpty())
					for (int i = 0; i < posList.size(); i += 3)
						pointList.add(posList.subList(i, i + 3));
			} else {
				List<GeometricPositionGroup> posGroupList = controlPoint.getGeometricPositionGroup();
				
				if (posGroupList != null && !posGroupList.isEmpty()) {
					
					for (GeometricPositionGroup posGroup : posGroupList) {
						if (posGroup.getPos() != null)
							pointList.add(posGroup.getPos().toList()); 
						else if (posGroup.getPointProperty() != null) {
							PointProperty pointProperty = posGroup.getPointProperty();
							if (pointProperty.getPoint() != null)
								pointList.add(pointProperty.getPoint().toList());
						}
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

				multiPointGeom = JGeometry.createMultiPoint(pointArray, 3, Integer.valueOf(dbSrid));
			}
		}
		
		return multiPointGeom;
	}
	
	public JGeometry getMultiCurve(MultiCurveProperty multiCurveProperty) {
		JGeometry multiCurveGeom = null;
		
		if (multiCurveProperty != null) {
			MultiCurve multiCurve = multiCurveProperty.getMultiCurve();
			
			if (multiCurve != null && multiCurve.getCurveMember() != null && !multiCurve.getCurveMember().isEmpty()) {
				List<CurveProperty> multiCurveList = multiCurve.getCurveMember();
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				
				for (CurveProperty curveProperty : multiCurveList) {
					AbstractCurve curve = curveProperty.getCurve();
					
					if (curve != null) {
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

					multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, Integer.valueOf(dbSrid));
				}
			}
		}
		
		return multiCurveGeom;
	}
	
	public JGeometry getMultiCurve(GeometricComplex geometricComplex) {
		JGeometry multiCurveGeom = null;
		
		if (geometricComplex != null) {
			List<GeometricPrimitiveProperty> primitivPropertyList = geometricComplex.getElement();
			if (primitivPropertyList != null) {
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				
				for (GeometricPrimitiveProperty primitiveProperty : primitivPropertyList) {
					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();
					
					if (primitive != null) {
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

					multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, Integer.valueOf(dbSrid));
				}
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
			if (curve.getSegments() != null) {
				CurveSegmentArrayProperty arrayProperty = curve.getSegments();
				
				if (arrayProperty.getCurveSegment() != null && !arrayProperty.getCurveSegment().isEmpty()) {
					List<Double> points = new ArrayList<Double>();
					
					for (AbstractCurveSegment abstractCurveSegment : arrayProperty.getCurveSegment()) {
						if (abstractCurveSegment.getGMLClass() == GMLClass.LINESTRINGSEGMENT)
							points.addAll(((LineStringSegment)abstractCurveSegment).toList());
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
		
		else if (abstractCurve.getGMLClass() == GMLClass.COMPOSITECURVE) {		
			CompositeCurve compositeCurve = (CompositeCurve)abstractCurve;
			if (compositeCurve.getCurveMember() != null && !compositeCurve.getCurveMember().isEmpty()) {		
				for (CurveProperty curveProperty : compositeCurve.getCurveMember()) {
					if (curveProperty.getCurve() != null)
						generatePointList(curveProperty.getCurve(), pointList, reverse);
					else {
						//xlinks are not allowed here...
					}
				}
			}
		} 
		
		else if (abstractCurve.getGMLClass() == GMLClass.ORIENTABLECURVE) {			
			OrientableCurve orientableCurve = (OrientableCurve)abstractCurve;
			String orientation = orientableCurve.getOrientation();
			if (orientation != null && orientation.equals("-"))
				reverse = !reverse;
			
			if (orientableCurve.getBaseCurve() != null) {
				CurveProperty curveProperty = orientableCurve.getBaseCurve();
				
				if (curveProperty.getCurve() != null)
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
				if (property.getLineStringSegment() != null && !property.getLineStringSegment().isEmpty()) {
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

				multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, Integer.valueOf(dbSrid));
			}
		}
		
		return multiCurveGeom;
	}
	
	public JGeometry getPolygon(PolygonProperty polygonProperty) {
		JGeometry polygonGeom = null;
		
		if (polygonProperty != null && polygonProperty.getPolygon() != null) {
			Polygon polygon = polygonProperty.getPolygon();
			
			// exterior
			if (polygon.getExterior() != null) {
				LinearRing exteriorLinearRing = (LinearRing)polygon.getExterior().getRing();
				List<List<Double>> pointList = new ArrayList<List<Double>>();
				
				if (exteriorLinearRing != null) {
					List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList();
					
					if (points != null && !points.isEmpty()) {
						pointList.add(points);
						
						if (polygon.getInterior() != null) {
							List<AbstractRingProperty> abstractRingPropertyList = polygon.getInterior();
							for (AbstractRingProperty abstractRingProperty : abstractRingPropertyList) {
								LinearRing interiorLinearRing = (LinearRing)abstractRingProperty.getRing();
								List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList();

								if (interiorPoints != null && !interiorPoints.isEmpty())
									pointList.add(interiorPoints);			
							}
						}
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
					
					polygonGeom = JGeometry.createLinearPolygon(pointArray, 3, Integer.valueOf(dbSrid));
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
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.SDO_GEOMETRY;
	}

}
