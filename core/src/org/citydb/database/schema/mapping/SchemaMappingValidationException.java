package org.citydb.database.schema.mapping;

public class SchemaMappingValidationException extends Exception {
	private static final long serialVersionUID = -3716015045363231263L;
	
	public SchemaMappingValidationException() {
		super();
	}
	
	public SchemaMappingValidationException(String message) {
		super(message);
	}
	
	public SchemaMappingValidationException(Throwable cause) {
		super(cause);
	}
	
	public SchemaMappingValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
