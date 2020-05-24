package de.tigges.tchreservation.reservation.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSystemConfig {

	private long id;

	private String name;

	@Min(1)
	private int courts;

	@Min(15)
	private int durationUnitInMinutes;

	@Min(0)
	@Max(23)
	private int openingHour;

	@Min(1)
	@Max(24)
	private int closingHour;
}
