package org.citydb.config.project.query.filter.selection.comparison;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="propertyIsBetween")
@XmlType(name="BetweenOperatorType", propOrder={
		"lowerBoundary",
		"upperBoundary"
})
public class BetweenOperator extends AbstractComparisonOperator {
	@XmlElement(required = true)
	private String lowerBoundary;
	@XmlElement(required = true)
	private String upperBoundary;
	
	public boolean isSetLowerBoundary() {
		return lowerBoundary != null;
	}

	public String getLowerBoundary() {
		return lowerBoundary;
	}

	public void setLowerBoundary(String lowerBoundary) {
		this.lowerBoundary = lowerBoundary;
	}
	
	public boolean isSetUpperBoundary() {
		return upperBoundary != null;
	}

	public String getUpperBoundary() {
		return upperBoundary;
	}

	public void setUpperBoundary(String upperBoundary) {
		this.upperBoundary = upperBoundary;
	}
	
	@Override
	public void reset() {
		lowerBoundary = null;
		upperBoundary = null;
		super.reset();
	}

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.BETWEEN;
	}

}
