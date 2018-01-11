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
