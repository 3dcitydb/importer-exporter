package de.tub.citydb.db.exporter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import org.citygml4j.impl.jaxb.gml._3_1_1.ControlPointImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.CurvePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.DirectPositionImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.DirectPositionListImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.ExteriorImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometricComplexImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometricComplexPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometricPositionGroupImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometricPrimitivePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.InteriorImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.LineStringImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.LineStringSegmentArrayPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.LineStringSegmentImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.LinearRingImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiCurveImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiCurvePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiPointImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiPointPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.PointImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.PointPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.PolygonImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.PolygonPropertyImpl;
import org.citygml4j.model.gml.ControlPoint;
import org.citygml4j.model.gml.CurveProperty;
import org.citygml4j.model.gml.DirectPosition;
import org.citygml4j.model.gml.DirectPositionList;
import org.citygml4j.model.gml.Exterior;
import org.citygml4j.model.gml.GeometricComplex;
import org.citygml4j.model.gml.GeometricComplexProperty;
import org.citygml4j.model.gml.GeometricPositionGroup;
import org.citygml4j.model.gml.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.Interior;
import org.citygml4j.model.gml.LineString;
import org.citygml4j.model.gml.LineStringSegment;
import org.citygml4j.model.gml.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.LinearRing;
import org.citygml4j.model.gml.MultiCurve;
import org.citygml4j.model.gml.MultiCurveProperty;
import org.citygml4j.model.gml.MultiPoint;
import org.citygml4j.model.gml.MultiPointProperty;
import org.citygml4j.model.gml.Point;
import org.citygml4j.model.gml.PointProperty;
import org.citygml4j.model.gml.Polygon;
import org.citygml4j.model.gml.PolygonProperty;

import de.tub.citydb.config.Config;

public class DBSdoGeometry implements DBExporter {
	private String gmlSrsName;

	public DBSdoGeometry(Config config) {
		if (config.getInternal().isTransformCoordinates())
			gmlSrsName = config.getInternal().getExportTargetSRS().getSrsName();
		else
			gmlSrsName = config.getInternal().getOpenConnection().getMetaData().getSrsName();
	}

	public PointProperty getPoint(JGeometry geom, boolean setSrsName) {
		PointProperty pointProperty = null;

		if (geom != null && geom.getType() == JGeometry.GTYPE_POINT) {
			pointProperty = new PointPropertyImpl();
			int dimensions = geom.getDimensions();

			double[] pointCoord = geom.getPoint();

			if (pointCoord != null && pointCoord.length >= dimensions) {
				Point point = new PointImpl();

				List<Double> value = new ArrayList<Double>();
				for (int i = 0; i < dimensions; i++)
					value.add(pointCoord[i]);

				DirectPosition pos = new DirectPositionImpl();
				pos.setValue(value);
				pos.setSrsDimension(dimensions);
				if (setSrsName)
					pos.setSrsName(gmlSrsName);
				point.setPos(pos);

				pointProperty.setPoint(point);
			}
		}

		return pointProperty;
	}
	
