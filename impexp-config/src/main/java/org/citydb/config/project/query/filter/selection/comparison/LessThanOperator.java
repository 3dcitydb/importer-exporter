package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsLessThan")
@XmlType(name="LessThanOperatorType")
public class LessThanOperator extends AbstractBinaryComparisonOperator {

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.LESS_THAN;
	}
	
}
