package de.tigges.tchreservation.reservation.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "reservation_system")
public class ReservationSystemConfigEntity {

	ReservationSystemConfigEntity() {
	}

	public ReservationSystemConfigEntity(long id, String name, int courts, int durationUnitInminutes, int openingHour,
			int closingHour) {
		this.id = id;
		this.name = name;
		this.courts = courts;
		this.durationUnitInMinutes = durationUnitInminutes;
		this.openingHour = openingHour;
		this.closingHour = closingHour;
	}

	@Id
	long id;
	String name;
	int courts;
	int durationUnitInMinutes;
	int openingHour;
	int closingHour;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

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
}