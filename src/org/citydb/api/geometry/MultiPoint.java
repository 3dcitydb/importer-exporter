package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="multiPoint")
@XmlType(name="MultiPointType", propOrder={
		"points"
})
public class MultiPoint extends AbstractGeometry {
	@XmlElement(name="point", required=true)
	private List<Point> points;
	
	public MultiPoint() {
		points = new ArrayList<>();
	}
	
	public List<Point> getPoints() {
		return points;
	}
	
	public void setPoints(List<Point> points) {
		this.points = points;
	}

	@Override
	public boolean is3D() {
		if (isValid()) {
			for (Point point : points) {
				if (!point.is3D())
					return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isValid() {
		if (points != null && !points.isEmpty()) {
			for (Point point : points) {
				if (!point.isValid())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.MULTI_POINT;
	}

}
