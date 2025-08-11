package de.tigges.tchreservation.util.exception;

import de.tigges.tchreservation.util.message.MessageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthorizationException extends RestException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthorizationException(ErrorCode code,String message) {
        super(HttpStatus.UNAUTHORIZED, code, message);
    }

    public AuthorizationException(MessageUtil messageUtil, ErrorCode errorCode, Object...args) {
        this(errorCode, messageUtil.msg(errorCode, args));
    }

}
