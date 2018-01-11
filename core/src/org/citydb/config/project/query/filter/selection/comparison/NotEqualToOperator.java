package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsNotEqualTo")
@XmlType(name="NotEqualToOperatorType")
public class NotEqualToOperator extends AbstractBinaryComparisonOperator {

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.NOT_EQUAL_TO;
	}
	
}
