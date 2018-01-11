package org.citydb.database.schema.path.predicate.logical;

import org.citydb.database.schema.path.predicate.PredicateName;

public enum LogicalPredicateName implements PredicateName {
	AND("and"),
	OR("or");

	final String symbol;

	LogicalPredicateName(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}
	
}
