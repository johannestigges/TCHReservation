package de.tigges.tchreservation.exception;

public class ErrorDetails {
	private String message;
	private String details;

	public ErrorDetails() {
	}

	public ErrorDetails(String message, String details) {
		this.message = message;
		this.details = details;
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}
}
