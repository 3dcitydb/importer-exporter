package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsGreaterThan")
@XmlType(name="GreaterThanOperatorType")
public class GreaterThanOperator extends AbstractBinaryComparisonOperator {

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.GREATER_THAN;
	}
	
}
