package org.citydb.query.filter.selection.operator.spatial;

import java.util.EnumSet;

import org.citydb.query.filter.selection.operator.OperatorName;

public enum SpatialOperatorName implements OperatorName {
	EQUALS("Equals"),
	DISJOINT("Disjoint"),
	TOUCHES("Touches"),
	WITHIN("Within"),
	OVERLAPS("Overlaps"),
	INTERSECTS("Intersects"),
	CONTAINS("Contains"),
	BBOX("BBOX"),
	DWITHIN("DWithin"),
	BEYOND("Beyond");
	
	public static final EnumSet<SpatialOperatorName> BINARY_SPATIAL_OPERATORS = EnumSet.of(
			BBOX, EQUALS, DISJOINT, TOUCHES, WITHIN, OVERLAPS, INTERSECTS, CONTAINS);

	public static final EnumSet<SpatialOperatorName> DISTANCE_OPERATORS = EnumSet.of(DWITHIN, BEYOND);
	
	final String symbol;
	
	SpatialOperatorName(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}
}
