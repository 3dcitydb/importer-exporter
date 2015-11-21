package org.citydb.api.database;

@SuppressWarnings("serial")
public class DatabaseConnectionWarning extends Exception {
	private final Enum<?> type;
	private final String formattedMessage;
	
	public enum ConnectionWarningType {
		OUTDATED_DATABASE_VERSION
	}
	
	public DatabaseConnectionWarning(String message, String formattedMessage, Enum<?> type, Throwable cause) {
		super(message, cause);
		this.formattedMessage = formattedMessage;
		this.type = type;
	}
	
	public DatabaseConnectionWarning(String message, String formattedMessage, Enum<?> type) {
		this(message, formattedMessage, type, null);
	}
	
	public String getFormattedMessage() {
		return formattedMessage;
	}
	
	public Enum<?> getType() {
		return type;
	}
	
}