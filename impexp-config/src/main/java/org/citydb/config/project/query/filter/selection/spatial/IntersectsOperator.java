package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="intersects")
@XmlType(name="IntersectsOperatorType")
public class IntersectsOperator extends AbstractBinarySpatialOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.INTERSECTS;
	}
	
}
