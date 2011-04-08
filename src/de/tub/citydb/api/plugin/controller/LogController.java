package de.tub.citydb.api.plugin.controller;

public interface LogController {
	public void debug(String message);
	public void info(String message);
	public void warn(String message);
	public void error(String message);
}
