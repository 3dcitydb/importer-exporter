package org.citydb.query.filter.selection.operator.logical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;

public class BinaryLogicalOperator extends AbstractLogicalOperator {
	private List<Predicate> operands;
	private final LogicalOperatorName name;
	
	public BinaryLogicalOperator(LogicalOperatorName name) {
		this.name = name;
		operands = new ArrayList<Predicate>();
	}

	public BinaryLogicalOperator(LogicalOperatorName name, List<Predicate> operands) throws FilterException {
		if (!LogicalOperatorName.BINARY_OPERATIONS.contains(name))
			throw new FilterException("Allowed binary comparisons only include " + LogicalOperatorName.BINARY_OPERATIONS);

		if (operands == null)
			throw new FilterException("List of operands may not be null.");
		
		this.operands = operands;
		this.name = name;
	}
	
	public BinaryLogicalOperator(LogicalOperatorName name, Predicate... operands) throws FilterException {
		this(name, Arrays.asList(operands));
	}

	public int numberOfOperands() {
		return operands != null ? operands.size() : 0;
	}

	public void clear() {
		if (operands != null)
			operands.clear();
	}

	public boolean addOperand(Predicate predicate) {
		return operands.add(predicate);
	}

	public List<Predicate> getOperands() {
		return operands;
	}

	@Override
	public LogicalOperatorName getOperatorName() {
		return name;
	}

	@Override
	public BinaryLogicalOperator copy() throws FilterException {
		BinaryLogicalOperator copy = new BinaryLogicalOperator(name);
		for (Predicate operand : operands)
			copy.addOperand(operand.copy());
		
		return copy;
	}

}
