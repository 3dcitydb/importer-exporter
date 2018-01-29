package org.citydb.config.project.query.filter.selection.spatial;

public enum SpatialOperatorName {
	EQUALS("equals"),
	DISJOINT("disjoint"),
	TOUCHES("touches"),
	WITHIN("within"),
	OVERLAPS("overlaps"),
	INTERSECTS("intersects"),
	CONTAINS("contains"),
	BBOX("bbox"),
	DWITHIN("dWithin"),
	BEYOND("beyond");
	
	final String name;

	SpatialOperatorName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
