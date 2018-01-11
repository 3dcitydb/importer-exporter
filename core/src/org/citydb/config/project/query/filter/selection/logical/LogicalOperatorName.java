package org.citydb.config.project.query.filter.selection.logical;

public enum LogicalOperatorName {
	AND("and"),
	OR("or"),
	NOT("not");
	
	final String name;

	LogicalOperatorName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
