package de.tigges.tchreservation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class RestException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public static final Logger logger = LoggerFactory.getLogger(RestException.class);
	
	private HttpStatus status;
	private ErrorDetails errorDetails;

	public RestException(HttpStatus status, String message) {
		this(status, new ErrorDetails(message,null));
	}

	public RestException(HttpStatus status, ErrorDetails errorDetails) {
		super();
		this.status = status;
		this.errorDetails = errorDetails;
		logger.error("{}: Status: {} {}", getClass().getSimpleName(), status, errorDetails);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public ErrorDetails getErrorDetails() {
		return errorDetails;
	}
}
