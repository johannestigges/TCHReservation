package de.tigges.tchreservation.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RestException.class)
	public final ResponseEntity<ErrorDetails> handleRestException(RestException e, WebRequest request) {
		return new ResponseEntity<>(new ErrorDetails(e.getMessage(), request.getDescription(false)), e.getStatus());
	}
}
