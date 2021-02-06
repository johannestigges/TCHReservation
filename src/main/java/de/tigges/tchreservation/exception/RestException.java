package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class RestException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private HttpStatus status;
	private ErrorDetails errorDetails;

	public RestException(HttpStatus status, String message) {
		this(status, new ErrorDetails(message, null));
	}

	public RestException(HttpStatus status, ErrorDetails errorDetails) {
		super();
		this.status = status;
		this.errorDetails = errorDetails;
		log.error("{}: Status: {} {}", getClass().getSimpleName(), status, errorDetails);
	}
}
