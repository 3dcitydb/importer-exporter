package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsGreaterThanOrEqualTo")
@XmlType(name="GreaterThanOrEqualToOperatorType")
public class GreaterThanOrEqualToOperator extends AbstractBinaryComparisonOperator {

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.GREATER_THAN_OR_EQUAL_TO;
	}
	
}
