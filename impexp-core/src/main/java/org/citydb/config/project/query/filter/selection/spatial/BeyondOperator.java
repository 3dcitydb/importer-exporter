package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="beyond")
@XmlType(name="BeyondOperatorType")
public class BeyondOperator extends AbstractDistanceOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.BEYOND;
	}
	
}
