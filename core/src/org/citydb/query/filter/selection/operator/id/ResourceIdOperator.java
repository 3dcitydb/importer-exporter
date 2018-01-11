package org.citydb.query.filter.selection.operator.id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.citydb.query.filter.FilterException;

public class ResourceIdOperator extends AbstractIdOperator {
	private HashSet<String> resourceIds;
	
	public ResourceIdOperator() {
		resourceIds = new HashSet<String>();
	}
	
	public ResourceIdOperator(Collection<String> resourceIds) throws FilterException {
		if (resourceIds == null)
			throw new FilterException("List of resource ids may not be null.");
		
		this.resourceIds = new HashSet<>(resourceIds);
	}
	
	public ResourceIdOperator(String... resourceIds) throws FilterException {
		this(Arrays.asList(resourceIds));
	}
	
	public boolean isEmpty() {
		return resourceIds.isEmpty();
	}
	
	public void clear() {
		resourceIds.clear();
	}

	public int numberOfResourceIds() {
		return resourceIds.size();
	}
	
	public boolean addResourceId(String resourceId) {
		return resourceIds.add(resourceId);
	}
	
	public HashSet<String> getResourceIds() {
		return resourceIds;
	}
	
	@Override
	public IdOperationName getOperatorName() {
		return IdOperationName.RESOURCE_ID;
	}

	@Override
	public ResourceIdOperator copy() throws FilterException {
		return new ResourceIdOperator(new ArrayList<>(resourceIds));
	}

}
