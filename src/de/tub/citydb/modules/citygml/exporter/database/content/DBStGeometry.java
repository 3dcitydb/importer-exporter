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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.impl.gml.geometry.GeometryPropertyImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiCurveImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiCurvePropertyImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiPointImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiPointPropertyImpl;
import org.citygml4j.impl.gml.geometry.complexes.GeometricComplexImpl;
import org.citygml4j.impl.gml.geometry.complexes.GeometricComplexPropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.ControlPointImpl;
import org.citygml4j.impl.gml.geometry.primitives.CurvePropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.DirectPositionImpl;
import org.citygml4j.impl.gml.geometry.primitives.DirectPositionListImpl;
import org.citygml4j.impl.gml.geometry.primitives.ExteriorImpl;
import org.citygml4j.impl.gml.geometry.primitives.GeometricPositionGroupImpl;
import org.citygml4j.impl.gml.geometry.primitives.GeometricPrimitivePropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.InteriorImpl;
import org.citygml4j.impl.gml.geometry.primitives.LineStringImpl;
import org.citygml4j.impl.gml.geometry.primitives.LineStringSegmentArrayPropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.LineStringSegmentImpl;
import org.citygml4j.impl.gml.geometry.primitives.LinearRingImpl;
import org.citygml4j.impl.gml.geometry.primitives.PointImpl;
import org.citygml4j.impl.gml.geometry.primitives.PointPropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.PolygonImpl;
import org.citygml4j.impl.gml.geometry.primitives.PolygonPropertyImpl;
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
import org.postgis.Geometry;
import org.postgis.MultiLineString;

import de.tub.citydb.config.Config;

public class DBStGeometry implements DBExporter {
	private String gmlSrsName;

	public DBStGeometry(Config config) {
		gmlSrsName = config.getInternal().getExportTargetSRS().getGMLSrsName();
	}

	public Point getPoint(Geometry geom, boolean setSrsName) {
		Point point = null;

		if (geom != null && geom.getType() == Geometry.POINT) {
			int dimensions = geom.getDimension();

			if (dimensions == 2) {
				point = new PointImpl();

				List<Double> value = new ArrayList<Double>();
				value.add(geom.getPoint(0).getX());
				value.add(geom.getPoint(0).getY());			
				
				DirectPosition pos = new DirectPositionImpl();
				pos.setValue(value);
				pos.setSrsDimension(dimensions);
				if (setSrsName)
					pos.setSrsName(gmlSrsName);
				point.setPos(pos);
			}
			
			if (dimensions == 3) {
				point = new PointImpl();

				List<Double> value = new ArrayList<Double>();
				value.add(geom.getPoint(0).getX());
				value.add(geom.getPoint(0).getY());
				value.add(geom.getPoint(0).getZ());
				
				DirectPosition pos = new DirectPositionImpl();
				pos.setValue(value);
				pos.setSrsDimension(dimensions);
				if (setSrsName)
					pos.setSrsName(gmlSrsName);
				point.setPos(pos);
			}
		}

		return point;
	}
	
	public PointProperty getPointProperty(Geometry geom, boolean setSrsName) {
		PointProperty pointProperty = null;

		Point point = getPoint(geom, setSrsName);
		if (point != null) {
			pointProperty = new PointPropertyImpl();
			pointProperty.setPoint(point);
		}

		return pointProperty;
	}
	
	public MultiPoint getMultiPoint(Geometry geom, boolean setSrsName) {
		MultiPoint multiPoint = null;

		if (geom != null) {
			multiPoint = new MultiPointImpl();
			int dimensions = geom.getDimension();
			
			if (geom.getType() == Geometry.MULTIPOINT) {
				List<Double> value = new ArrayList<Double>();
				Point point = new PointImpl();
				
				if (dimensions == 2)
					
					for (int i = 0; i < geom.numPoints(); i++) {					
						value.add(geom.getPoint(i).x);
						value.add(geom.getPoint(i).y);
					}
				
				if (dimensions == 3)
					
					for (int i = 0; i < geom.numPoints(); i++) {					
						value.add(geom.getPoint(i).x);
						value.add(geom.getPoint(i).y);
						value.add(geom.getPoint(i).z);
					}
				
				DirectPosition pos = new DirectPositionImpl();
				pos.setValue(value);
				pos.setSrsDimension(dimensions);
				if (setSrsName)
					pos.setSrsName(gmlSrsName);
				point.setPos(pos);

				PointProperty pointProperty = new PointPropertyImpl();
				pointProperty.setPoint(point);

				multiPoint.addPointMember(pointProperty);
			}			
			else if (geom.getType() == Geometry.POINT) {
				Point point = getPoint(geom, setSrsName);
				if (point != null) {
					PointProperty pointProperty = new PointPropertyImpl();
					pointProperty.setPoint(point);
					multiPoint.addPointMember(pointProperty);
				}
			}
			
			if (!multiPoint.isSetPointMember())
				multiPoint = null;
		} 

		return multiPoint;
	} 

