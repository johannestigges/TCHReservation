package de.tigges.tchreservation.reservation;

import java.time.LocalDate;

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
	private LocalDate start;
	private int duration;
	
	@ManyToOne
	private Reservation reservation;
	
	public int getCourt() {
		return court;
	}
	public void setCourt(int court) {
		this.court = court;
	}
	public LocalDate getStart() {
		return start;
	}
	public void setStart(LocalDate start) {
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

}
