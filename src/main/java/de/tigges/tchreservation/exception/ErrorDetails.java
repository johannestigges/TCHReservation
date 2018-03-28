package de.tigges.tchreservation.exception;

import java.util.ArrayList;
import java.util.List;

public class ErrorDetails {
	private String message;
	private String details;
	private List<FieldError> fieldErrors;

	public ErrorDetails() {
		fieldErrors = new ArrayList<>();
	}

	public ErrorDetails(String message, String details) {
		this();
		this.message = message;
		this.details = details;
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}
	
}
