package de.tigges.tchreservation.util.exception;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.util.message.MessageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(MessageUtil messageUtil, EntityType entityType, Long id) {
        super(HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND,
                messageUtil.msg(ErrorCode.NOT_FOUND, entityType,id));
    }
}
