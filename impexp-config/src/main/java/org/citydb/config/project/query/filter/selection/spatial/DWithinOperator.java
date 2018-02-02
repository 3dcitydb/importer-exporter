package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="dWithin")
@XmlType(name="DWithinOperatorType")
public class DWithinOperator extends AbstractDistanceOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.DWITHIN;
	}
	
}
