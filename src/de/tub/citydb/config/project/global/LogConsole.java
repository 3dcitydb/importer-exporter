package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.log.LogLevelType;

@XmlType(name="LogConsoleType", propOrder={
		"wrapText",
		"logLevel"
		})
public class LogConsole {
	private boolean wrapText = false;
	private LogLevelType logLevel = LogLevelType.INFO;
	
	public LogConsole() {
	}

	public LogLevelType getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevelType logLevel) {
		this.logLevel = logLevel;
	}

	public void setWrapText(boolean wrapText) {
		this.wrapText = wrapText;
	}

	public boolean isWrapText() {
		return wrapText;
	}
	
}
