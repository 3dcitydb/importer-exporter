package org.citydb.query.filter.selection.operator.logical;

import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.Operator;

public abstract class AbstractLogicalOperator implements Operator {

	@Override
	public PredicateName getPredicateName() {
		return PredicateName.LOGICAL_OPERATOR;
	}
	
}
