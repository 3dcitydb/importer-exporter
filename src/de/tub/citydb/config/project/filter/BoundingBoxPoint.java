package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="BoundingBoxPointType", propOrder={
		"x",
		"y"
		})
public class BoundingBoxPoint {
	private Double x;
	private Double y;
	
	public BoundingBoxPoint() {
	}

	public BoundingBoxPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}
	
}