package de.tigges.tchreservation.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4705790634629365378L;

	public UserNotFoundException(long userId) {
		super ("user '" + userId + "' not found");
	}
}
