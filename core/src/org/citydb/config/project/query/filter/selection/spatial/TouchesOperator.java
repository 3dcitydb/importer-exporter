package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="touches")
@XmlType(name="TouchesOperatorType")
public class TouchesOperator extends AbstractBinarySpatialOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.TOUCHES;
	}
	
}
