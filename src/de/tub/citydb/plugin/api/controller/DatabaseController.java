package de.tub.citydb.plugin.api.controller;

public interface DatabaseController {
	public boolean connect();
	public boolean disconnect();
	public boolean isConnected();
}
