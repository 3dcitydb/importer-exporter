package org.citydb.query.filter.selection.operator.id;

import org.citydb.query.filter.selection.operator.OperatorName;

public enum IdOperationName implements OperatorName {
	RESOURCE_ID("gml:id");
	
	final String symbol;
	
	IdOperationName(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}
}
