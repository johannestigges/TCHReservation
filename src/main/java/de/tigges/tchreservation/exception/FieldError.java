package de.tigges.tchreservation.exception;

/**
 * data describing an error concerning a field
 * 
 * @author johannes
 */
public class FieldError {
	private String entity;
	private String field;
	private String message;

	public FieldError() {
	}

	public FieldError(String entity, String field, String message) {
		this.entity = entity;
		this.field = field;
		this.message = message;
		System.out.println(String.format("create FieldError(%s,%s,%s)",entity, field, message));
	}

	public String getEntity() {
		return entity;
	}

	public String getField() {
		return field;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return entity + "." + field + ": " + message;
	}
}
