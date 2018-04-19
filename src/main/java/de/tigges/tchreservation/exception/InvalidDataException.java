package de.tigges.tchreservation.exception;

public class InvalidDataException extends BadRequestException {
	private static final long serialVersionUID = 1L;

	public InvalidDataException(ErrorDetails errorDetail) {
		super(errorDetail);
	}
}
