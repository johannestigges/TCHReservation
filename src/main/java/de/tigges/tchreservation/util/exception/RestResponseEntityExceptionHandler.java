package de.tigges.tchreservation.util.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collection;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RestException.class)
    public final ResponseEntity<Collection<ErrorMessage>> handleRestException(RestException e, WebRequest request) {
        return new ResponseEntity<>(e.getErrorMessages(), e.getStatus());
    }
}
