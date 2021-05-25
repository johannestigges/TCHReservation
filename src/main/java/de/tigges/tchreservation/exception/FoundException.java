package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;

import de.tigges.tchreservation.protocol.EntityType;

public class FoundException extends RestException {
	private static final long serialVersionUID = 1L;

	public FoundException(EntityType entityType, long id) {
		super(HttpStatus.BAD_REQUEST, String.format("%s with id %d exists already", entityType.name(), id));
	}
}
