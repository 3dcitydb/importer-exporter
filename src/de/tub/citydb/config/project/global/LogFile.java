package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.log.LogLevelType;

@XmlType(name="LogFileType", propOrder={
		"logLevel",
		"useAlternativeLogPath",
		"alternativeLogPath"
		})
public class LogFile {
	@XmlAttribute(required=false)
	private Boolean active = false;
	private LogLevelType logLevel = LogLevelType.INFO;
	private Boolean useAlternativeLogPath = false;
	private String alternativeLogPath = "";
	
	public LogFile() {
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

	public LogLevelType getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevelType logLevel) {
		if (logLevel != null)
			this.logLevel = logLevel;
	}

	public Boolean getUseAlternativeLogPath() {
		return useAlternativeLogPath;
	}
	
	public boolean isSetUseAlternativeLogPath() {
		if (useAlternativeLogPath != null)
			return useAlternativeLogPath.booleanValue();
		
		return false;
	}

	public void setUseAlternativeLogPath(Boolean useAlternativeLogPath) {
		if (useAlternativeLogPath != null)
			this.useAlternativeLogPath = useAlternativeLogPath;
	}

	public String getAlternativeLogPath() {
		return alternativeLogPath;
	}

	public void setAlternativeLogPath(String alternativeLogPath) {
		if (alternativeLogPath != null)
			this.alternativeLogPath = alternativeLogPath;
	}

}
