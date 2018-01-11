package org.citydb.config.project.query.filter.selection.logical;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

@XmlType(name="AbstractLogicalOperatorType")
@XmlSeeAlso({
	AbstractBinaryLogicalOperator.class,
	NotOperator.class
})
public abstract class AbstractLogicalOperator extends AbstractPredicate {
	public abstract LogicalOperatorName getOperatorName();
	
	@Override
	public PredicateName getPredicateName() {
		return PredicateName.LOGICAL_OPERATOR;
	}
}
