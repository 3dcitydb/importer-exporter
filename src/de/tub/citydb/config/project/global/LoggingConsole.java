package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.log.LogLevelType;

@XmlType(name="LoggingConsoleType", propOrder={
		"logLevel"
		})
public class LoggingConsole {
	private LogLevelType logLevel = LogLevelType.INFO;
	
	public LoggingConsole() {
	}

	public LogLevelType getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
	}
	
}
