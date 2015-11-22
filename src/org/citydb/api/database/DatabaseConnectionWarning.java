package org.citydb.api.database;

@SuppressWarnings("serial")
public class DatabaseConnectionWarning extends Exception {
	private final String formattedMessage;
	private final String productName;
	private final Enum<?> type;
	
	public enum ConnectionWarningType {
		OUTDATED_DATABASE_VERSION
	}
	
	public DatabaseConnectionWarning(String message, String formattedMessage, String productName, Enum<?> type, Throwable cause) {
		super(message, cause);
		this.formattedMessage = formattedMessage;
		this.productName = productName;
		this.type = type;
	}
	
	public DatabaseConnectionWarning(String message, String formattedMessage, String productName, Enum<?> type) {
		this(message, formattedMessage, productName, type, null);
	}
	
	public String getFormattedMessage() {
		return formattedMessage;
	}
	
	public String getProductName() {
		return productName;
	}

	public Enum<?> getType() {
		return type;
	}
	
}