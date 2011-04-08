package de.tub.citydb.api.plugin.exception;

@SuppressWarnings("serial")
public class DatabaseConfigurationException extends Exception {

	public DatabaseConfigurationException(String reason) {
		super(reason);
	}
	
	public DatabaseConfigurationException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
