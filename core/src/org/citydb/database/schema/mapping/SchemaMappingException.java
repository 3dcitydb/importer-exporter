package org.citydb.database.schema.mapping;

public class SchemaMappingException extends Exception {
	private static final long serialVersionUID = -3716015045363231263L;
	
	public SchemaMappingException() {
		super();
	}
	
	public SchemaMappingException(String message) {
		super(message);
	}
	
	public SchemaMappingException(Throwable cause) {
		super(cause);
	}
	
	public SchemaMappingException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
