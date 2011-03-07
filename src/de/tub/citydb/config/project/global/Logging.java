package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="LoggingType", propOrder={
		"console",
		"file"
		})
public class Logging {
	private LogConsole console;
	private LogFile file;
	
	public Logging() {
		console = new LogConsole();
		file = new LogFile();
	}

	public LogConsole getConsole() {
		return console;
	}

	public void setConsole(LogConsole console) {
		if (console != null)
			this.console = console;
	}

	public LogFile getFile() {
		return file;
	}

	public void setFile(LogFile file) {
		if (file != null)
			this.file = file;
	}
	
}
