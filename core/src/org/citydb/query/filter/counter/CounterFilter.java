package org.citydb.query.filter.counter;

import org.citydb.query.filter.FilterException;

public class CounterFilter {
	private final long lowerLimit;
	private final long upperLimit;
	
	public CounterFilter(long lowerLimit, long upperLimit) throws FilterException {
		if (lowerLimit <= 0 || upperLimit <= 0)
			throw new FilterException("Counter limits must be greater than zero.");
		
		if (lowerLimit > upperLimit)
			throw new FilterException("The upper counter limit must be greater than or equal to the lower limit.");
		
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}
	
	public CounterFilter(long upperLimit) throws FilterException {
		this(1, upperLimit);
	}
	
	public long getLowerLimit() {
		return lowerLimit;
	}

	public long getUpperLimit() {
		return upperLimit;
	}
	
}
