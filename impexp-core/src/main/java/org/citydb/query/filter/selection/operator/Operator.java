package org.citydb.query.filter.selection.operator;

import org.citydb.query.filter.selection.Predicate;

public interface Operator extends Predicate {
	public OperatorName getOperatorName();
}
