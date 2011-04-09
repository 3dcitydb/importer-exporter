package de.tub.citydb.api.database;

@SuppressWarnings("serial")
public class DatabaseConfigurationException extends Exception {

	public DatabaseConfigurationException(String reason) {
		super(reason);
	}
	
	public DatabaseConfigurationException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
