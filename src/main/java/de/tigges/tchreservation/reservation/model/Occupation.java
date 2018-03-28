package de.tigges.tchreservation.reservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Occupation {
	
	@Id
	@GeneratedValue
	private long id;
	
	private int court;
	private int lastCourt;
	private LocalDate date;
	private LocalTime start;
	private int duration;
	private String text;
	private ReservationType type;

	private long systemConfigId;
	
	public long getSystemConfigId() {
		return systemConfigId;
	}
	public void setSystemConfigId(long systemConfigId) {
		this.systemConfigId = systemConfigId;
	}
	@ManyToOne(optional=false)
	private Reservation reservation;
	
	public int getCourt() {
		return court;
	}
	public void setCourt(int court) {
		this.court = court;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public LocalTime getStart() {
		return start;
	}
	public void setStart(LocalTime start) {
		this.start = start;
	}
	public int getDuration() {
		return duration;	
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public Reservation getReservation() {
		return reservation;
	}
	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}
	public int getLastCourt() {
		return lastCourt;
	}
	public void setLastCourt(int lastCourt) {
		this.lastCourt = lastCourt;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public ReservationType getType() {
		return type;
	}
	public void setType(ReservationType type) {
		this.type = type;
	}

}
