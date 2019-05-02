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
package org.citydb.config.project.query.filter.selection.logical;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

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
