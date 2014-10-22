package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportLogType", propOrder={
		"logImportedFeatures",
		"logPath"
})
public class ImportLog {
	@XmlElement(required=true, defaultValue="false")
	private Boolean logImportedFeatures = false;
	private String logPath;
	
	public boolean isSetLogImportedFeatures() {
		if (logImportedFeatures != null)
			return logImportedFeatures.booleanValue();

		return false;
	}

	public Boolean getLogImportedFeatures() {
		return logImportedFeatures;
	}

	public void setLogImportedFeatures(Boolean logImportedFeatures) {
		this.logImportedFeatures = logImportedFeatures;
	}
	
	public boolean isSetLogPath() {
		return logPath != null;
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String logPath) {
		if (logPath != null && !logPath.isEmpty())
			this.logPath = logPath;
	}
	
}
