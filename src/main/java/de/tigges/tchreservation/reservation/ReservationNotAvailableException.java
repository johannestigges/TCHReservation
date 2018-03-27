package de.tigges.tchreservation.reservation;

public class ReservationNotAvailableException extends ReservationException {
	private static final long serialVersionUID = -2557150563579285919L;

	public ReservationNotAvailableException(String message) {
		super(message);
	}

	public ReservationNotAvailableException(String msg, Object... args) {
		super(msg, args);
	}

}
