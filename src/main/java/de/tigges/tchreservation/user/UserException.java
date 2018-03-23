package de.tigges.tchreservation.user;

import de.tigges.tchreservation.reservation.ReservationException;

public class UserException extends ReservationException {

	private static final long serialVersionUID = -2176960289647126914L;

	public UserException(String message) {
		super(message);
	}

	public UserException(String msg, Object... args) {
		super(msg, args);
	}

}
