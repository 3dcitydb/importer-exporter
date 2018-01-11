package org.citydb.query.filter.lod;

public enum LodFilterMode {
	AND("AND"),
	OR("OR");

	final String symbol;

	LodFilterMode(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
