package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="XLinkFeatureConfigType", propOrder={
		"keepGmlIdAsExternalReference"
		})
public class XLinkFeatureConfig extends XLinkConfig {
	@XmlElement(defaultValue="false")
	private Boolean keepGmlIdAsExternalReference = false;
	
	public XLinkFeatureConfig() {
	}

	public boolean isSetKeepGmlIdAsExternalReference() {
		if (keepGmlIdAsExternalReference != null)
			return keepGmlIdAsExternalReference.booleanValue();
		
		return false;
	}
	
	public Boolean getKeepGmlIdAsExternalReference() {
		return keepGmlIdAsExternalReference;
	}

	public void setKeepGmlIdAsExternalReference(Boolean keepGmlIdAsExternalReference) {
		this.keepGmlIdAsExternalReference = keepGmlIdAsExternalReference;
	}

}