	public MultiPointProperty getMultiPointProperty(Geometry geom, boolean setSrsName) {
		MultiPointProperty multiPointProperty = null;

		MultiPoint multiPoint = getMultiPoint(geom, setSrsName);
		if (multiPoint != null) {
			multiPointProperty = new MultiPointPropertyImpl();
			multiPointProperty.setMultiPoint(multiPoint);
		}

		return multiPointProperty;
	} 
	
	public ControlPoint getControlPoint(Geometry geom, boolean setSrsName) {
		ControlPoint controlPoint = null;

		if (geom != null) {
			controlPoint = new ControlPointImpl();

			if (geom.getType() == Geometry.MULTIPOINT) {	
				MultiPoint multiPoint = getMultiPoint(geom, setSrsName);
				if (multiPoint != null) {
					for (PointProperty pointProperty : multiPoint.getPointMember()) {
						GeometricPositionGroup group = new GeometricPositionGroupImpl(pointProperty.getPoint().getPos());
						controlPoint.addGeometricPositionGroup(group);
					}
				}
			}

			else if (geom.getType() == Geometry.POINT) {
				Point point = getPoint(geom, setSrsName);
				if (point != null) {
					GeometricPositionGroup group = new GeometricPositionGroupImpl(point.getPos());
					controlPoint.addGeometricPositionGroup(group);
				}
			}

			if (!controlPoint.isSetGeometricPositionGroup())
				controlPoint = null;
		}

		return controlPoint;
	}

	public LineString getLineString(Geometry geom, boolean setSrsName) {
		LineString lineString = null;

		if (geom != null && geom.getType() == Geometry.LINESTRING) {
			int dimensions = geom.getDimension();
			
			List<Double> value = new ArrayList<Double>();

			if (dimensions == 2)
				for (int i = 0; i < geom.numPoints(); i++){
					value.add(geom.getPoint(i).x);
					value.add(geom.getPoint(i).y);
				}
			
			if (dimensions == 3)
				for (int i = 0; i < geom.numPoints(); i++){
					value.add(geom.getPoint(i).x);
					value.add(geom.getPoint(i).y);
					value.add(geom.getPoint(i).z);
				}

			lineString = new LineStringImpl();
			DirectPositionList directPositionList = new DirectPositionListImpl();

			directPositionList.setValue(value);
			directPositionList.setSrsDimension(dimensions);
			if (setSrsName)
				directPositionList.setSrsName(gmlSrsName);
			lineString.setPosList(directPositionList);	
		}

		return lineString;
	}

	public CurveProperty getCurveProperty(Geometry geom, boolean setSrsName) {
		CurveProperty curveProperty = null;

		LineString lineString = getLineString(geom, setSrsName);
		if (lineString != null) {
			curveProperty = new CurvePropertyImpl();
			curveProperty.setCurve(lineString);
		}

		return curveProperty;
	}
	
