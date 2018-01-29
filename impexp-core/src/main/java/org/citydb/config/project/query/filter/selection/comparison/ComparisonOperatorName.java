package org.citydb.config.project.query.filter.selection.comparison;

public enum ComparisonOperatorName {
	EQUAL_TO("propertyIsEqualTo"),
	NOT_EQUAL_TO("propertyIsNotEqualTo"),
	LESS_THAN("propertyIsLessThan"),
	GREATER_THAN("propertyIsGreaterThan"),
	LESS_THAN_OR_EQUAL_TO("propertyIsLessThanOrEqualTo"),
	GREATER_THAN_OR_EQUAL_TO("propertyIsGreaterThanOrEqualTo"),
	BETWEEN("propertyIsBetween"),
	LIKE("propertyIsLike"),
	NULL("propertyIsNull");

	final String name;

	ComparisonOperatorName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
