package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GmlIdLookupServerType", propOrder={
		"feature",
		"geometry"		
})
public class SysGmlIdLookupServer {
	@XmlElement(required=true)
	private SysGmlIdLookupServerConfig feature;
	@XmlElement(required=true)
	private SysGmlIdLookupServerConfig geometry;

	public SysGmlIdLookupServer() {
		feature = new SysGmlIdLookupServerConfig();
		geometry = new SysGmlIdLookupServerConfig();
	}

	public SysGmlIdLookupServerConfig getFeature() {
		return feature;
	}

	public void setFeature(SysGmlIdLookupServerConfig feature) {
		if (feature != null)
			this.feature = feature;
	}

	public SysGmlIdLookupServerConfig getGeometry() {
		return geometry;
	}

	public void setGeometry(SysGmlIdLookupServerConfig geometry) {
		if (geometry != null)
			this.geometry = geometry;
	}

}