	public MultiCurve getMultiCurve(Geometry geom, boolean setSrsName) {
		MultiCurve multiCurve = null;
				
		if (geom != null) {
			multiCurve = new MultiCurveImpl();
			int dimensions = geom.getDimension();

			if (geom.getType() == Geometry.MULTILINESTRING) {
								
				MultiLineString mlineGeom = (MultiLineString)geom;
				
				for (int i = 0; i < mlineGeom.numLines(); i++){
					List<Double> values = new ArrayList<Double>();
					
					if (dimensions == 2)
						for (int j = 0; j < mlineGeom.getLine(i).numPoints(); j++){
							values.add(mlineGeom.getLine(i).getPoint(j).x);
							values.add(mlineGeom.getLine(i).getPoint(j).y);
						}
					
					if (dimensions == 3)
						for (int j = 0; j < mlineGeom.getLine(i).numPoints(); j++){
							values.add(mlineGeom.getLine(i).getPoint(j).x);
							values.add(mlineGeom.getLine(i).getPoint(j).y);
							values.add(mlineGeom.getLine(i).getPoint(j).z);
						}
					
					LineString lineString = new LineStringImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(dimensions);
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);
					lineString.setPosList(directPositionList);

					CurveProperty curveProperty = new CurvePropertyImpl();
					curveProperty.setCurve(lineString);					
					multiCurve.addCurveMember(curveProperty);
				}
			}
			else if (geom.getType() == Geometry.LINESTRING) {
				LineString lineString = getLineString(geom, setSrsName);
				if (lineString != null) {
					CurveProperty curveProperty = new CurvePropertyImpl();
					curveProperty.setCurve(lineString);				
					multiCurve.addCurveMember(curveProperty);
				}			
			}

			if (!multiCurve.isSetCurveMember())
				multiCurve = null;		
		}

