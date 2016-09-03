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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.api.geometry.GeometryType;
import org.citydb.config.Config;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.GeometricPositionGroup;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;

public class DBOtherGeometry implements DBExporter {
	private String gmlSrsName;

	public DBOtherGeometry(Config config) {
		gmlSrsName = config.getInternal().getExportTargetSRS().getGMLSrsName();
	}

	public Point getPoint(GeometryObject geomObj, boolean setSrsName) {
		Point point = null;

		if (geomObj != null && geomObj.getGeometryType() == GeometryType.POINT) {
			point = new Point();
			int dimension = geomObj.getDimension();
			double[] coordinates = geomObj.getCoordinates(0);

			List<Double> value = new ArrayList<Double>(dimension);
			for (int i = 0; i < dimension; i++)
				value.add(coordinates[i]);

			DirectPosition pos = new DirectPosition();
			pos.setValue(value);
			pos.setSrsDimension(dimension);
			if (setSrsName)
				pos.setSrsName(gmlSrsName);

			point.setPos(pos);
		}

		return point;
	}

	public PointProperty getPointProperty(GeometryObject geomObj, boolean setSrsName) {
		PointProperty pointProperty = null;

		Point point = getPoint(geomObj, setSrsName);
		if (point != null) {
			pointProperty = new PointProperty();
			pointProperty.setPoint(point);
		}

		return pointProperty;
	}

	public MultiPoint getMultiPoint(GeometryObject geomObj, boolean setSrsName) {
		MultiPoint multiPoint = null;

		if (geomObj != null) {
			int dimension = geomObj.getDimension();

			if (geomObj.getGeometryType() == GeometryType.MULTI_POINT) {								
				multiPoint = new MultiPoint();

				for (int i = 0; i < geomObj.getNumElements(); i++) {
					Point point = new Point();
					double[] coordiantes = geomObj.getCoordinates(i);

					List<Double> value = new ArrayList<Double>(dimension);
					for (int j = 0; j < dimension; j++)
						value.add(coordiantes[j]);

					DirectPosition pos = new DirectPosition();
					pos.setValue(value);
					pos.setSrsDimension(dimension);
					if (setSrsName)
						pos.setSrsName(gmlSrsName);

					point.setPos(pos);
					multiPoint.addPointMember(new PointProperty(point));
				}
			} 

			else if (geomObj.getGeometryType() == GeometryType.POINT) {
				multiPoint = new MultiPoint();

				Point point = getPoint(geomObj, setSrsName);
				if (point != null)
					multiPoint.addPointMember(new PointProperty(point));
			}
		} 

		return multiPoint;
	}

	public MultiPointProperty getMultiPointProperty(GeometryObject geomObj, boolean setSrsName) {
		MultiPointProperty multiPointProperty = null;

		MultiPoint multiPoint = getMultiPoint(geomObj, setSrsName);
		if (multiPoint != null) {
			multiPointProperty = new MultiPointProperty();
			multiPointProperty.setMultiPoint(multiPoint);
		}

		return multiPointProperty;
	} 

	public ControlPoint getControlPoint(GeometryObject geomObj, boolean setSrsName) {
		ControlPoint controlPoint = null;

		if (geomObj != null) {
			if (geomObj.getGeometryType() == GeometryType.MULTI_POINT) {
				controlPoint = new ControlPoint();
				MultiPoint multiPoint = getMultiPoint(geomObj, setSrsName);

				if (multiPoint != null) {
					for (PointProperty pointProperty : multiPoint.getPointMember()) {
						GeometricPositionGroup group = new GeometricPositionGroup(pointProperty.getPoint().getPos());
						controlPoint.addGeometricPositionGroup(group);
					}
				}
			}

			else if (geomObj.getGeometryType() == GeometryType.POINT) {
				controlPoint = new ControlPoint();
				Point point = getPoint(geomObj, setSrsName);

				if (point != null) {
					GeometricPositionGroup group = new GeometricPositionGroup(point.getPos());
					controlPoint.addGeometricPositionGroup(group);
				}
			}
		}

		return controlPoint;
	}

