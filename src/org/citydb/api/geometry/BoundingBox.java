package org.citydb.api.geometry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;

@XmlRootElement(name="envelope")
@XmlType(name="BoundingBoxType", propOrder = {
		"lowerCorner",
		"upperCorner"
})
public class BoundingBox extends AbstractGeometry {
	@XmlElement(required = true)
	private Position lowerCorner;
	@XmlElement(required = true)
	private Position upperCorner;
	
	public BoundingBox() {
		lowerCorner = new Position();
		upperCorner = new Position();
	}
	
	public BoundingBox(Position lowerCorner, Position upperCorner) {
		this.lowerCorner = lowerCorner;
		this.upperCorner = upperCorner;
	}
	
	public BoundingBox(Position lowerCorner, Position upperCorner, DatabaseSrs srs) {
		this.lowerCorner = lowerCorner;
		this.upperCorner = upperCorner;
		setSrs(srs);
	}
	
	public BoundingBox(BoundingBox other) {
		copyFrom(other);
	}
	
	public Position getLowerCorner() {
		return lowerCorner;
	}

	public void setLowerCorner(Position lowerCorner) {
		this.lowerCorner = lowerCorner;
	}

	public Position getUpperCorner() {
		return upperCorner;
	}

	public void setUpperCorner(Position upperCorner) {
		this.upperCorner = upperCorner;
	}
	
	public void update(Position lowerCorner, Position upperCorner) {
		if (lowerCorner.getX() < this.lowerCorner.getX())
			this.lowerCorner.setX(lowerCorner.getX());

		if (lowerCorner.getY() < this.lowerCorner.getY())
			this.lowerCorner.setY(lowerCorner.getY());
		
		if (upperCorner.getX() > this.upperCorner.getX())
			this.upperCorner.setX(upperCorner.getX());

		if (upperCorner.getY() > this.upperCorner.getY())
			this.upperCorner.setY(upperCorner.getY());
		
		if (is3D()) {
			if (lowerCorner.isSetZ() && lowerCorner.getZ() < this.lowerCorner.getZ())
				this.lowerCorner.setZ(lowerCorner.getZ());
			
			if (upperCorner.isSetZ() && upperCorner.getZ() > this.upperCorner.getZ())
				this.upperCorner.setZ(upperCorner.getZ());
		}
	}
	
	public void copyFrom(BoundingBox other) {
		setSrs(other.getSrs());
		lowerCorner = new Position(other.getLowerCorner().getX(), other.getLowerCorner().getY());
		upperCorner = new Position(other.getUpperCorner().getX(), other.getUpperCorner().getY());
	}
	
	@Override
	public boolean is3D() {
		return isValid() && lowerCorner.is3D() && upperCorner.is3D();
	}
	
	@Override
	public boolean isValid() {
		return lowerCorner != null && lowerCorner.isValid()
				&& upperCorner != null && upperCorner.isValid();
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.ENVELOPE;
	}
	
}
