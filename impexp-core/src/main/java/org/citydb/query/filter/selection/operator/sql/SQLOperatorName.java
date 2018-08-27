package org.citydb.query.filter.selection.operator.sql;

import org.citydb.query.filter.selection.operator.OperatorName;

public enum SQLOperatorName implements OperatorName {
    SELECT("SQL Select");

    final String symbol;

    SQLOperatorName(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }
}
