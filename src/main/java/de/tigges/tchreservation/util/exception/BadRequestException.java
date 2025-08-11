package de.tigges.tchreservation.util.exception;

import de.tigges.tchreservation.util.message.MessageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;
import java.util.Collection;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RestException {
	@Serial
	private static final long serialVersionUID = 1L;

	public BadRequestException(Collection<ErrorMessage> errorMessages) {
		super(HttpStatus.BAD_REQUEST, errorMessages);
	}

	public BadRequestException(ErrorCode code, String message) {
		super(HttpStatus.BAD_REQUEST, code, message);
	}

	public BadRequestException(MessageUtil messageUtil, ErrorCode errorCode, Object...args) {
		this(errorCode, messageUtil.msg(errorCode, args));
	}
}
