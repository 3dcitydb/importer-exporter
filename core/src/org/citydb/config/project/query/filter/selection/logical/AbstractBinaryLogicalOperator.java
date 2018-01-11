package org.citydb.config.project.query.filter.selection.logical;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;

@XmlType(name="AbstractBinaryLogicalOperatorType", propOrder={
		"operands"
})
@XmlSeeAlso({
	AndOperator.class,
	OrOperator.class
})
public abstract class AbstractBinaryLogicalOperator extends AbstractLogicalOperator {
	@XmlElementRef
	private List<AbstractPredicate> operands;
	
	public AbstractBinaryLogicalOperator() {
		operands = new ArrayList<>();
	}
	
	public boolean isSetOperands() {
		return operands != null;
	}

	public List<AbstractPredicate> getOperands() {
		return operands;
	}

	public void setOperands(List<AbstractPredicate> operands) {
		if (operands != null && !operands.isEmpty())
			this.operands = operands;
	}
	
	public int numberOfOperands() {
		return operands != null ? operands.size() : 0;
	}

	@Override
	public void reset() {
		operands.clear();
	}

}
