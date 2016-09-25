package org.citydb.api.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="multiLineString")
@XmlType(name="MultiLineStringType", propOrder={
		"lineStrings"
})
public class MultiLineString extends AbstractGeometry {
	@XmlElement(name="lineString", required=true)
	private List<LineString> lineStrings;
	
	public MultiLineString() {
		lineStrings = new ArrayList<>();
	}
	
	public List<LineString> getLineStrings() {
		return lineStrings;
	}

	public void setLineStrings(List<LineString> lineStrings) {
		this.lineStrings = lineStrings;
	}

	@Override
	public boolean is3D() {
		if (isValid()) {
			for (LineString lineString : lineStrings) {
				if (!lineString.is3D())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		if (lineStrings != null && !lineStrings.isEmpty()) {
			for (LineString lineString : lineStrings) {
				if (!lineString.isValid())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.MULTI_LINE_STRING;
	}

}