	public PolygonProperty getPolygon(JGeometry geom, boolean setSrsName) {
		PolygonProperty polygonProperty = null;
		
		if (geom != null && geom.getType() == JGeometry.GTYPE_POLYGON) {
			polygonProperty = new PolygonPropertyImpl();
			Polygon polygon = new PolygonImpl();
			int dimensions = geom.getDimensions();
			
			int[] elemInfoArray = geom.getElemInfo();
			double[] ordinatesArray = geom.getOrdinatesArray();

			if (elemInfoArray.length < 3 || ordinatesArray.length == 0)
				return null;
			
			List<Integer> ringLimits = new ArrayList<Integer>();
			for (int i = 3; i < elemInfoArray.length; i += 3)
				ringLimits.add(elemInfoArray[i] - 1);

			ringLimits.add(ordinatesArray.length);
			
			boolean isExterior = elemInfoArray[1] == 1003;
			int ringElem = 0;
			for (Integer curveLimit : ringLimits) {
				List<Double> values = new ArrayList<Double>();

				for ( ; ringElem < curveLimit; ringElem++)
					values.add(ordinatesArray[ringElem]);
				
				if (isExterior) {
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

					isExterior = false;
				} else {
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
			
			if (polygon.isSetExterior() || polygon.isSetInterior())
				polygonProperty.setPolygon(polygon);
		}
		
		return polygonProperty;
	}
	
	public MultiPointProperty getMultiPointProperty(JGeometry geom, boolean setSrsName) {
		MultiPointProperty multiPointProperty = null;

		if (geom != null) {
			multiPointProperty = new MultiPointPropertyImpl();
			MultiPoint multiPoint = new MultiPointImpl();
			int dimensions = geom.getDimensions();

			if (geom.getType() == JGeometry.GTYPE_MULTIPOINT) {								
				double[] ordinates = geom.getOrdinatesArray();

				for (int i = 0; i < ordinates.length; i += dimensions) {
					Point point = new PointImpl();

					List<Double> value = new ArrayList<Double>();

					for (int j = 0; j < dimensions; j++)
						value.add(ordinates[i + j]);

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
			} else if (geom.getType() == JGeometry.GTYPE_POINT) {
				double[] pointCoord = geom.getPoint();

				if (pointCoord != null && pointCoord.length >= dimensions) {
					Point point = new PointImpl();

					List<Double> value = new ArrayList<Double>();
					for (int i = 0; i < dimensions; i++)
						value.add(pointCoord[i]);

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
			}

			if (multiPoint.isSetPointMember())
				multiPointProperty.setMultiPoint(multiPoint);
		} 

		return multiPointProperty;
	} 

	public ControlPoint getControlPoint(JGeometry geom, boolean setSrsName) {
		ControlPoint controlPoint = null;
		
		if (geom != null) {
			controlPoint = new ControlPointImpl();
			int dimensions = geom.getDimensions();
			
			if (geom.getType() == JGeometry.GTYPE_MULTIPOINT) {								
				double[] ordinates = geom.getOrdinatesArray();

				for (int i = 0; i < ordinates.length; i += dimensions) {			
					List<Double> value = new ArrayList<Double>();

					for (int j = 0; j < dimensions; j++)
						value.add(ordinates[i + j]);

					DirectPosition pos = new DirectPositionImpl();
					pos.setValue(value);
					pos.setSrsDimension(dimensions);
					if (setSrsName)
						pos.setSrsName(gmlSrsName);
					
					GeometricPositionGroup group = new GeometricPositionGroupImpl(pos);
					controlPoint.addGeometricPositionGroup(group);
				}
			}
			
			else if (geom.getType() == JGeometry.GTYPE_POINT) {
				double[] pointCoord = geom.getPoint();

				if (pointCoord != null && pointCoord.length >= dimensions) {
					List<Double> value = new ArrayList<Double>();
					for (int i = 0; i < dimensions; i++)
						value.add(pointCoord[i]);

					DirectPosition pos = new DirectPositionImpl();
					pos.setValue(value);
					pos.setSrsDimension(dimensions);
					if (setSrsName)
						pos.setSrsName(gmlSrsName);
					
					GeometricPositionGroup group = new GeometricPositionGroupImpl(pos);
					controlPoint.addGeometricPositionGroup(group);
				}
			}
			
			if (!controlPoint.isSetGeometricPositionGroup())
				controlPoint = null;
		}
		
		return controlPoint;
	}
	
	public MultiCurveProperty getMultiCurveProperty(JGeometry geom, boolean setSrsName) {
		MultiCurveProperty multiCurveProperty = null;

		if (geom != null) {
			multiCurveProperty = new MultiCurvePropertyImpl();
			MultiCurve multiCurve = new MultiCurveImpl();
			int dimensions = geom.getDimensions();

			if (geom.getType() == JGeometry.GTYPE_MULTICURVE) {
				int[] elemInfoArray = geom.getElemInfo();
				double[] ordinatesArray = geom.getOrdinatesArray();

				if (elemInfoArray.length < 3 || ordinatesArray.length == 0)
					return null;

				List<Integer> curveLimits = new ArrayList<Integer>();
				for (int i = 3; i < elemInfoArray.length; i += 3)
					curveLimits.add(elemInfoArray[i] - 1);

				curveLimits.add(ordinatesArray.length);

				int curveElem = 0;
				for (Integer curveLimit : curveLimits) {
					List<Double> values = new ArrayList<Double>();

					for ( ; curveElem < curveLimit; curveElem++)
						values.add(ordinatesArray[curveElem]);

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

					curveElem = curveLimit;
				}
			}

			else if (geom.getType() == JGeometry.GTYPE_CURVE) {
				double[] ordinatesArray = geom.getOrdinatesArray();
				List<Double> value = new ArrayList<Double>();

				for (int i = 0; i < ordinatesArray.length; i++)
					value.add(ordinatesArray[i]);

				LineString lineString = new LineStringImpl();
				DirectPositionList directPositionList = new DirectPositionListImpl();

				directPositionList.setValue(value);
				directPositionList.setSrsDimension(dimensions);
				if (setSrsName)
					directPositionList.setSrsName(gmlSrsName);
				lineString.setPosList(directPositionList);

				CurveProperty curveProperty = new CurvePropertyImpl();
				curveProperty.setCurve(lineString);				
				multiCurve.addCurveMember(curveProperty);				
			}

			if (multiCurve.isSetCurveMember())
				multiCurveProperty.setMultiCurve(multiCurve);
		}

		return multiCurveProperty;
	}

	public List<LineStringSegmentArrayProperty> getListOfLineStringSegmentArrayProperty(JGeometry geom, boolean setSrsName) {
		List<LineStringSegmentArrayProperty> arrayPropertyList = new ArrayList<LineStringSegmentArrayProperty>();
		
		if (geom != null) {
			int dimensions = geom.getDimensions();
			
			if (geom.getType() == JGeometry.GTYPE_MULTICURVE) {
				int[] elemInfoArray = geom.getElemInfo();
				double[] ordinatesArray = geom.getOrdinatesArray();

				if (elemInfoArray.length < 3 || ordinatesArray.length == 0)
					return null;

				List<Integer> curveLimits = new ArrayList<Integer>();
				for (int i = 3; i < elemInfoArray.length; i += 3)
					curveLimits.add(elemInfoArray[i] - 1);

				curveLimits.add(ordinatesArray.length);
				
				int curveElem = 0;
				for (Integer curveLimit : curveLimits) {
					List<Double> values = new ArrayList<Double>();

					for ( ; curveElem < curveLimit; curveElem++)
						values.add(ordinatesArray[curveElem]);

					LineStringSegment lineStringSegment = new LineStringSegmentImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(dimensions);
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);
					lineStringSegment.setPosList(directPositionList);
					
					LineStringSegmentArrayProperty arrayProperty = new LineStringSegmentArrayPropertyImpl();
					arrayProperty.addLineStringSegment(lineStringSegment);
					arrayPropertyList.add(arrayProperty);
										
					curveElem = curveLimit;
				}
			}
			
			else if (geom.getType() == JGeometry.GTYPE_CURVE) {
				double[] ordinatesArray = geom.getOrdinatesArray();
				List<Double> value = new ArrayList<Double>();

				for (int i = 0; i < ordinatesArray.length; i++)
					value.add(ordinatesArray[i]);

				LineStringSegment lineStringSegment = new LineStringSegmentImpl();
				DirectPositionList directPositionList = new DirectPositionListImpl();

				directPositionList.setValue(value);
				directPositionList.setSrsDimension(dimensions);
				if (setSrsName)
					directPositionList.setSrsName(gmlSrsName);
				lineStringSegment.setPosList(directPositionList);

				LineStringSegmentArrayProperty arrayProperty = new LineStringSegmentArrayPropertyImpl();
				arrayProperty.addLineStringSegment(lineStringSegment);
				arrayPropertyList.add(arrayProperty);			
			}
		}
				
		if (!arrayPropertyList.isEmpty())
			return arrayPropertyList;
		
		return null;
	}
	
	public GeometricComplexProperty getGeometricComplexPropertyOfCurves(JGeometry geom, boolean setSrsName) {
		GeometricComplexProperty complexProperty = null;
		
		if (geom != null) {
			complexProperty = new GeometricComplexPropertyImpl();
			GeometricComplex complex = new GeometricComplexImpl();
			int dimensions = geom.getDimensions();
			
			if (geom.getType() == JGeometry.GTYPE_MULTICURVE) {
				int[] elemInfoArray = geom.getElemInfo();
				double[] ordinatesArray = geom.getOrdinatesArray();

				if (elemInfoArray.length < 3 || ordinatesArray.length == 0)
					return null;

				List<Integer> curveLimits = new ArrayList<Integer>();
				for (int i = 3; i < elemInfoArray.length; i += 3)
					curveLimits.add(elemInfoArray[i] - 1);

				curveLimits.add(ordinatesArray.length);

				int curveElem = 0;
				for (Integer curveLimit : curveLimits) {
					List<Double> values = new ArrayList<Double>();

					for ( ; curveElem < curveLimit; curveElem++)
						values.add(ordinatesArray[curveElem]);

					LineString lineString = new LineStringImpl();
					DirectPositionList directPositionList = new DirectPositionListImpl();

					directPositionList.setValue(values);
					directPositionList.setSrsDimension(dimensions);
					if (setSrsName)
						directPositionList.setSrsName(gmlSrsName);
					lineString.setPosList(directPositionList);

					GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
					primitiveProperty.setGeometricPrimitive(lineString);					
					complex.addElement(primitiveProperty);

					curveElem = curveLimit;
				}
			}
			
			else if (geom.getType() == JGeometry.GTYPE_CURVE) {
				double[] ordinatesArray = geom.getOrdinatesArray();
				List<Double> value = new ArrayList<Double>();

				for (int i = 0; i < ordinatesArray.length; i++)
					value.add(ordinatesArray[i]);

				LineString lineString = new LineStringImpl();
				DirectPositionList directPositionList = new DirectPositionListImpl();

				directPositionList.setValue(value);
				directPositionList.setSrsDimension(dimensions);
				if (setSrsName)
					directPositionList.setSrsName(gmlSrsName);
				lineString.setPosList(directPositionList);

				GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
				primitiveProperty.setGeometricPrimitive(lineString);					
				complex.addElement(primitiveProperty);				
			}
			
			if (complex.isSetElement())
				complexProperty.setGeometricComplex(complex);
		}
		
		return complexProperty;
	}
	
	@Override
	public void close() throws SQLException {
		// nothing to do here
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.SDO_GEOMETRY;
	}

}
