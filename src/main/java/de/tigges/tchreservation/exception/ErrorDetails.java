package de.tigges.tchreservation.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ErrorDetails: ");
		sb.append(message);
		if (getDetails() != null) {
			sb.append(" details: ").append(getDetails());
		}
		if (fieldErrors != null && !fieldErrors.isEmpty()) {
			sb.append(" [").append(logFieldErrors()).append(" ]");
		}
		return sb.toString();
	}

	private String logFieldErrors() {
		return fieldErrors.stream().map(f -> f.toString()).collect(Collectors.joining(","));
	}
}
