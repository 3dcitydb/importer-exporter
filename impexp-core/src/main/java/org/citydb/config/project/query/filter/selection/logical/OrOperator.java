package org.citydb.config.project.query.filter.selection.logical;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="or")
@XmlType(name="OrOperatorType")
public class OrOperator extends AbstractBinaryLogicalOperator {

	@Override
	public LogicalOperatorName getOperatorName() {
		return LogicalOperatorName.OR;
	}
	
}
