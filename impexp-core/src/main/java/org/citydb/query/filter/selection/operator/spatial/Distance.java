package org.citydb.query.filter.selection.operator.spatial;

import javax.measure.unit.Dimension;

public class Distance {
	private double value;
	private DistanceUnit unit;

	public Distance(double value, DistanceUnit unit) {
		this.value = value;
		setUnit(unit);
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isSetUnit() {
		return unit != null;
	}

	public boolean isLinearUnit() {
		return unit != null && unit.toUnit().getDimension() == Dimension.LENGTH;
	}

	public boolean isAngularUnit() {
		return unit != null && unit.toUnit().getDimension() == Dimension.NONE;
	}

	public DistanceUnit getUnit() {
		return unit;
	}

	public void setUnit(DistanceUnit unit) {
		if (unit != null && (unit.toUnit().getDimension() == Dimension.LENGTH || unit.toUnit().getDimension() == Dimension.NONE))
			this.unit = unit;
	}

}
