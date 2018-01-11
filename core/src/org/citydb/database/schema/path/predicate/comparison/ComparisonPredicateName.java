package org.citydb.database.schema.path.predicate.comparison;

import org.citydb.database.schema.path.predicate.PredicateName;

public enum ComparisonPredicateName implements PredicateName {
	EQUAL_TO("=");

	final String symbol;

	ComparisonPredicateName(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}
	
}
