package de.tigges.tchreservation.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.tigges.tchreservation.user.model.ActivationStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class ActivationStatusException extends RuntimeException {

	private static final long serialVersionUID = -6891309491588779751L;

	public ActivationStatusException(ActivationStatus from, ActivationStatus to, String id) {
		super(String.format("cannot change activation status from '%s' to '%s' for '%s'.", from, to, id));
	}
}
