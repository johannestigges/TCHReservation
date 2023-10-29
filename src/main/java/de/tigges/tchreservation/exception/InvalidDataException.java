package de.tigges.tchreservation.exception;

import java.io.Serial;
import java.util.Collection;

public class InvalidDataException extends BadRequestException {
	@Serial
	private static final long serialVersionUID = 1L;

	public InvalidDataException(Collection<ErrorMessage> errorMessages) {
		super(errorMessages);
	}
}
