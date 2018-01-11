package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="contains")
@XmlType(name="ContainsOperatorType")
public class ContainsOperator extends AbstractBinarySpatialOperator {

	@Override
	public SpatialOperatorName getOperatorName() {
		return SpatialOperatorName.CONTAINS;
	}
	
}