		return multiCurve;
	}

	public MultiCurveProperty getMultiCurveProperty(Geometry geom, boolean setSrsName) {
		MultiCurveProperty multiCurveProperty = null;

		MultiCurve multiCurve = getMultiCurve(geom, setSrsName);
		if (multiCurve != null) {
			multiCurveProperty = new MultiCurvePropertyImpl();
			multiCurveProperty.setMultiCurve(multiCurve);
		}

		return multiCurveProperty;
	}
	
	public List<LineStringSegmentArrayProperty> getListOfLineStringSegmentArrayProperty(Geometry geom, boolean setSrsName) {
		List<LineStringSegmentArrayProperty> arrayPropertyList = new ArrayList<LineStringSegmentArrayProperty>();
		
		if (geom != null) {
			arrayPropertyList = new ArrayList<LineStringSegmentArrayProperty>();

			if (geom.getType() == Geometry.MULTILINESTRING) {
				MultiCurve multiCurve = getMultiCurve(geom, setSrsName);
				if (multiCurve != null) {
					for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
						LineStringSegment lineStringSegment = new LineStringSegmentImpl();
						lineStringSegment.setPosList(((LineString)curveProperty.getCurve()).getPosList());

						LineStringSegmentArrayProperty arrayProperty = new LineStringSegmentArrayPropertyImpl();
						arrayProperty.addLineStringSegment(lineStringSegment);
						arrayPropertyList.add(arrayProperty);
					}
				}
			}

			else if (geom.getType() == Geometry.LINESTRING) {
				LineString lineString = getLineString(geom, setSrsName);
				if (lineString != null) {
					LineStringSegment lineStringSegment = new LineStringSegmentImpl();
					lineStringSegment.setPosList(lineString.getPosList());

					LineStringSegmentArrayProperty arrayProperty = new LineStringSegmentArrayPropertyImpl();
					arrayProperty.addLineStringSegment(lineStringSegment);
					arrayPropertyList.add(arrayProperty);
				}			
			}

			if (arrayPropertyList.isEmpty())
				arrayPropertyList = null;
		}

		return arrayPropertyList;
	}
	
	public GeometricComplex getPointOrCurveComplex(Geometry geom, boolean setSrsName) {
		GeometricComplex complex = null;
		
		if (geom != null) {
			complex = new GeometricComplexImpl();

			if (geom.getType() == Geometry.MULTILINESTRING) {
				MultiCurve multiCurve = getMultiCurve(geom, setSrsName);
				if (multiCurve != null) {
					for (CurveProperty curveProperty : multiCurve.getCurveMember()) {
						GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
						primitiveProperty.setGeometricPrimitive((LineString)curveProperty.getCurve());					
						complex.addElement(primitiveProperty);
					}
				}
			}

			else if (geom.getType() == Geometry.LINESTRING) {
				LineString lineString = getLineString(geom, setSrsName);
				if (lineString != null) {
					GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
					primitiveProperty.setGeometricPrimitive(lineString);					
					complex.addElement(primitiveProperty);		
				}			
			}

			else if (geom.getType() == Geometry.MULTIPOINT) {
				MultiPoint multiPoint = getMultiPoint(geom, setSrsName);
				if (multiPoint != null) {
					for (PointProperty pointProperty : multiPoint.getPointMember()) {
						GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
						primitiveProperty.setGeometricPrimitive(pointProperty.getPoint());					
						complex.addElement(primitiveProperty);	
					}
				}
			}

			else if (geom.getType() == Geometry.POINT) {
				Point point = getPoint(geom, setSrsName);
				if (point != null) {
					GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
					primitiveProperty.setGeometricPrimitive(point);					
					complex.addElement(primitiveProperty);	
				}
			}

			if (!complex.isSetElement())
				complex = null;
		}

		return complex;
	}
	
	public GeometricComplexProperty getPointOrCurveComplexProperty(Geometry geom, boolean setSrsName) {
		GeometricComplexProperty complexProperty = null;
		
		GeometricComplex complex = getPointOrCurveComplex(geom, setSrsName);
		if (complex != null) {
			complexProperty = new GeometricComplexPropertyImpl();
			complexProperty.setGeometricComplex(complex);
		}

		return complexProperty;
	}
	
	public AbstractGeometry getPointOrCurveGeometry(Geometry geom, boolean setSrsName) {
		if (geom != null) {
			switch (geom.getType()) {
			case Geometry.MULTILINESTRING:
				return getMultiCurve(geom, setSrsName);
			case Geometry.LINESTRING:
				return getLineString(geom, setSrsName);
			case Geometry.MULTIPOINT:
				return getMultiPoint(geom, setSrsName);
			case Geometry.POINT:
				return getPoint(geom, setSrsName);
			}
		}

		return null;
	}
	
	public GeometryProperty<? extends AbstractGeometry> getPointOrCurveGeometryProperty(Geometry geom, boolean setSrsName) {
		GeometryProperty<AbstractGeometry> geometryProperty = null;
		
		AbstractGeometry geometry = getPointOrCurveGeometry(geom, setSrsName);
		if (geometry != null) {
			geometryProperty = new GeometryPropertyImpl<AbstractGeometry>();
			geometryProperty.setGeometry(geometry);
		}
		
		return geometryProperty;
	}
	
	public Polygon getPolygon(Geometry geom, boolean setSrsName) {
		Polygon polygon = null;
		
		if (geom != null && geom.getType() == Geometry.POLYGON) {
			polygon = new PolygonImpl();
			int dimensions = geom.getDimension();
			
			if (geom.getValue() == null)
				return null;

			org.postgis.Polygon polyGeom = (org.postgis.Polygon) geom;
						
			for (int i = 0; i < polyGeom.numRings(); i++){
				List<Double> values = new ArrayList<Double>();
				
				if (dimensions == 2)
					for (int j = 0; j < polyGeom.getRing(i).numPoints(); j++){
						values.add(polyGeom.getRing(i).getPoint(j).x);
						values.add(polyGeom.getRing(i).getPoint(j).y);
					}
				
				if (dimensions == 3)
					for (int j = 0; j < polyGeom.getRing(i).numPoints(); j++){
						values.add(polyGeom.getRing(i).getPoint(j).x);
						values.add(polyGeom.getRing(i).getPoint(j).y);
						values.add(polyGeom.getRing(i).getPoint(j).z);
					}
					
				//isExterior
				if (i == 0) {
					LinearRing linearRing = new LinearRingImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(dimensions);
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);
					linearRing.setPosList(directPositionList);				

					Exterior exterior = new ExteriorImpl();
					exterior.setRing(linearRing);
					polygon.setExterior(exterior);

				} else {
				//isInterior
					LinearRing linearRing = new LinearRingImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(dimensions);
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);
					linearRing.setPosList(directPositionList);

					Interior interior = new InteriorImpl();
					interior.setRing(linearRing);
					polygon.addInterior(interior);
				}
			}
		}
		
		return polygon;
	}

	public PolygonProperty getPolygonProperty(Geometry geom, boolean setSrsName) {
		PolygonProperty polygonProperty = null;

		Polygon polygon = getPolygon(geom, setSrsName);
		if (polygon != null && (polygon.isSetExterior() || polygon.isSetInterior())) {
			polygonProperty = new PolygonPropertyImpl();
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
		return DBExporterEnum.ST_GEOMETRY;
	}

}
