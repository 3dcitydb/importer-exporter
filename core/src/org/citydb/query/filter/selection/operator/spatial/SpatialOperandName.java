package org.citydb.query.filter.selection.operator.spatial;

import org.citygml4j.model.gml.GMLClass;

public enum SpatialOperandName {
	ENVELOPE("Envelope", GMLClass.ENVELOPE),
	POINT("Point", GMLClass.POINT),
	MULTI_POINT("MultiPoint", GMLClass.MULTI_POINT),
	LINE_STRING("LineString", GMLClass.LINE_STRING),
	MULTI_LINE_STRING("MultiLineString", GMLClass.MULTI_LINE_STRING),
	CURVE("Curve", GMLClass.CURVE),
	MULTI_CURVE("MultiCurve", GMLClass.MULTI_CURVE),
	POLYGON("Polygon", GMLClass.POLYGON),
	MULTI_POLYGON("MultiPolygon", GMLClass.MULTI_POLYGON),
	SURFACE("Surface", GMLClass.SURFACE),
	MULTI_SURFACE("MultiSurface", GMLClass.MULTI_SURFACE);
	
	private String name;
	private GMLClass type;
	
	SpatialOperandName(String name, GMLClass type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public GMLClass getType() {
		return type;
	}
	
	public static boolean contains(GMLClass type) {
		for (SpatialOperandName operand : values())
			if (operand.type == type)
				return true;
		
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
	
}
