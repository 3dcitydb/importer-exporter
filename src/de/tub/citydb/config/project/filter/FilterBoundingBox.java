package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="BoundingBoxType", propOrder={
		"mode",
		"lowerLeftCorner",
		"upperRightCorner"
})
public class FilterBoundingBox {
	@XmlElement(required=true)
	private FilterBoundingBoxMode mode = FilterBoundingBoxMode.OVERLAP;
	private FilterBoundingBoxPoint lowerLeftCorner;
	private FilterBoundingBoxPoint upperRightCorner;
	@XmlAttribute(required=true)
	private Boolean active = false;

	public FilterBoundingBox() {
		lowerLeftCorner = new FilterBoundingBoxPoint();
		upperRightCorner = new FilterBoundingBoxPoint();
	}

	public boolean isSetContainMode() {
		return mode == FilterBoundingBoxMode.CONTAIN;
	}

	public boolean isSetOverlapMode() {
		return mode == FilterBoundingBoxMode.OVERLAP;
	}

	public FilterBoundingBoxMode getMode() {
		return mode;
	}

	public void setMode(FilterBoundingBoxMode mode) {
		this.mode = mode;
	}

	public FilterBoundingBoxPoint getLowerLeftCorner() {
		return lowerLeftCorner;
	}

	public void setLowerLeftCorner(FilterBoundingBoxPoint lowerLeftCorner) {
		if (lowerLeftCorner != null)
			this.lowerLeftCorner = lowerLeftCorner;
	}

	public FilterBoundingBoxPoint getUpperRightCorner() {
		return upperRightCorner;
	}

	public void setUpperRightCorner(FilterBoundingBoxPoint upperRightCorner) {
		if (upperRightCorner != null)
			this.upperRightCorner = upperRightCorner;
	}

	public boolean isSet() {
		return active == true;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
