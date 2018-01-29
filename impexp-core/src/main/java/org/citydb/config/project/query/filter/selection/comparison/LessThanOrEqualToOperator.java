package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsLessThanOrEqualTo")
@XmlType(name="LessThanOrEqualToOperatorType")
public class LessThanOrEqualToOperator extends AbstractBinaryComparisonOperator {

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.LESS_THAN_OR_EQUAL_TO;
	}
	
}
