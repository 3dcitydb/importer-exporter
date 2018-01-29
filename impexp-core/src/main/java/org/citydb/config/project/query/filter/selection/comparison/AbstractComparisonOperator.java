package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

@XmlType(name="AbstractComparisonOperatorType", propOrder={
		"valueReference"
})
@XmlSeeAlso({
	AbstractBinaryComparisonOperator.class,
	BetweenOperator.class,
	LikeOperator.class,
	NullOperator.class
})
public abstract class AbstractComparisonOperator extends AbstractPredicate {
	@XmlElement(required = true)
	private String valueReference;
	
	public abstract ComparisonOperatorName getOperatorName();
	
	public boolean isSetValueReference() {
		return valueReference != null;
	}

	public String getValueReference() {
		return valueReference;
	}

	public void setValueReference(String valueReference) {
		this.valueReference = valueReference;
	}

	@Override
	public void reset() {
		valueReference = null;
	}

	@Override
	public PredicateName getPredicateName() {
		return PredicateName.COMPARISON_OPERATOR;
	}
	
}
