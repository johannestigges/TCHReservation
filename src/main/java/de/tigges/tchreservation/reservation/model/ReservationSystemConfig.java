package de.tigges.tchreservation.reservation.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSystemConfig {

	private long id;

	private String name;

	private List<String> courts;

	private int durationUnitInMinutes;

	private int maxDaysReservationInFuture;

	private int maxDuration;

	private int openingHour;

	private int closingHour;

	private String title;
}
