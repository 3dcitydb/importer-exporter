package org.citydb.config.project.query.filter.selection.spatial;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name="DistanceType")
public class Distance {
	@XmlAttribute
	private String uom;
	@XmlValue
	private double value;
	
	public String getUom() {
		return uom;
	}
	
	public boolean isSetUom() {
		return uom != null;
	}
	
	public void setUom(String uom) {
		this.uom = uom;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
}
