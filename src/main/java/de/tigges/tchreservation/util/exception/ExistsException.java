package de.tigges.tchreservation.util.exception;

import de.tigges.tchreservation.protocol.EntityType;
import de.tigges.tchreservation.util.message.MessageUtil;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ExistsException extends RestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExistsException(ErrorCode code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }

    public ExistsException(MessageUtil messageUtil, EntityType entityType, Long id) {
        this(ErrorCode.EXISTS, messageUtil.msg(ErrorCode.EXISTS, entityType,id));
    }
}
