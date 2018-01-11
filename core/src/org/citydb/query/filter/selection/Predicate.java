package org.citydb.query.filter.selection;

import org.citydb.query.filter.FilterException;

public interface Predicate {
	public PredicateName getPredicateName();
	public Predicate copy() throws FilterException;
}
