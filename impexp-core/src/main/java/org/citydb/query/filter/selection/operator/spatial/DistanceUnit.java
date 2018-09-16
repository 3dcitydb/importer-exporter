/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
