package org.citydb.query.builder.config;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.counter.CounterFilter;

public class CounterFilterBuilder {

	protected CounterFilterBuilder() {
		
	}
	
	public CounterFilter buildCounterFilter(org.citydb.config.project.query.filter.counter.CounterFilter counterFilterConfig) throws FilterException {
		if (!counterFilterConfig.isSetUpperLimit())
			throw new FilterException("Upper counter limit must not be null.");
		
		long upperLimit = counterFilterConfig.getUpperLimit().longValue();
		long lowerLimit = counterFilterConfig.isSetLowerLimit() ? counterFilterConfig.getLowerLimit().longValue() : 1;
		
		return new CounterFilter(lowerLimit, upperLimit);
	}
}
