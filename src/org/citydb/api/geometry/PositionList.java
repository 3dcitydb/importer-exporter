package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "PositionListType")
public class PositionList {
	@XmlAttribute(required=false)
	private Integer dimension = 2;
	@XmlValue
	@XmlList
    private List<Double> coords;
	
	public PositionList() {
		coords = new ArrayList<>();
	}

	public List<Double> getCoords() {
		return coords;
	}

	public void setCoords(List<Double> coords) {
		this.coords = coords;
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
		return coords != null && !coords.isEmpty() && coords.size() % getDimension() == 0;
	}
	
}