package de.tub.citydb.filter.statistic;

import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.FilterConfig;
import de.tub.citydb.config.project.filter.FilterFeatureCount;
import de.tub.citydb.filter.Filter;
import de.tub.citydb.filter.FilterMode;

public class FeatureCounterFilter implements Filter<Long> {
	private final FilterConfig filter;
	private boolean isActive;
	private FilterFeatureCount featureCountFilter;

	public FeatureCounterFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filter = config.getProject().getExporter().getFilter();
		else
			filter = config.getProject().getImporter().getFilter();
			
		init();
	}
	
	private void init() {
		isActive = filter.isSetComplex() &&
			filter.getComplexFilter().getFeatureCountFilter().isSet();
		
		if (isActive)
			featureCountFilter = filter.getComplexFilter().getFeatureCountFilter();
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}
	
	public void reset() {
		init();
	}
	
	public boolean filter(Long number) {
		if (isActive) {			
			Long firstElement = featureCountFilter.getFrom();
			Long lastElement = featureCountFilter.getTo();
			
			if (firstElement != null && number < firstElement)
				return true;
			
			if (lastElement != null && number > lastElement)
				return true;
		}
		
		return false;
	}
	
	public List<Long> getFilterState() {
		return getInternalState(false);
	}
	
	public List<Long> getNotFilterState() {
		return getInternalState(true);
	}
	
	private List<Long> getInternalState(boolean inverse) {
		List<Long> state = new ArrayList<Long>();
		
		if (isActive) {						
			Long firstElement = featureCountFilter.getFrom();
			Long lastElement = featureCountFilter.getTo();
			
			if (!inverse)
				state.add(firstElement);
			else 
				state.add(lastElement);
			
			if (!inverse)
				state.add(lastElement);
			else 
				state.add(firstElement);
		} else {
			state.add(null);
			state.add(null);
		}		
		
		return state;
	}
}
