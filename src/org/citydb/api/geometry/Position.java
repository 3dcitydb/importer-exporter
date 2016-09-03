package org.citydb.api.geometry;

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "PositionType")
public class Position {	
	@XmlValue
	@XmlList
    private Double[] coords;
	
	public Position() {
		coords = new Double[3];
	}
	
	public Position(Double x, Double y) {
		this();
		coords[0] = x;
		coords[1] = y;
	}
	
	public Position(Double x, Double y, Double z) {
		this();
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	public Position(Double value, int dimension) {
		this();
		
		if (dimension < 2 || dimension > 3)
			throw new IllegalArgumentException("Dimension must be 2 or 3.");
		
		coords[0] = coords[1] = value;
		if (dimension == 3)
			coords[2] =value;
	}

	public Double getX() {
		prepareCoords();
		return coords[0];
	}
	
	public boolean isSetX() {
		return isValid() && coords[0] != null;
	}

	public void setX(Double x) {
		prepareCoords();
		coords[0] = x;
	}

	public Double getY() {
		prepareCoords();
		return coords[1];
	}
	
	public boolean isSetY() {
		return isValid() && coords[1] != null;
	}

	public void setY(Double y) {
		prepareCoords();
		coords[1] = y;
	}
	
	public Double getZ() {
		prepareCoords();
		return coords[2];
	}
	
	public boolean isSetZ() {
		return isValid() && coords.length > 2 && coords[2] != null;
	}

	public void setZ(Double z) {
		prepareCoords();
		coords[2] = z;
	}
	
	public boolean is3D() {
		return isSetZ();
	}
	
	public boolean isValid() {
		return coords != null && coords.length > 1;
	}
	
	private void prepareCoords() {
		if (!isValid())
			coords = new Double[3];
	}
	
}
