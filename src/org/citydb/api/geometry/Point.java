package org.citydb.api.geometry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;

@XmlRootElement(name="point")
@XmlType(name = "PointType", propOrder={
		"pos"
})
public class Point extends AbstractGeometry {
	@XmlElement(name="pos", required=true)
	private Position pos;
	
	public Point() {
		pos = new Position();
	}

	public Point(Position pos, DatabaseSrs srs) {
		this.pos = pos;
		setSrs(srs);
	}
	
	public Point(Double x, Double y, DatabaseSrs srs) {
		this(new Position(x, y), srs);
	}
	
	public Point(Double x, Double y, Double z, DatabaseSrs srs) {
		this(new Position(x, y, z), srs);
	}
	
	public Point(Position pos) {
		this(pos, null);
	}
	
	public Point(Double x, Double y, Double z) {
		this(new Position(x, y, z), null);
	}
	
	public Point(Double x, Double y) {
		this(new Position(x, y), null);
	}

	public Position getPos() {
		return pos;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}
	
	public Double getX() {
		return pos.getX();
	}
	
	public boolean isSetX() {
		return pos.isSetX();
	}

	public void setX(Double x) {
		pos.setX(x);
	}

	public Double getY() {
		return pos.getY();
	}
	
	public boolean isSetY() {
		return pos.isSetY();
	}

	public void setY(Double y) {
		pos.setY(y);
	}
	
	public Double getZ() {
		return pos.getZ();
	}
	
	public boolean isSetZ() {
		return pos.isSetZ();
	}

	public void setZ(Double z) {
		pos.setZ(z);
	}
	
	@Override
	public boolean is3D() {
		return isValid() && pos.is3D();
	}

	@Override
	public boolean isValid() {
		return pos.isValid();
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.POINT;
	}	
	
}
