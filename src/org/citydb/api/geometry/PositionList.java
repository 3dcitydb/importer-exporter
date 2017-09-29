package org.citydb.api.geometry;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "PositionListType")
public class PositionList {
	@XmlAttribute(required=false)
	private Integer dimension = 2;
	@XmlValue
    private DoubleList coords;
	
	public PositionList() {
		coords = new DoubleList();
	}

	public List<Double> getCoords() {
		return coords.getValues();
	}

	public void setCoords(List<Double> coords) {
		this.coords.setValues(coords);
	}
	
	public int getDimension() {
		return dimension != null ? dimension : 2;
	}

	public void setDimension(int dimension) {
		if (dimension < 2 || dimension > 3)
			throw new IllegalArgumentException("Dimension must be 2 or 3.");
		
		this.dimension = dimension;
	}

	public boolean isValid() {
		return coords.getValues() != null && !coords.getValues().isEmpty() && coords.getValues().size() % getDimension() == 0;
	}
	
}