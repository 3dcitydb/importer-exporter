package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "DoubleListType")
public class DoubleList {
	@XmlValue
    private List<Double> values;
	
	public DoubleList() {
		values = new ArrayList<>();
	}
	
	public List<Double> getValues() {
		return values;
	}

	public void setValues(List<Double> values) {
		this.values = values;
	}
	
}