	public LineString getLineString(GeometryObject geomObj, boolean setSrsName) {
		LineString lineString = null;

		if (geomObj != null && geomObj.getGeometryType() == GeometryType.LINE_STRING) {
			lineString = new LineString();

			DirectPositionList directPositionList = new DirectPositionList();
			directPositionList.setValue(geomObj.getCoordinatesAsList(0));
			directPositionList.setSrsDimension(geomObj.getDimension());
			if (setSrsName)
				directPositionList.setSrsName(gmlSrsName);

			lineString.setPosList(directPositionList);			
		}

		return lineString;
	}

	public CurveProperty getCurveProperty(GeometryObject geomObj, boolean setSrsName) {
		CurveProperty curveProperty = null;

		LineString lineString = getLineString(geomObj, setSrsName);
		if (lineString != null) {
			curveProperty = new CurveProperty();
			curveProperty.setCurve(lineString);
		}

		return curveProperty;
	}

	public MultiCurve getMultiCurve(GeometryObject geomObj, boolean setSrsName) {
		MultiCurve multiCurve = null;

		if (geomObj != null) {
			if (geomObj.getGeometryType() == GeometryType.MULTI_LINE_STRING) {
				multiCurve = new MultiCurve();

				for (int i = 0; i < geomObj.getNumElements(); i++) {
					LineString lineString = new LineString();
					
					DirectPositionList directPositionList = new DirectPositionList();
					directPositionList.setValue(geomObj.getCoordinatesAsList(i));
					directPositionList.setSrsDimension(geomObj.getDimension());
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);

					lineString.setPosList(directPositionList);
					multiCurve.addCurveMember(new CurveProperty(lineString));
				}
			}

			else if (geomObj.getGeometryType() == GeometryType.LINE_STRING) {
				multiCurve = new MultiCurve();

				LineString lineString = getLineString(geomObj, setSrsName);
				if (lineString != null) 
					multiCurve.addCurveMember(new CurveProperty(lineString));
			}
		}

