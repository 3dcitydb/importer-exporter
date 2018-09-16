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
package org.citydb.database.schema.mapping;

import java.util.EnumSet;

public enum PathElementType {
	FEATURE_TYPE,
	FEATURE_PROPERTY,
	SIMPLE_ATTRIBUTE,
	COMPLEX_ATTRIBUTE,
	GEOMETRY_PROPERTY,
	COMPLEX_TYPE,
	COMPLEX_PROPERTY,
	OBJECT_TYPE,
	OBJECT_PROPERTY,
	IMPLICIT_GEOMETRY_PROPERTY;
	
	public static final EnumSet<PathElementType> TYPES = EnumSet.of(FEATURE_TYPE, OBJECT_TYPE, COMPLEX_TYPE);
	public static final EnumSet<PathElementType> OBJECT_TYPES = EnumSet.of(FEATURE_TYPE, OBJECT_TYPE);
	public static final EnumSet<PathElementType> TYPE_PROPERTIES = EnumSet.of(FEATURE_PROPERTY, OBJECT_PROPERTY, COMPLEX_PROPERTY, IMPLICIT_GEOMETRY_PROPERTY);
	public static final EnumSet<PathElementType> GEOMETRY_PROPERTIES = EnumSet.of(GEOMETRY_PROPERTY, IMPLICIT_GEOMETRY_PROPERTY);
}
