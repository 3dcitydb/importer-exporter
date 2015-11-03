package org.citydb.api.database;

@SuppressWarnings("serial")
public class DatabaseConnectionWarning extends Exception {
	private final Enum<?> type;
	
	public enum ConnectionWarningType {
		OUTDATED_DATABASE_VERSION
	}
	
	public DatabaseConnectionWarning(String reason, Enum<?> type) {
		super(reason);
		this.type = type;
	}
	
	public DatabaseConnectionWarning(String reason, Enum<?> type, Throwable cause) {
		super(reason, cause);
		this.type = type;
	}

	public Enum<?> getType() {
		return type;
	}
	
}