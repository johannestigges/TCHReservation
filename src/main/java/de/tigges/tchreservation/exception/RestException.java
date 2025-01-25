package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class RestException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final Collection<ErrorMessage> errorMessages;

    public RestException(HttpStatus status, ErrorCode code, String message) {
        this(status, new ErrorMessage(code, message, null));
    }

    public RestException(HttpStatus status, ErrorMessage errorDetail) {
        this(status, List.of(errorDetail));
    }
}
