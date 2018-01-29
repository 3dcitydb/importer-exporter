package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.Operator;

public abstract class AbstractComparisonOperator implements Operator {

	@Override
	public PredicateName getPredicateName() {
		return PredicateName.COMPARISON_OPERATOR;
	}

	@Override
	public abstract ComparisonOperatorName getOperatorName();
	
}
