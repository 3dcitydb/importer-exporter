package org.citydb.query.filter.selection.operator.logical;

import java.util.EnumSet;

import org.citydb.query.filter.selection.operator.OperatorName;

public enum LogicalOperatorName implements OperatorName {
	AND("AND"),
	OR("OR"),
	NOT("NOT");
	
	public static final EnumSet<LogicalOperatorName> BINARY_OPERATIONS = EnumSet.of(AND, OR);	
	public static final EnumSet<LogicalOperatorName> UNARY_OPERATIONS = EnumSet.of(NOT);
	
	final String symbol;
	
	LogicalOperatorName(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}
}
