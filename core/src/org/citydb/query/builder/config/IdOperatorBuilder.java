package org.citydb.query.builder.config;

import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;

public class IdOperatorBuilder {
	
	protected IdOperatorBuilder() {
		
	}

	protected Predicate buildResourceIdOperator(org.citydb.config.project.query.filter.selection.id.ResourceIdOperator idOperatorConfig) throws QueryBuildException {
		ResourceIdOperator idOperator = new ResourceIdOperator();
		for (String id : idOperatorConfig.getResourceIds()) {
			if (id != null && !id.isEmpty())
				idOperator.addResourceId(id);
		}
		
		if (idOperator.isEmpty())
			throw new QueryBuildException("No valid gml:ids provided for resource id filter.");

		return idOperator;
	}
	
}
