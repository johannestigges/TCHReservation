package de.tigges.tchreservation.reservation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSystemConfig {

	private long id;

	private String name;

	private String title;

	private List<String> courts;

	private int durationUnitInMinutes;

	private int maxDaysReservationInFuture;

	private int maxDuration;

	private int openingHour;

	private int closingHour;

	private List<SystemConfigReservationType> types;
}
