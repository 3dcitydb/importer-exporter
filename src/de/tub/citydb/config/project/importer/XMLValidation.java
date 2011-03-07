package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="XMLValidationType", propOrder={
		"useXMLValidation",
		"useLocalSchemas",
		"reportOneErrorPerFeature"
})
public class XMLValidation {
	@XmlElement(required=true, defaultValue="false")
	private Boolean useXMLValidation = false;
	@XmlElement(required=true)
	private LocalXMLSchemas useLocalSchemas;
	@XmlElement(defaultValue="false")
	private Boolean reportOneErrorPerFeature = false;

	public XMLValidation() {
		useLocalSchemas = new LocalXMLSchemas();
	}

	public boolean isSetUseXMLValidation() {
		if (useXMLValidation != null)
			return useXMLValidation.booleanValue();

		return false;
	}

	public Boolean getUseXMLValidation() {
		return useXMLValidation;
	}

	public void setUseXMLValidation(Boolean useXMLValidation) {
		this.useXMLValidation = useXMLValidation;
	}

	public LocalXMLSchemas getUseLocalSchemas() {
		return useLocalSchemas;
	}

	public void setUseLocalSchemas(LocalXMLSchemas useLocalSchemas) {
		if (useLocalSchemas != null)
			this.useLocalSchemas = useLocalSchemas;
	}

	public boolean isSetReportOneErrorPerFeature() {
		if (reportOneErrorPerFeature != null)
			return reportOneErrorPerFeature.booleanValue();
		
		return false;
	}
	
	public Boolean getReportOneErrorPerFeature() {
		return reportOneErrorPerFeature;
	}

	public void setReportOneErrorPerFeature(Boolean reportOneErrorPerFeature) {
		this.reportOneErrorPerFeature = reportOneErrorPerFeature;
	}
	
}
