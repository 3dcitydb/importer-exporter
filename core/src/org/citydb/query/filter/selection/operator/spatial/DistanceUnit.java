package org.citydb.query.filter.selection.operator.spatial;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public enum DistanceUnit {
	METER(SI.METER, "m", "metre", "meter"),
	KILOMETER(SI.KILOMETER, "km", "kilometre", "kilometre"),
	CENTIMETER(SI.CENTIMETER, "cm", "centimetre", "centimeter"),
	MILLIMETER(SI.MILLIMETER, "mm", "millimetre", "millimeter"),
	MILE(NonSI.MILE, "mile"),
	NAUTICAL_MILE(NonSI.NAUTICAL_MILE, "nautical mile"),
	FOOT_SURVEY_US(NonSI.FOOT_SURVEY_US, "US survey foot"),
	FOOT(NonSI.FOOT, "foot"),
	INCH(NonSI.INCH, "inch"),
	YARD(NonSI.YARD, "yard"),
	RADIAN(SI.RADIAN, "rad", "radian"),
	DEGREE(SI.RADIAN.times(0.017453292519943295), "degree"),
	DMS(SI.RADIAN.times(0.017453292519943295), "dms");
	
	private final Unit<?> unit;
	private final String[] symbols;
	
	private DistanceUnit(Unit<?> unit, String... symbols) {
		this.unit = unit;
		this.symbols = symbols;
	}
	
	public static DistanceUnit fromSymbol(String symbol) {
		if (symbol == null || symbol.length() == 0)
			return null;
		
		if (symbol.startsWith("#"))
			symbol = symbol.substring(1,symbol.length());
		
		for (DistanceUnit unit : DistanceUnit.values()) {
			for (String tmp : unit.symbols)
				if (tmp.toUpperCase().equals(symbol.toUpperCase()))
					return unit;
		}
		
		return null;
	}

	public Unit<?> toUnit() {
		return unit;
	}
	
	public String getSymbol() {
		return symbols[0];
	}

	@Override
	public String toString() {
		return symbols[0];
	}
	
}
