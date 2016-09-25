package org.citydb.api.geometry;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="polygon")
@XmlType(name="PolygonType", propOrder={
		"exterior",
		"interior"
})
public class Polygon extends AbstractGeometry {
	@XmlElement(required=true)
    private PositionList exterior;
	@XmlElement(required=false)
	private List<PositionList> interior;
	
	public Polygon() {
		exterior = new PositionList();
	}
	
	public PositionList getExterior() {
		return exterior;
	}

	public void setExterior(PositionList exterior) {
		this.exterior = exterior;
	}

	public List<PositionList> getInterior() {
		return interior;
	}
	
	public boolean isSetInterior() {
		return interior != null;
	}

	public void setInterior(List<PositionList> interior) {
		this.interior = interior;
	}

	@Override
	public boolean is3D() {
		if (isValid()) {
			if (exterior.getDimension() != 3)
				return false;
			
			if (interior != null) {
				for (PositionList tmp : interior) {
					if (tmp.getDimension() != 3)
						return false;
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		if (exterior != null && exterior.isValid()) {
			if (interior != null) {
				for (PositionList tmp : interior) {
					if (!tmp.isValid())
						return false;
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.POLYGON;
	}

}
