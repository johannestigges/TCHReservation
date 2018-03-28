package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

public class RestException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private HttpStatus status;

	public RestException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
