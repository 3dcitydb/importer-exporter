package org.citydb.config.project.query.filter.selection.logical;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="and")
@XmlType(name="AndOperatorType")
public class AndOperator extends AbstractBinaryLogicalOperator {

	@Override
	public LogicalOperatorName getOperatorName() {
		return LogicalOperatorName.AND;
	}

}
