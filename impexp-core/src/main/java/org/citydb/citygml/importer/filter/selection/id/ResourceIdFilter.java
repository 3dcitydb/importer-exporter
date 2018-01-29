package org.citydb.citygml.importer.filter.selection.id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.citygml.core.AbstractCityObject;

public class ResourceIdFilter {
	private final List<String> ids;
	
	public ResourceIdFilter(ResourceIdOperator idOperator) throws FilterException {
		if (idOperator == null)
			throw new FilterException("The resource id operator must not be null.");
		
		if (idOperator.isSetResourceIds())
			ids = new ArrayList<>(idOperator.getResourceIds());
		else
			ids = Collections.emptyList();
	}
	
	public boolean isSatisfiedBy(AbstractCityObject cityObject) {
		return cityObject.isSetId() ? ids.contains(cityObject.getId()) : false;
	}
	
}
