package org.citydb.query.filter.selection.operator.id;

import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.Operator;

public abstract class AbstractIdOperator implements Operator {

	@Override
	public PredicateName getPredicateName() {
		return PredicateName.ID_OPERATOR;
	}
	
	@Override
	public abstract IdOperationName getOperatorName();
}
