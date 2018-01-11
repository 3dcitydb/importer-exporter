package org.citydb.config.project.query.filter.selection.logical;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;

@XmlRootElement(name="not")
@XmlType(name="NotOperatorType", propOrder={
		"operand"
})
public class NotOperator extends AbstractLogicalOperator {
	@XmlElementRef
	private AbstractPredicate operand;
	
	public boolean isSetOperand() {
		return operand != null;
	}

	public AbstractPredicate getOperand() {
		return operand;
	}

	public void setOperand(AbstractPredicate operand) {
		this.operand = operand;
	}
	
	@Override
	public void reset() {
		operand = null;
	}
	
	@Override
	public LogicalOperatorName getOperatorName() {
		return LogicalOperatorName.NOT;
	}
	
}
