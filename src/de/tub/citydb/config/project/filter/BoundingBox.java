package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.ReferenceSystem;

@XmlType(name="BoundingBoxType", propOrder={
		"mode",
		"srs",
		"lowerLeftCorner",
		"upperRightCorner"		
})
public class BoundingBox {
	@XmlElement(required=true)
	private BoundingBoxMode mode = BoundingBoxMode.OVERLAP;
	@XmlIDREF
	private ReferenceSystem srs = Internal.DEFAULT_DB_REF_SYS;
	private BoundingBoxPoint lowerLeftCorner;
	private BoundingBoxPoint upperRightCorner;
	@XmlAttribute(required=true)
	private Boolean active = false;

	public BoundingBox() {
		lowerLeftCorner = new BoundingBoxPoint();
		upperRightCorner = new BoundingBoxPoint();
	}

	public boolean isSetContainMode() {
		return mode == BoundingBoxMode.CONTAIN;
	}

	public boolean isSetOverlapMode() {
		return mode == BoundingBoxMode.OVERLAP;
	}

	public BoundingBoxMode getMode() {
		return mode;
	}

	public void setMode(BoundingBoxMode mode) {
		this.mode = mode;
	}

	public BoundingBoxPoint getLowerLeftCorner() {
		return lowerLeftCorner;
	}

	public void setLowerLeftCorner(BoundingBoxPoint lowerLeftCorner) {
		if (lowerLeftCorner != null)
			this.lowerLeftCorner = lowerLeftCorner;
	}

	public BoundingBoxPoint getUpperRightCorner() {
		return upperRightCorner;
	}

	public void setUpperRightCorner(BoundingBoxPoint upperRightCorner) {
		if (upperRightCorner != null)
			this.upperRightCorner = upperRightCorner;
	}

	public boolean isSet() {
		if (active != null)
			return active.booleanValue();

		return false;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setSRS(ReferenceSystem srs) {
		if (srs != null)
			this.srs = srs;
	}

	public ReferenceSystem getSRS() {
		return srs;
	}

}
