package org.citydb.config.project.query.filter.selection;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.comparison.AbstractComparisonOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.logical.AbstractLogicalOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator;

@XmlType(name="AbstractPredicateType")
@XmlSeeAlso({
	AbstractLogicalOperator.class,
	AbstractComparisonOperator.class,
	AbstractSpatialOperator.class,
	ResourceIdOperator.class
})
public abstract class AbstractPredicate {
	public abstract void reset();
	public abstract PredicateName getPredicateName();	
}
