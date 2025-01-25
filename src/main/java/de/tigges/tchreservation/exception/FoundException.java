package de.tigges.tchreservation.exception;

import de.tigges.tchreservation.protocol.EntityType;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class FoundException extends RestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FoundException(EntityType entityType, long id) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.EXISTS,
                String.format("%s with id %d exists already", entityType.name(), id));
    }
}
