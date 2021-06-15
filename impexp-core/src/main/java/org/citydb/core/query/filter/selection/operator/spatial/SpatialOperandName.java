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
