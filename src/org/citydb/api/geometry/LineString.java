package org.citydb.api.geometry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="lineString")
@XmlType(name="LineStringType", propOrder={
		"posList"
})
public class LineString extends AbstractGeometry {
	@XmlElement(required=true)
    private PositionList posList;
		
	public LineString() {
		posList = new PositionList();
	}

	public PositionList getPosList() {
		return posList;
	}

	public void setPosList(PositionList posList) {
		this.posList = posList;
	}

	@Override
	public boolean is3D() {
		return isValid() && posList.getDimension() == 3;
	}
	
	@Override
	public boolean isValid() {
		return posList != null && posList.isValid();
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.LINE_STRING;
	}

}
