/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.query.filter.selection.operator.logical;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
