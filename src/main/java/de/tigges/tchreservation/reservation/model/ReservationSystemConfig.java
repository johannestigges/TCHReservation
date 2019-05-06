package de.tigges.tchreservation.reservation.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReservationSystemConfig {

	private long id;

	private String name;
	private int courts;
	private int durationUnitInMinutes;
	private int openingHour;
	private int closingHour;
}
