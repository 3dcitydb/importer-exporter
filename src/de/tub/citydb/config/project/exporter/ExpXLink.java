package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportXLinkType", propOrder={
		"feature",
		"geometry"
})
public class ExpXLink {
	@XmlElement(required=true)
	private ExpXLinkFeatureConfig feature;
	@XmlElement(required=true)
	private ExpXLinkConfig geometry;

	public ExpXLink() {
		feature = new ExpXLinkFeatureConfig();
		geometry = new ExpXLinkConfig();
	}

	public ExpXLinkFeatureConfig getFeature() {
		return feature;
	}

	public void setFeature(ExpXLinkFeatureConfig feature) {
		if (feature != null)
			this.feature = feature;
	}

	public ExpXLinkConfig getGeometry() {
		return geometry;
	}

	public void setGeometry(ExpXLinkConfig geometry) {
		if (geometry != null)
			this.geometry = geometry;
	}

}
