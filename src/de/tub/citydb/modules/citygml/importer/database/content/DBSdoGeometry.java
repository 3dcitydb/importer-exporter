/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import org.citygml4j.impl.gml.geometry.primitives.LinearRingImpl;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurve;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurveSegment;
import org.citygml4j.model.gml.geometry.primitives.AbstractGeometricPrimitive;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.Curve;
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.GeometricPositionGroup;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableCurve;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBSdoGeometry implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	private final DBImporterManager dbImporterManager;
	
	private int dbSrid;
	private boolean affineTransformation;
	
	public DBSdoGeometry(Config config, DBImporterManager dbImporterManager) {
		this.dbImporterManager = dbImporterManager;
		
		dbSrid = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().getSrid();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
	}

	public JGeometry getPoint(PointProperty pointProperty) {
		JGeometry pointGeom = null;

		if (pointProperty != null && pointProperty.isSetPoint()) {
			Point point = pointProperty.getPoint();
			List<Double> values = point.toList3d();

			if (values != null && !values.isEmpty()) {
				if (affineTransformation)
					dbImporterManager.getAffineTransformer().transformCoordinates(values);
				
				double[] coords = new double[values.size()];
				
				int i = 0;
				for (Double value : values)
					coords[i++] = value.doubleValue();

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
						pointList.add(pointProperty.getPoint().toList3d());

			} else if (multiPoint.isSetPointMembers()) {
				PointArrayProperty pointArrayProperty = multiPoint.getPointMembers();
				for (Point point : pointArrayProperty.getPoint())
					pointList.add(point.toList3d());
			}

			if (!pointList.isEmpty()) {				
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					if (affineTransformation)
						dbImporterManager.getAffineTransformer().transformCoordinates(coordsList);
					
					double[] coords = new double[3];

					coords[0] = coordsList.get(0).doubleValue();
					coords[1] = coordsList.get(1).doubleValue();
					coords[2] = coordsList.get(2).doubleValue();

					pointArray[i++] = coords;					
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

			if (!pointList.isEmpty()) {
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					if (affineTransformation)
						dbImporterManager.getAffineTransformer().transformCoordinates(coordsList);
					
					double[] coords = new double[3];

					coords[0] = coordsList.get(0).doubleValue();
					coords[1] = coordsList.get(1).doubleValue();
					coords[2] = coordsList.get(2).doubleValue();

					pointArray[i++] = coords;					
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
						if (affineTransformation)
							dbImporterManager.getAffineTransformer().transformCoordinates(coordsList);
						
						double[] coords = new double[coordsList.size()];

						int j = 0;
						for (Double coord : coordsList)
							coords[j++] = coord.doubleValue();

						pointArray[i++] = coords;					
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
					case LINE_STRING:
					case COMPOSITE_CURVE:
					case ORIENTABLE_CURVE:
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
					if (affineTransformation)
						dbImporterManager.getAffineTransformer().transformCoordinates(coordsList);
					
					double[] coords = new double[coordsList.size()];

					int j = 0;
					for (Double coord : coordsList)
						coords[j++] = coord.doubleValue();

					pointArray[i++] = coords;					
				}

				multiCurveGeom = JGeometry.createLinearMultiLineString(pointArray, 3, dbSrid);
			}
		}

		return multiCurveGeom;
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

	public JGeometry getMultiCurve(List<LineStringSegmentArrayProperty> propertyList) {
		JGeometry multiCurveGeom = null;

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

			if (!pointList.isEmpty()) {
				Object[] pointArray = new Object[pointList.size()];
				int i = 0;
				for (List<Double> coordsList : pointList) {
					if (affineTransformation)
						dbImporterManager.getAffineTransformer().transformCoordinates(coordsList);
					
					double[] coords = new double[coordsList.size()];

					int j = 0;
					for (Double coord : coordsList)
						coords[j++] = coord.doubleValue();

					pointArray[i++] = coords;					
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
					List<Double> points = ((LinearRingImpl)exteriorLinearRing).toList3d();

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
						
						if (affineTransformation)
							dbImporterManager.getAffineTransformer().transformCoordinates(points);
						
						pointList.add(points);

						if (polygon.getInterior() != null) {
							List<AbstractRingProperty> abstractRingPropertyList = polygon.getInterior();
							for (AbstractRingProperty abstractRingProperty : abstractRingPropertyList) {
								LinearRing interiorLinearRing = (LinearRing)abstractRingProperty.getRing();
								List<Double> interiorPoints = ((LinearRingImpl)interiorLinearRing).toList3d();

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
									
									if (affineTransformation)
										dbImporterManager.getAffineTransformer().transformCoordinates(interiorPoints);
									
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
						for (Double coord : coordsList)
							coords[j++] = coord.doubleValue();								

						pointArray[i++] = coords;					
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
