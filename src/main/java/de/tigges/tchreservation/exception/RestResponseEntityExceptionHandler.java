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
		ErrorDetails errorDetails = e.getErrorDetails();
		errorDetails.setDetails(request.getDescription(false));
		return new ResponseEntity<>(errorDetails, e.getStatus());
	}
}
