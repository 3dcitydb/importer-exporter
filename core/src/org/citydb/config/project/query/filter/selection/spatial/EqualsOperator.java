package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="equals")
@XmlType(name="EqualsOperatorType")
public class EqualsOperator extends AbstractBinarySpatialOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.EQUALS;
	}
	
}
