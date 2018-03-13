package de.tigges.tchreservation.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.tigges.tchreservation.user.model.ActivationStatus;

@ResponseStatus(code=HttpStatus.INTERNAL_SERVER_ERROR)
public class ActivationStatusException extends RuntimeException {

	private static final long serialVersionUID = -6891309491588779751L;

	public ActivationStatusException(ActivationStatus from, ActivationStatus to, String id) {
		super("cannot change activation status from '" + from + "' to '" + to + "' for '" + id + "'.");
	}
}
