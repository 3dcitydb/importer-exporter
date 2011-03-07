package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportXLinkFeatureConfigType", propOrder={
		"keepGmlIdAsExternalReference"
		})
public class ExpXLinkFeatureConfig extends ExpXLinkConfig {
	@XmlElement(defaultValue="false")
	private Boolean keepGmlIdAsExternalReference = false;
	
	public ExpXLinkFeatureConfig() {
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
