package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RestException {
	private static final long serialVersionUID = 1L;

	public BadRequestException(ErrorDetails errorDetails) {
		super(HttpStatus.BAD_REQUEST, errorDetails);
	}

	public BadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}
