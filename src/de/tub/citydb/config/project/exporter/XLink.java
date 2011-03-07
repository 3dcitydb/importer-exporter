package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="XLinkType", propOrder={
		"feature",
		"geometry"
})
public class XLink {
	@XmlElement(required=true)
	private XLinkFeatureConfig feature;
	@XmlElement(required=true)
	private XLinkConfig geometry;

	public XLink() {
		feature = new XLinkFeatureConfig();
		geometry = new XLinkConfig();
	}

	public XLinkFeatureConfig getFeature() {
		return feature;
	}

	public void setFeature(XLinkFeatureConfig feature) {
		if (feature != null)
			this.feature = feature;
	}

	public XLinkConfig getGeometry() {
		return geometry;
	}

	public void setGeometry(XLinkConfig geometry) {
		if (geometry != null)
			this.geometry = geometry;
	}

}
