package org.citydb.query.filter.selection.operator.spatial;

import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.Operator;

public abstract class AbstractSpatialOperator implements Operator {

	@Override
	public PredicateName getPredicateName() {
		return PredicateName.SPATIAL_OPERATOR;
	}

	@Override
	public abstract SpatialOperatorName getOperatorName();
	
}
