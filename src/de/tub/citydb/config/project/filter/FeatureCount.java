package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FeatureCountType", propOrder={
		"from",
		"to"
})
public class FeatureCount {
	@XmlSchemaType(name="nonNegativeInteger")
	private Long from;
	@XmlSchemaType(name="nonNegativeInteger")
	private Long to;
	@XmlAttribute(required=true)
	private Boolean active = false;

	public FeatureCount() {
	}

	public Long getFrom() {
		return from;
	}

	public void setFrom(Long from) {
		this.from = from;
	}

	public Long getTo() {
		return to;
	}

	public void setTo(Long to) {
		this.to = to;
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

}
