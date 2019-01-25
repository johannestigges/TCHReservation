package de.tigges.tchreservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.tigges.tchreservation.EntityType;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RestException {
	private static final long serialVersionUID = 1L;

	public NotFoundException(EntityType entityType, long id) {
		super(HttpStatus.NOT_FOUND, String.format("%s with id %d not found", entityType.name(), id));
	}
}
