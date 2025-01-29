package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class FoundException extends RestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FoundException(ErrorCode code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
