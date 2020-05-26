package de.tigges.tchreservation.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSystemConfig {

	private long id;

	private String name;

	private int courts;

	private int durationUnitInMinutes;

	private int openingHour;

	private int closingHour;
}
