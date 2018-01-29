package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.path.predicate.PredicateName;

public abstract class AbstractNodePredicate {
	protected AbstractNode<? extends AbstractPathElement> contextNode;
	public abstract boolean isEqualTo(AbstractNodePredicate other);
	public abstract PredicateName getPredicateName();
	public abstract String toString(boolean removeAttributePrefixes);

	@Override
	public String toString() {
		return toString(false);
	}
	
}
