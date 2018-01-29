package org.citydb.database.schema.path;

public class InvalidSchemaPathException extends Exception {
	private static final long serialVersionUID = -3716015045363231263L;
	
	public InvalidSchemaPathException() {
		super();
	}
	
	public InvalidSchemaPathException(String message) {
		super(message);
	}
	
	public InvalidSchemaPathException(Throwable cause) {
		super(cause);
	}
	
	public InvalidSchemaPathException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
