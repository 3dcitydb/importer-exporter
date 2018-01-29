package org.citydb.query.filter.selection.operator.comparison;

import java.util.EnumSet;

import org.citydb.query.filter.selection.operator.OperatorName;

public enum ComparisonOperatorName implements OperatorName {
	EQUAL_TO("="),
	NOT_EQUAL_TO("<>"),
	LESS_THAN("<"),
	GREATER_THAN(">"),
	LESS_THAN_OR_EQUAL_TO("<="),
	GREATER_THAN_OR_EQUAL_TO(">="),
	BETWEEN("BETWEEN"),
	LIKE("LIKE"),
	NULL("IS_NULL");
	
	public static final EnumSet<ComparisonOperatorName> BINARY_COMPARISONS = EnumSet.of(
			EQUAL_TO, NOT_EQUAL_TO, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN_OR_EQUAL_TO);
	
	final String symbol;
	
	ComparisonOperatorName(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}
}
