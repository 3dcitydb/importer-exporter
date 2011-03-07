package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GmlIdLookupServerType", propOrder={
		"feature",
		"geometry"		
})
public class GmlIdLookupServer {
	@XmlElement(required=true)
	private GmlIdLookupServerConfig feature;
	@XmlElement(required=true)
	private GmlIdLookupServerConfig geometry;

	public GmlIdLookupServer() {
		feature = new GmlIdLookupServerConfig();
		geometry = new GmlIdLookupServerConfig();
	}

	public GmlIdLookupServerConfig getFeature() {
		return feature;
	}

	public void setFeature(GmlIdLookupServerConfig feature) {
		if (feature != null)
			this.feature = feature;
	}

	public GmlIdLookupServerConfig getGeometry() {
		return geometry;
	}

	public void setGeometry(GmlIdLookupServerConfig geometry) {
		if (geometry != null)
			this.geometry = geometry;
	}

}
