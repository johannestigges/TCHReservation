package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RestException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final HttpStatus status;
	private final ErrorDetails errorDetails;

	public RestException(HttpStatus status, String message) {
		this(status, new ErrorDetails(message, null));
	}
}
