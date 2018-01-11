package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="within")
@XmlType(name="WithinOperatorType")
public class WithinOperator extends AbstractBinarySpatialOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.WITHIN;
	}
	
}
