package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthorizationException extends RestException {

	private static final long serialVersionUID = 1L;

	public AuthorizationException(String message) {
		super(HttpStatus.UNAUTHORIZED, message);
	}

}
