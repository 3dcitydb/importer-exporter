package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="multiPolygon")
@XmlType(name="MultiPolygonType", propOrder={
		"polygons"
})
public class MultiPolygon extends AbstractGeometry {
	@XmlElement(name="polygon", required=true)
	private List<Polygon> polygons;
	
	public MultiPolygon() {
		polygons = new ArrayList<>();
	}

	public List<Polygon> getPolygons() {
		return polygons;
	}

	public void setPolygons(List<Polygon> polygons) {
		this.polygons = polygons;
	}

	@Override
	public boolean is3D() {
		if (isValid()) {
			for (Polygon polygon : polygons) {
				if (!polygon.is3D())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		if (polygons != null && !polygons.isEmpty()) {
			for (Polygon polygon : polygons) {
				if (!polygon.isValid())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.MULTI_POLYGON;
	}
	
	
}
