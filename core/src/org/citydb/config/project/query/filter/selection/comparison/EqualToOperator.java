package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsEqualTo")
@XmlType(name="EqualToOperatorType")
public class EqualToOperator extends AbstractBinaryComparisonOperator {

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.EQUAL_TO;
	}
	
}
