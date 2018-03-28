package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

public class RestException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private HttpStatus status;
	private ErrorDetails errorDetails;

	public RestException(HttpStatus status, String message) {
		super();
		this.errorDetails = new ErrorDetails(message, null);
		this.status = status;
	}

	public RestException(HttpStatus status, ErrorDetails errorDetails) {
		super();
		this.status = status;
		this.errorDetails = errorDetails;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public ErrorDetails getErrorDetails() {
		return errorDetails;
	}
}
