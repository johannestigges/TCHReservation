package de.tigges.tchreservation.exception;

import de.tigges.tchreservation.protocol.EntityType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(EntityType entityType, long id) {
        super(HttpStatus.NOT_FOUND, "%s with id %d not found".formatted(entityType.name(), id));
    }
}
