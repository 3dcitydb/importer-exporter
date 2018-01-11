package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractDistanceOperatorType", propOrder={
		"distance"
})
@XmlSeeAlso({
	BeyondOperator.class,
	DWithinOperator.class
})
public abstract class AbstractDistanceOperator extends AbstractBinarySpatialOperator {
	@XmlElement(required = true)
	private Distance distance; 
	
	public Distance getDistance() {
		return distance;
	}
	
	public boolean isSetDistance() {
		return distance != null;
	}

	public void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	@Override
	public void reset() {
		distance = null;
	}
	
}
