package de.tub.citydb.api.controller;

import de.tub.citydb.api.log.LogLevel;

public interface LogController {
	public void debug(String message);
	public void info(String message);
	public void warn(String message);
	public void error(String message);
	public void all(LogLevel level, String message);
	public void log(LogLevel level, String msg);
	public void print(String msg);
	
	public LogLevel getDefaultConsoleLogLevel();
	public LogLevel getDefaultFileLogLevel();
}
