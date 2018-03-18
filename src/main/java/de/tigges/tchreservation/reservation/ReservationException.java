package de.tigges.tchreservation.reservation;

public class ReservationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ReservationException (String message) {
		super(message);
	}
	
	public ReservationException (String msg, Object...args) {
		this(String.format(msg, args));
	}
}
