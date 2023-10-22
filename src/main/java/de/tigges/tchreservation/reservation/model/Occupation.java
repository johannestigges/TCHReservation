package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class Occupation {

	private long id;

	private String text;
	private LocalDate date;
	private LocalTime start;
	private int duration;

	private int court;
	private int lastCourt;

	private int type;

	private long systemConfigId;

	private Reservation reservation;
}