		return multiCurve;
	}

	public MultiCurveProperty getMultiCurveProperty(GeometryObject geomObj, boolean setSrsName) {
		MultiCurveProperty multiCurveProperty = null;

		MultiCurve multiCurve = getMultiCurve(geomObj, setSrsName);
		if (multiCurve != null) {
			multiCurveProperty = new MultiCurveProperty();
			multiCurveProperty.setMultiCurve(multiCurve);
		}

		return multiCurveProperty;
	}

	public List<LineStringSegmentArrayProperty> getListOfLineStringSegmentArrayProperty(GeometryObject geomObj, boolean setSrsName) {
		List<LineStringSegmentArrayProperty> arrayPropertyList = null;

		if (geomObj != null) {
			if (geomObj.getGeometryType() == GeometryType.MULTI_LINE_STRING) {
				arrayPropertyList = new ArrayList<LineStringSegmentArrayProperty>();
				MultiCurve multiCurve = getMultiCurve(geomObj, setSrsName);
				
				if (multiCurve != null) {
					for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
						LineStringSegment lineStringSegment = new LineStringSegment();
						lineStringSegment.setPosList(((LineString)curveProperty.getCurve()).getPosList());
						arrayPropertyList.add(new LineStringSegmentArrayProperty(lineStringSegment));
					}
				}
			}

			else if (geomObj.getGeometryType() == GeometryType.LINE_STRING) {
				arrayPropertyList = new ArrayList<LineStringSegmentArrayProperty>();
				LineString lineString = getLineString(geomObj, setSrsName);
				
				if (lineString != null) {
					LineStringSegment lineStringSegment = new LineStringSegment();
					lineStringSegment.setPosList(lineString.getPosList());
					arrayPropertyList.add(new LineStringSegmentArrayProperty(lineStringSegment));
				}			
			}
		}

		return arrayPropertyList;
	}

	public GeometricComplex getPointOrCurveComplex(GeometryObject geomObj, boolean setSrsName) {
		GeometricComplex complex = null;

		if (geomObj != null) {
			if (geomObj.getGeometryType() == GeometryType.MULTI_LINE_STRING) {
				complex = new GeometricComplex();
				MultiCurve multiCurve = getMultiCurve(geomObj, setSrsName);
				
				if (multiCurve != null) {
					for (CurveProperty curveProperty : multiCurve.getCurveMember())
						complex.addElement(new GeometricPrimitiveProperty((LineString)curveProperty.getCurve()));
				}
			}

			else if (geomObj.getGeometryType() == GeometryType.LINE_STRING) {
				complex = new GeometricComplex();
				LineString lineString = getLineString(geomObj, setSrsName);
				
				if (lineString != null) 
					complex.addElement(new GeometricPrimitiveProperty(lineString));		
			}

			else if (geomObj.getGeometryType() == GeometryType.MULTI_POINT) {
				complex = new GeometricComplex();
				MultiPoint multiPoint = getMultiPoint(geomObj, setSrsName);
				
				if (multiPoint != null) {
					for (PointProperty pointProperty : multiPoint.getPointMember())
						complex.addElement(new GeometricPrimitiveProperty(pointProperty.getPoint()));	
				}
			}

			else if (geomObj.getGeometryType() == GeometryType.POINT) {
				complex = new GeometricComplex();
				Point point = getPoint(geomObj, setSrsName);
				
				if (point != null)
					complex.addElement(new GeometricPrimitiveProperty(point));	
			}
		}

		return complex;
	}

	public GeometricComplexProperty getPointOrCurveComplexProperty(GeometryObject geomObj, boolean setSrsName) {
		GeometricComplexProperty complexProperty = null;

		GeometricComplex complex = getPointOrCurveComplex(geomObj, setSrsName);
		if (complex != null) {
			complexProperty = new GeometricComplexProperty();
			complexProperty.setGeometricComplex(complex);
		}

		return complexProperty;
	}

	public AbstractGeometry getPointOrCurveGeometry(GeometryObject geomObj, boolean setSrsName) {
		if (geomObj != null) {
			switch (geomObj.getGeometryType()) {
			case MULTI_LINE_STRING:
				return getMultiCurve(geomObj, setSrsName);
			case LINE_STRING:
				return getLineString(geomObj, setSrsName);
			case MULTI_POINT:
				return getMultiPoint(geomObj, setSrsName);
			case POINT:
				return getPoint(geomObj, setSrsName);
			default:
				return null;
			}
		}

		return null;
	}

	public GeometryProperty<? extends AbstractGeometry> getPointOrCurveGeometryProperty(GeometryObject geomObj, boolean setSrsName) {
		GeometryProperty<AbstractGeometry> geometryProperty = null;

		AbstractGeometry geometry = getPointOrCurveGeometry(geomObj, setSrsName);
		if (geometry != null) {
			geometryProperty = new GeometryProperty<AbstractGeometry>();
			geometryProperty.setGeometry(geometry);
		}

		return geometryProperty;
	}

	public Polygon getPolygon(GeometryObject geomObj, boolean setSrsName) {
		Polygon polygon = null;

		if (geomObj != null && geomObj.getGeometryType() == GeometryType.POLYGON) {
			polygon = new Polygon();
			boolean isExterior = true;
			
			for (int i = 0; i < geomObj.getNumElements(); i++) {
				if (isExterior) {
					LinearRing linearRing = new LinearRing();
					
					DirectPositionList directPositionList = new DirectPositionList();
					directPositionList.setValue(geomObj.getCoordinatesAsList(i));
					directPositionList.setSrsDimension(geomObj.getDimension());
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);
					
					linearRing.setPosList(directPositionList);				
					polygon.setExterior(new Exterior(linearRing));

					isExterior = false;
				} else {
					LinearRing linearRing = new LinearRing();
					
					DirectPositionList directPositionList = new DirectPositionList();
					directPositionList.setValue(geomObj.getCoordinatesAsList(i));
					directPositionList.setSrsDimension(geomObj.getDimension());
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);

					linearRing.setPosList(directPositionList);
					polygon.addInterior(new Interior(linearRing));
				}
			}
		}

		return polygon;
	}

	public PolygonProperty getPolygonProperty(GeometryObject geomObj, boolean setSrsName) {
		PolygonProperty polygonProperty = null;

		Polygon polygon = getPolygon(geomObj, setSrsName);
		if (polygon != null && (polygon.isSetExterior() || polygon.isSetInterior())) {
			polygonProperty = new PolygonProperty();
			polygonProperty.setPolygon(polygon);
		}

		return polygonProperty;
	}

	@Override
	public void close() throws SQLException {
		// nothing to do here
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.OTHER_GEOMETRY;
	}

}
