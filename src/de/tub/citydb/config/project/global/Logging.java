package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="LoggingType", propOrder={
		"console",
		"file"
		})
public class Logging {
	private LoggingConsole console;
	private LoggingFile file;
	
	public Logging() {
		console = new LoggingConsole();
		file = new LoggingFile();
	}

	public LoggingConsole getConsole() {
		return console;
	}

	public void setConsole(LoggingConsole console) {
		if (console != null)
			this.console = console;
	}

	public LoggingFile getFile() {
		return file;
	}

	public void setFile(LoggingFile file) {
		if (file != null)
			this.file = file;
	}
	
}
