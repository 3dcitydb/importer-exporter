/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.query.filter.selection.operator.spatial;

import org.geotools.measure.Units;
import si.uom.NonSI;
import si.uom.SI;
import systems.uom.common.USCustomary;

import javax.measure.MetricPrefix;
import javax.measure.Unit;

public enum DistanceUnit {
    METER(SI.METRE, "m", "metre", "meter"),
    KILOMETER(MetricPrefix.KILO(SI.METRE), "km", "kilometre", "kilometre"),
    CENTIMETER(MetricPrefix.CENTI(SI.METRE), "cm", "centimetre", "centimeter"),
    MILLIMETER(MetricPrefix.MILLI(SI.METRE), "mm", "millimetre", "millimeter"),
    MILE(USCustomary.MILE, "mi", "mile"),
    NAUTICAL_MILE(USCustomary.NAUTICAL_MILE, "nmi", "nautical mile"),
    FOOT_SURVEY_US(USCustomary.FOOT_SURVEY, "ft_survey_us", "US survey foot"),
    FOOT(USCustomary.FOOT, "ft", "foot"),
    INCH(USCustomary.INCH, "in", "inch"),
    YARD(USCustomary.YARD, "yd", "yard"),
    RADIAN(SI.RADIAN, "rad", "radian"),
    DEGREE(NonSI.DEGREE_ANGLE, "deg", "degree"),
    DMS(Units.DEGREE_MINUTE_SECOND, "dms");

    private final Unit<?> unit;
    private final String[] symbols;

    DistanceUnit(Unit<?> unit, String... symbols) {
        this.unit = unit;
        this.symbols = symbols;
    }

    public static DistanceUnit fromSymbol(String symbol) {
        if (symbol == null || symbol.length() == 0) {
            return null;
        }

        if (symbol.startsWith("#")) {
            symbol = symbol.substring(1);
        }

        for (DistanceUnit unit : DistanceUnit.values()) {
            for (String tmp : unit.symbols) {
                if (tmp.equalsIgnoreCase(symbol)) {
                    return unit;
                }
            }
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
