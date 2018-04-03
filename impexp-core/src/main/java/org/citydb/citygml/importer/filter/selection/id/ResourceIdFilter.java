package org.citydb.citygml.importer.filter.selection.id;

import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	
	public boolean isSatisfiedBy(AbstractFeature feature) {
		return feature.isSetId() && ids.contains(feature.getId());
	}
	
}
