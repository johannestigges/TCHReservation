package de.tigges.tchreservation.reservation.model;

public class ReservationSystemConfig {

	public ReservationSystemConfig() {
	}

	public ReservationSystemConfig(long id, String name, int courts, int durationUnitInMinutes, int openingHour,
			int closingHour) {
		this.id = id;
		this.name = name;
		this.courts = courts;
		this.durationUnitInMinutes = durationUnitInMinutes;
		this.openingHour = openingHour;
		this.closingHour = closingHour;
	}

	private long id;

	private String name;
	private int courts;
	private int durationUnitInMinutes;
	private int openingHour;
	private int closingHour;

	public int getCourts() {
		return courts;
	}

	public int getDurationUnitInMinutes() {
		return durationUnitInMinutes;
	}

	public int getOpeningHour() {
		return openingHour;
	}

	public int getClosingHour() {
		return closingHour;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
