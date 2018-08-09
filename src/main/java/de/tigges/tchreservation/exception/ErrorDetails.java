package de.tigges.tchreservation.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
		this.setDetails(details);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ErrorDetails: ");
		sb.append(message);
		if (getDetails() != null) {
			sb.append(" details: ");
			sb.append(getDetails());
		}
		if (fieldErrors != null && !fieldErrors.isEmpty()) {
			sb.append(" [");
			sb.append(String.join(",", fieldErrors.stream().map(f -> f.toString()).collect(Collectors.toList())));
			sb.append(" ]");
		}
		return sb.toString();
	}

}
