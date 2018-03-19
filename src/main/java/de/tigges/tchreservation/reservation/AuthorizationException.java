package de.tigges.tchreservation.reservation;

public class AuthorizationException extends ReservationException {

	private static final long serialVersionUID = -4610961896397475282L;

	public AuthorizationException(String msg, Object... args) {
		super(msg, args);
	}

	public AuthorizationException(String message) {
		super(message);
	}
}
